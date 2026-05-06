package com.majorbriggs.metronome.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun IconVibrate(tint: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 18f
        val sw = 1.5f * s
        drawRoundRect(
            color = tint,
            topLeft = Offset(5 * s, 3 * s),
            size = Size(8 * s, 12 * s),
            cornerRadius = CornerRadius(1.5f * s),
            style = Stroke(width = sw, cap = StrokeCap.Round)
        )
        drawLine(tint, Offset(2 * s, 6 * s), Offset(2 * s, 12 * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(16 * s, 6 * s), Offset(16 * s, 12 * s), sw, cap = StrokeCap.Round)
    }
}

@Composable
fun IconAudio(tint: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 18f
        val stroke = Stroke(width = 1.5f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val speaker = Path().apply {
            moveTo(3 * s, 6.5f * s); lineTo(6 * s, 6.5f * s)
            lineTo(10 * s, 3.5f * s); lineTo(10 * s, 14.5f * s)
            lineTo(6 * s, 11.5f * s); lineTo(3 * s, 11.5f * s); close()
        }
        drawPath(speaker, color = tint, style = stroke)
        val w1 = Path().apply {
            moveTo(13 * s, 6 * s)
            cubicTo(14.1f * s, 7.1f * s, 14.1f * s, 10.9f * s, 13 * s, 12 * s)
        }
        drawPath(w1, color = tint, style = stroke)
        val w2 = Path().apply {
            moveTo(15 * s, 4.5f * s)
            cubicTo(17.2f * s, 6.7f * s, 17.2f * s, 11.3f * s, 15 * s, 13.5f * s)
        }
        drawPath(w2, color = tint, style = stroke)
    }
}

@Composable
fun IconBoth(tint: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.width / 18f
        val sw = 1.5f * s
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Speaker in top-left (original coords scaled by 0.6, centered ~(3.5, 4))
        val speakerBody = Path().apply {
            moveTo(1.4f * s, 2.5f * s)
            lineTo(3.2f * s, 2.5f * s)
            lineTo(5.6f * s, 0.7f * s)
            lineTo(5.6f * s, 7.3f * s)
            lineTo(3.2f * s, 5.5f * s)
            lineTo(1.4f * s, 5.5f * s)
            close()
        }
        drawPath(speakerBody, color = tint, style = stroke)
        val speakerWave = Path().apply {
            moveTo(7.4f * s, 2.2f * s)
            cubicTo(8.06f * s, 2.86f * s, 8.06f * s, 5.14f * s, 7.4f * s, 5.8f * s)
        }
        drawPath(speakerWave, color = tint, style = stroke)

        // Vibration in bottom-right (original coords scaled by 0.6, centered ~(13, 13))
        drawRoundRect(
            color = tint,
            topLeft = Offset(10.4f * s, 9.4f * s),
            size = Size(4.8f * s, 7.2f * s),
            cornerRadius = CornerRadius(0.9f * s),
            style = Stroke(width = sw, cap = StrokeCap.Round)
        )
        drawLine(tint, Offset(8.6f * s, 11.2f * s), Offset(8.6f * s, 14.8f * s), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(17.0f * s, 11.2f * s), Offset(17.0f * s, 14.8f * s), sw, cap = StrokeCap.Round)

    }
}