package com.majorbriggs.metronome.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.AccentDim
import com.majorbriggs.metronome.presentation.theme.BgSurface
import com.majorbriggs.metronome.presentation.theme.OutfitFamily
import com.majorbriggs.metronome.presentation.theme.TextSecondary

@Composable
fun IndicationScreen(
    selectedMode: FeedbackMode,
    onSelect: (FeedbackMode) -> Unit
) {
    val items = FeedbackMode.entries
    val selectedIdx = items.indexOf(selectedMode).coerceAtLeast(0)
    // +1 because index 0 is the ListHeader
    val listState = rememberScalingLazyListState(initialCenterItemIndex = selectedIdx + 1)

    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            scalingParams = ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.65f,
                edgeAlpha = 0.2f,
                minTransitionArea = 0.35f,
                maxTransitionArea = 0.55f
            )
        ) {
            item {
                ListHeader {
                    Text(
                        text = "INDICATION",
                        fontFamily = OutfitFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            items(items.size) { idx ->
                val mode = items[idx]
                val isSelected = mode == selectedMode
                val iconTint = if (isSelected) Accent else MaterialTheme.colors.onSurface

                Chip(
                    onClick = { onSelect(mode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                "${mode.name.lowercase()}${if (isSelected) ", selected" else ""}"
                        },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IndicationIcon(mode = mode, tint = iconTint, size = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = when (mode) {
                                    FeedbackMode.VIBRATION -> "Vibration"
                                    FeedbackMode.AUDIO     -> "Audio"
                                    FeedbackMode.BOTH      -> "Both"
                                },
                                fontFamily = OutfitFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Accent else MaterialTheme.colors.onSurface
                            )
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (isSelected) AccentDim else BgSurface,
                        contentColor = if (isSelected) Accent else MaterialTheme.colors.onSurface
                    )
                )
            }
        }
    }
}
