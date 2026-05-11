package com.majorbriggs.metronome.presentation

import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.MetronomeDefaults
import com.majorbriggs.metronome.data.TimeSignature

data class MetronomeUiState(
    val bpm: Int = MetronomeDefaults.DEFAULT_BPM,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val feedbackMode: FeedbackMode = FeedbackMode.AUDIO,
    val isRunning: Boolean = false,
    val currentBeat: Int = -1
)
