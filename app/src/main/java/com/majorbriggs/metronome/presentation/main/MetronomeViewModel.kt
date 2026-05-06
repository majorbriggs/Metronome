package com.majorbriggs.metronome.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.MetronomePreferences
import com.majorbriggs.metronome.data.MetronomeRepository
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.presentation.MetronomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        repository.sendUpdate(clamped, state.value.timeSignature, state.value.feedbackMode)
        viewModelScope.launch { preferences.setBpm(clamped) }
    }

    fun setTimeSignature(ts: TimeSignature) {
        repository.sendUpdate(state.value.bpm, ts, state.value.feedbackMode)
        viewModelScope.launch { preferences.setTimeSignature(ts) }
    }

    fun setFeedbackMode(mode: FeedbackMode) {
        repository.sendUpdate(state.value.bpm, state.value.timeSignature, mode)
        viewModelScope.launch { preferences.setFeedbackMode(mode) }
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
