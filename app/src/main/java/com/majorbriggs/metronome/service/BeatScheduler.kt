package com.majorbriggs.metronome.service

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock
import com.majorbriggs.metronome.data.TimeSignature
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

fun interface BeatListener {
    fun onBeat(beatIndex: Int, isAccent: Boolean)
}

@ServiceScoped
class BeatScheduler @Inject constructor() {
    private val handlerThread = HandlerThread("MetronomeThread", Process.THREAD_PRIORITY_URGENT_AUDIO)
    private val handler: Handler

    @Volatile private var bpm = 120
    @Volatile private var timeSignature: TimeSignature = TimeSignature.FOUR_FOUR
    @Volatile private var isRunning = false

    private var beatIndex = 0
    private var nextBeatTime = 0L
    private var listener: BeatListener? = null

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    fun setListener(l: BeatListener) {
        listener = l
    }

    fun start(bpm: Int, timeSignature: TimeSignature) {
        this.bpm = bpm
        this.timeSignature = timeSignature
        beatIndex = 0
        isRunning = true
        nextBeatTime = SystemClock.uptimeMillis()
        handler.post { tick() }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        beatIndex = 0
    }

    fun updateBpm(newBpm: Int) {
        bpm = newBpm
    }

    fun updateTimeSignature(newSig: TimeSignature) {
        timeSignature = newSig
        beatIndex = 0
    }

    private fun tick() {
        if (!isRunning) return
        val sig = timeSignature
        val isAccent = sig.accentedBeats.contains(beatIndex)
        listener?.onBeat(beatIndex, isAccent)
        beatIndex = (beatIndex + 1) % sig.beatsPerBar
        nextBeatTime += 60_000L / bpm
        handler.postAtTime({ tick() }, nextBeatTime)
    }

    fun release() {
        stop()
        handlerThread.quitSafely()
    }
}
