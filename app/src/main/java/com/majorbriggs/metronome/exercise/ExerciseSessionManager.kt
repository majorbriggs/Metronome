package com.majorbriggs.metronome.exercise

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

// Placeholder for Health Services exercise integration.
// The foreground service + PARTIAL_WAKE_LOCK provide the background reliability guarantees.
// A full health-services-client exercise session can be added here to additionally
// signal an active workout to the OS scheduler.
@Singleton
class ExerciseSessionManager @Inject constructor() {
    fun startSession() = Log.d("ExerciseSession", "Session started")
    fun endSession() = Log.d("ExerciseSession", "Session ended")
}
