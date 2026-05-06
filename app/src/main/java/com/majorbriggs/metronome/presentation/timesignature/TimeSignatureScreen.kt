package com.majorbriggs.metronome.presentation.timesignature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.presentation.theme.Accent
import com.majorbriggs.metronome.presentation.theme.AccentDim
import com.majorbriggs.metronome.presentation.theme.BgSurface
import com.majorbriggs.metronome.presentation.theme.OutfitFamily
import com.majorbriggs.metronome.presentation.theme.TextSecondary

@Composable
fun TimeSignatureScreen(
    selectedSig: TimeSignature,
    onSelect: (TimeSignature) -> Unit
) {
    val items = TimeSignature.entries
    val selectedIdx = items.indexOf(selectedSig).coerceAtLeast(0)
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
                        text = "TIME SIGNATURE",
                        fontFamily = OutfitFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            items(items.size) { idx ->
                val sig = items[idx]
                val isSelected = sig == selectedSig

                Chip(
                    onClick = { onSelect(sig) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                "${sig.displayName}${if (isSelected) ", selected" else ""}"
                        },
                    label = {
                        Text(
                            text = sig.displayName,
                            fontFamily = OutfitFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Accent else MaterialTheme.colors.onSurface
                        )
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
