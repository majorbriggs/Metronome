package com.majorbriggs.metronome.data

import android.content.Context
import android.content.Intent
import com.majorbriggs.metronome.exercise.ExerciseSessionManager
import com.majorbriggs.metronome.service.MetronomeService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetronomeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val exerciseSessionManager: ExerciseSessionManager
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentBeat = MutableStateFlow(-1)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    fun start(bpm: Int, timeSignature: TimeSignature, feedbackMode: FeedbackMode) {
        context.startForegroundService(
            Intent(context, MetronomeService::class.java).apply {
                action = MetronomeService.ACTION_START
                putExtra(MetronomeService.EXTRA_BPM, bpm)
                putExtra(MetronomeService.EXTRA_TIME_SIG, timeSignature.key)
                putExtra(MetronomeService.EXTRA_FEEDBACK_MODE, feedbackMode.key)
            }
        )
        _isRunning.value = true
        exerciseSessionManager.startSession()
    }

    fun stop() {
        context.startService(
            Intent(context, MetronomeService::class.java).apply {
                action = MetronomeService.ACTION_STOP
            }
        )
        markStopped()
    }

    fun sendUpdate(bpm: Int, timeSignature: TimeSignature, feedbackMode: FeedbackMode) {
        if (!_isRunning.value) return
        context.startService(
            Intent(context, MetronomeService::class.java).apply {
                action = MetronomeService.ACTION_UPDATE
                putExtra(MetronomeService.EXTRA_BPM, bpm)
                putExtra(MetronomeService.EXTRA_TIME_SIG, timeSignature.key)
                putExtra(MetronomeService.EXTRA_FEEDBACK_MODE, feedbackMode.key)
            }
        )
    }

    fun onBeat(beatIndex: Int) {
        _currentBeat.value = beatIndex
    }

    fun onServiceStopped() = markStopped()

    private fun markStopped() {
        _isRunning.value = false
        _currentBeat.value = -1
        exerciseSessionManager.endSession()
    }
}
