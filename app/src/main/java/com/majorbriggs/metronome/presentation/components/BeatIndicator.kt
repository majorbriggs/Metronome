package com.majorbriggs.metronome.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.MetronomeTheme

@Composable
fun BeatIndicatorRow(
    beats: Int,
    currentBeat: Int,
    accentedBeats: Set<Int>,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        for (i in 0 until beats) {
            BeatDot(
                isActive = isRunning && currentBeat == i,
                isAccent = accentedBeats.contains(i)
            )
        }
    }
}

@Composable
private fun BeatDot(isActive: Boolean, isAccent: Boolean) {
    val sizeDp = if (isAccent) 10.dp else 8.dp
    val color = when {
        isActive && isAccent -> Color.White
        isActive -> Accent
        isAccent -> Color(0xFF666666)
        else -> Color(0xFF333333)
    }

    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Composable
fun BeatIndicatorPreview() {
    MetronomeTheme {
        BeatIndicatorRow(
            beats = 4,
            currentBeat = 0,
            accentedBeats = setOf(0),
            isRunning = true
        )
    }
}
