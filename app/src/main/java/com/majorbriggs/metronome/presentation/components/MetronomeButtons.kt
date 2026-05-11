package com.majorbriggs.metronome.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.presentation.theme.IconAudio
import com.majorbriggs.metronome.presentation.theme.IconVibrate
import com.majorbriggs.metronome.presentation.theme.OutfitFamily

@Composable
fun TimeSigButton(sig: TimeSignature, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(1.5.dp, Color(0xFF3A3A3A), CircleShape)
            .clickable { onClick() }
            .semantics { contentDescription = "Time signature ${sig.displayName}, tap to change" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sig.displayName,
            fontFamily = OutfitFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAAAAAA)
        )
    }
}

@Composable
fun FeedbackModeButton(mode: FeedbackMode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(1.5.dp, Color(0xFF3A3A3A), CircleShape)
            .clickable { onClick() }
            .semantics {
                contentDescription = "Feedback mode: ${mode.name.lowercase()}, tap to change"
            },
        contentAlignment = Alignment.Center
    ) {
        FeedbackModeIcon(mode = mode, tint = Color(0xFF888888), size = 18.dp)
    }
}

@Composable
fun TapTempoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(1.5.dp, Color(0xFF3A3A3A), CircleShape)
            .clickable { onClick() }
            .semantics {
                contentDescription = "Tap tempo"
            },
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "TAP",
            fontFamily = OutfitFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAAAAAA),
            letterSpacing = 0.08.em
        )
    }
}

@Composable
fun FeedbackModeIcon(mode: FeedbackMode, tint: Color, size: Dp = 18.dp) {
    when (mode) {
        FeedbackMode.VIBRATION -> IconVibrate(tint = tint, size = size)
        FeedbackMode.AUDIO -> IconAudio(tint = tint, size = size)
    }
}
