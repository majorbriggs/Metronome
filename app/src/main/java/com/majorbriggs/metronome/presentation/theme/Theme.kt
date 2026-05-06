package com.majorbriggs.metronome.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography
import com.majorbriggs.metronome.R

// Design system color tokens (amber accent, OLED dark)
val Accent       = Color(0xFFF0B429)
val AccentDim    = Color(0x26F0B429)
val BgPrimary    = Color.Black
val BgSurface    = Color(0xFF1C1C1C)
val BgElevated   = Color(0xFF2A2A2A)
val Border       = Color(0xFF2E2E2E)
val TextPrimary  = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF888888)

val OutfitFamily: FontFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

private val MetronomeColors = Colors(
    primary = Accent,
    primaryVariant = Accent,
    secondary = TextSecondary,
    secondaryVariant = TextSecondary,
    background = BgPrimary,
    surface = BgSurface,
    error = Color(0xFFEE675C),
    onPrimary = BgPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = BgPrimary,
)

@Composable
fun MetronomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MetronomeColors,
        content = content
    )
}
