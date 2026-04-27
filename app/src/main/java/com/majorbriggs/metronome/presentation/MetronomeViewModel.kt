package com.majorbriggs.metronome.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.MetronomePreferences
import com.majorbriggs.metronome.data.MetronomeRepository
import com.majorbriggs.metronome.data.TimeSignature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetronomeUiState(
    val bpm: Int = 120,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val feedbackMode: FeedbackMode = FeedbackMode.BOTH,
    val isRunning: Boolean = false,
    val currentBeat: Int = -1
)

@HiltViewModel
class MetronomeViewModel @Inject constructor(
    private val repository: MetronomeRepository,
    private val preferences: MetronomePreferences
) : ViewModel() {

    val state: StateFlow<MetronomeUiState> = combine(
        repository.isRunning,
        repository.currentBeat,
        preferences.bpm,
        preferences.timeSignature,
        preferences.feedbackMode
    ) { isRunning, currentBeat, bpm, timeSignature, feedbackMode ->
        MetronomeUiState(bpm, timeSignature, feedbackMode, isRunning, currentBeat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MetronomeUiState()
    )

    private val tapTimes = mutableListOf<Long>()

    fun togglePlayPause() {
        val s = state.value
        if (s.isRunning) repository.stop()
        else repository.start(s.bpm, s.timeSignature, s.feedbackMode)
    }

    fun setBpm(bpm: Int) {
        val clamped = bpm.coerceIn(40, 240)
        viewModelScope.launch { preferences.setBpm(clamped) }
        repository.sendUpdate(clamped, state.value.timeSignature, state.value.feedbackMode)
    }

    fun cycleTimeSignature() {
        val next = when (state.value.timeSignature) {
            TimeSignature.FOUR_FOUR -> TimeSignature.THREE_FOUR
            TimeSignature.THREE_FOUR -> TimeSignature.SIX_EIGHT
            TimeSignature.SIX_EIGHT -> TimeSignature.FOUR_FOUR
        }
        viewModelScope.launch { preferences.setTimeSignature(next) }
        repository.sendUpdate(state.value.bpm, next, state.value.feedbackMode)
    }

    fun cycleFeedbackMode() {
        val next = when (state.value.feedbackMode) {
            FeedbackMode.VIBRATION -> FeedbackMode.AUDIO
            FeedbackMode.AUDIO -> FeedbackMode.BOTH
            FeedbackMode.BOTH -> FeedbackMode.VIBRATION
        }
        viewModelScope.launch { preferences.setFeedbackMode(next) }
        repository.sendUpdate(state.value.bpm, state.value.timeSignature, next)
    }

    fun setTimeSignature(ts: TimeSignature) {
        viewModelScope.launch { preferences.setTimeSignature(ts) }
        repository.sendUpdate(state.value.bpm, ts, state.value.feedbackMode)
    }

    fun setFeedbackMode(mode: FeedbackMode) {
        viewModelScope.launch { preferences.setFeedbackMode(mode) }
        repository.sendUpdate(state.value.bpm, state.value.timeSignature, mode)
    }

    fun onTapTempo() {
        val now = System.currentTimeMillis()
        if (tapTimes.isNotEmpty() && now - tapTimes.last() > 2000) tapTimes.clear()
        tapTimes.add(now)
        if (tapTimes.size >= 2) {
            val avg = tapTimes.zipWithNext { a, b -> b - a }.average()
            setBpm((60_000.0 / avg).toInt())
        }
        if (tapTimes.size > 8) tapTimes.removeAt(0)
    }
}
