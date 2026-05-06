package com.majorbriggs.metronome.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.AccentDim
import com.majorbriggs.metronome.presentation.theme.MetronomeTheme

@Composable
fun PlayStopButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isPlaying) AccentDim else Color.White)
            .then(
                if (isPlaying) Modifier.border(1.5.dp, Accent, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (isPlaying) "Stop metronome" else "Start metronome"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    Modifier
                        .size(4.dp, 14.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Accent)
                )
                Box(
                    Modifier
                        .size(4.dp, 14.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Accent)
                )
            }
        } else {
            Canvas(Modifier.size(18.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.22f, 0f)
                    lineTo(size.width * 0.22f, size.height)
                    lineTo(size.width * 0.95f, size.height / 2f)
                    close()
                }
                drawPath(path, color = Color(0xFF0F0F0F))
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Composable
fun PlayStopButtonPreview() {
    MetronomeTheme {
        PlayStopButton(isPlaying = false, onClick = {})
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Composable
fun PlayStopButtonPlayingPreview() {
    MetronomeTheme {
        PlayStopButton(isPlaying = true, onClick = {})
    }
}
