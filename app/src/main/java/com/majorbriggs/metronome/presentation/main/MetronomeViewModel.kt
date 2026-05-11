package com.majorbriggs.metronome.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.MetronomeDefaults.DEFAULT_BPM
import com.majorbriggs.metronome.data.TapTempoCalculator
import com.majorbriggs.metronome.data.MetronomeDefaults.MAX_BPM
import com.majorbriggs.metronome.data.MetronomeDefaults.MIN_BPM
import com.majorbriggs.metronome.data.MetronomePreferences
import com.majorbriggs.metronome.data.MetronomeRepository
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.presentation.MetronomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class MetronomeViewModel @Inject constructor(
    private val repository: MetronomeRepository,
    private val preferences: MetronomePreferences,
    private val tapTempoCalculator: TapTempoCalculator
) : ViewModel() {

    private val _bpm = MutableStateFlow(DEFAULT_BPM)

    init {
        // Seed from persisted value on startup
        viewModelScope.launch {
            _bpm.value = preferences.bpm.first()
        }
        // Commit to service + prefs only after user stops changing BPM
        viewModelScope.launch {
            _bpm
                .drop(1)
                .debounce(BPM_COMMIT_DEBOUNCE_MS)
                .collect { commitBpm(it) }
        }
    }

    val state: StateFlow<MetronomeUiState> = combine(
        repository.isRunning,
        repository.currentBeat,
        _bpm,
        preferences.timeSignature,
        preferences.feedbackMode
    ) { isRunning, currentBeat, bpm, timeSignature, feedbackMode ->
        MetronomeUiState(bpm, timeSignature, feedbackMode, isRunning, currentBeat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MetronomeUiState()
    )

    fun onRotaryScroll(pixels: Float) {
        val tempoDiff = ((MAX_BPM - MIN_BPM) * pixels / SCROLL_SENSITIVITY).toInt()
        _bpm.value = (_bpm.value + tempoDiff).coerceIn(MIN_BPM, MAX_BPM)
    }

    fun togglePlayPause() {
        val s = state.value
        if (s.isRunning) repository.stop()
        else repository.start(s.bpm, s.timeSignature, s.feedbackMode)
    }

    fun setBpm(bpm: Int) {
        _bpm.value = bpm.coerceIn(MIN_BPM, MAX_BPM)
    }

    fun adjustBpm(delta: Int) {
        _bpm.value = (_bpm.value + delta).coerceIn(MIN_BPM, MAX_BPM)
    }

    fun setTimeSignature(ts: TimeSignature) {
        repository.sendUpdate(state.value.bpm, ts, state.value.feedbackMode)
        viewModelScope.launch { preferences.setTimeSignature(ts) }
    }

    fun toggleFeedbackMode() {
        val next = when (state.value.feedbackMode) {
            FeedbackMode.AUDIO -> FeedbackMode.VIBRATION
            FeedbackMode.VIBRATION -> FeedbackMode.AUDIO
        }
        repository.sendUpdate(state.value.bpm, state.value.timeSignature, next)
        viewModelScope.launch { preferences.setFeedbackMode(next) }
    }

    fun onTapTempo() {
        tapTempoCalculator.tap()?.let { setBpm(it) }
    }

    private fun commitBpm(bpm: Int) {
        repository.sendUpdate(bpm, state.value.timeSignature, state.value.feedbackMode)
        viewModelScope.launch { preferences.setBpm(bpm) }
    }

    companion object {
        private const val SCROLL_SENSITIVITY = 10000f
        private const val BPM_COMMIT_DEBOUNCE_MS = 500L
    }
}
