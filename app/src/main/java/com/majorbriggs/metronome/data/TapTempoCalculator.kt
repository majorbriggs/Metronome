package com.majorbriggs.metronome.data

import javax.inject.Inject

class TapTempoCalculator @Inject constructor() {

    private val tapTimes = mutableListOf<Long>()

    fun tap(): Int? {
        val now = System.currentTimeMillis()
        if (tapTimes.isNotEmpty() && now - tapTimes.last() > RESET_WINDOW_MS) tapTimes.clear()
        tapTimes.add(now)
        if (tapTimes.size > MAX_TAPS) tapTimes.removeAt(0)
        return if (tapTimes.size >= 2) {
            val avg = tapTimes.zipWithNext { a, b -> b - a }.average()
            (60_000.0 / avg).toInt()
        } else null
    }

    companion object {
        private const val RESET_WINDOW_MS = 2000L
        private const val MAX_TAPS = 8
    }
}
