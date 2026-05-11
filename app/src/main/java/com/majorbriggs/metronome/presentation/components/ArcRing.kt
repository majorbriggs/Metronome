package com.majorbriggs.metronome.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.tooling.preview.Preview
import com.majorbriggs.metronome.data.MetronomeDefaults
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.MetronomeTheme

@Composable
fun ArcRing(
    bpm: Int,
    modifier: Modifier = Modifier,
    minBpm: Int = MetronomeDefaults.MIN_BPM,
    maxBpm: Int = MetronomeDefaults.MAX_BPM
) {
    val pct = ((bpm - minBpm.toFloat()) / (maxBpm - minBpm).toFloat()).coerceIn(0f, 1f)
    val trackColor = Color(0xFF252525)

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val strokePx = 5.dp.toPx()
        val r = size.minDimension / 2f - strokePx / 2f
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
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun ArcRingPreview() {
    MetronomeTheme {
        ArcRing(bpm = 120)
    }
}
