package com.majorbriggs.metronome.presentation

import android.widget.Space
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.AccentDim
import com.majorbriggs.metronome.presentation.theme.BgPrimary
import com.majorbriggs.metronome.presentation.theme.IconAudio
import com.majorbriggs.metronome.presentation.theme.IconBoth
import com.majorbriggs.metronome.presentation.theme.IconVibrate
import com.majorbriggs.metronome.presentation.theme.OutfitFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MetronomeScreen(
    state: MetronomeUiState,
    onTogglePlay: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onNavigateToTimeSig: () -> Unit,
    onNavigateToIndication: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var bpmAccumulator by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .onRotaryScrollEvent { event ->
                val delta = event.verticalScrollPixels / 5000f
                bpmAccumulator = (bpmAccumulator + delta).coerceIn(0f, 1f)
                onBpmChange((40 + 200 * bpmAccumulator).toInt())
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        ArcRing(bpm = state.bpm, modifier = Modifier.fillMaxSize())

        // Content column shifted slightly down to center in the wider arc safe zone
        Column(
            modifier = Modifier.offset(y = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Beat dots
            Spacer(modifier = Modifier.height(10.dp))
            BeatIndicatorRow(
                beats = state.timeSignature.beatsPerBar,
                currentBeat = state.currentBeat,
                accentedBeats = state.timeSignature.accentedBeats,
                isRunning = state.isRunning
            )
            Text(
                text = "${state.bpm}",
                fontFamily = OutfitFamily,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.semantics { contentDescription = "${state.bpm} BPM" }
            )

            // Shortcut buttons row: time sig + indication mode
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeSigButton(
                    sig = state.timeSignature,
                    onClick = onNavigateToTimeSig
                )
                IndicationButton(
                    mode = state.feedbackMode,
                    onClick = onNavigateToIndication
                )
            }

            // Play / Stop button
            PlayStopButton(
                isPlaying = state.isRunning,
                onClick = onTogglePlay,
                onLongClick = onTapTempo
            )
        }
    }
}

@Composable
fun ArcRing(bpm: Int, modifier: Modifier = Modifier) {
    val pct = ((bpm - 40f) / 200f).coerceIn(0f, 1f)
    val trackColor = Color(0xFF252525)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = (size.minDimension / 2f) * 0.87f
        val strokePx = 5.dp.toPx()
        val startAngle = 140f
        val sweepAngle = 260f

        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        if (pct > 0f) {
            drawArc(
                color = Accent,
                startAngle = startAngle,
                sweepAngle = sweepAngle * pct,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        val thumbAngleRad = Math.toRadians((startAngle + sweepAngle * pct).toDouble())
        val thumbX = cx + r * cos(thumbAngleRad).toFloat()
        val thumbY = cy + r * sin(thumbAngleRad).toFloat()
        drawCircle(
            color = Accent,
            radius = 7.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )
    }
}

@Composable
fun TimeSigButton(sig: TimeSignature, onClick: () -> Unit) {
    Box(
        modifier = Modifier
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
fun IndicationButton(mode: FeedbackMode, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(1.5.dp, Color(0xFF3A3A3A), CircleShape)
            .clickable { onClick() }
            .semantics {
                contentDescription = "Indication mode: ${mode.name.lowercase()}, tap to change"
            },
        contentAlignment = Alignment.Center
    ) {
        IndicationIcon(mode = mode, tint = Color(0xFF888888), size = 16.dp)
    }
}

@Composable
fun PlayStopButton(isPlaying: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isPlaying) AccentDim else Color.White)
            .then(
                if (isPlaying) Modifier.border(1.5.dp, Accent, CircleShape)
                else Modifier
            )
            .pointerInput(isPlaying) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
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

@Composable
fun BeatIndicatorRow(
    beats: Int,
    currentBeat: Int,
    accentedBeats: Set<Int>,
    isRunning: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
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

@Composable
fun IndicationIcon(mode: FeedbackMode, tint: Color, size: Dp = 18.dp) {
    when (mode) {
        FeedbackMode.VIBRATION -> IconVibrate(tint = tint, size = size)
        FeedbackMode.AUDIO -> IconAudio(tint = tint, size = size)
        FeedbackMode.BOTH -> IconBoth(tint = tint, size = size)
    }
}

@Composable
fun AmbientMetronomeScreen(state: MetronomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${state.bpm}",
            fontFamily = OutfitFamily,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        BeatIndicatorRow(
            beats = state.timeSignature.beatsPerBar,
            currentBeat = if (state.isRunning) state.currentBeat else -1,
            accentedBeats = state.timeSignature.accentedBeats,
            isRunning = state.isRunning
        )
    }
}
