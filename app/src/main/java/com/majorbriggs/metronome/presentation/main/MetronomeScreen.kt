package com.majorbriggs.metronome.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.majorbriggs.metronome.presentation.MetronomeUiState
import com.majorbriggs.metronome.presentation.components.ArcRing
import com.majorbriggs.metronome.presentation.components.BeatIndicatorRow
import com.majorbriggs.metronome.presentation.components.FeedbackModeButton
import com.majorbriggs.metronome.presentation.components.BpmAdjustButton
import com.majorbriggs.metronome.presentation.components.PlayStopButton
import com.majorbriggs.metronome.presentation.components.TapTempoButton
import com.majorbriggs.metronome.presentation.components.TimeSigButton
import com.majorbriggs.metronome.presentation.theme.BgPrimary
import com.majorbriggs.metronome.presentation.theme.OutfitFamily

@Composable
fun MetronomeScreen(
    state: MetronomeUiState,
    onTogglePlay: () -> Unit,
    onRotaryScroll: (Float) -> Unit,
    onAdjustBpm: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onNavigateToTimeSig: () -> Unit,
    onToggleFeedbackMode: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .onRotaryScrollEvent { event ->
                onRotaryScroll(event.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        ArcRing(
            bpm = state.bpm,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.offset(y = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            BeatIndicatorRow(
                beats = state.timeSignature.beatsPerBar,
                currentBeat = state.currentBeat,
                accentedBeats = state.timeSignature.accentedBeats,
                isRunning = state.isRunning
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BpmAdjustButton(
                    label = "−",
                    onPress = { onAdjustBpm(-1) },
                    onRepeat = { onAdjustBpm(-5) }
                )
                Text(
                    text = "${state.bpm}",
                    style = TextStyle(
                        fontFamily = OutfitFamily,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 50.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                    modifier = Modifier
                        .width(100.dp)
                        .semantics { contentDescription = "${state.bpm} BPM" },
                    textAlign = TextAlign.Center
                )
                BpmAdjustButton(
                    label = "+",
                    onPress = { onAdjustBpm(1) },
                    onRepeat = { onAdjustBpm(5) }
                )
            }

            Row(
                modifier = Modifier.padding(bottom = 10.dp), 
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeSigButton(
                    sig = state.timeSignature,
                    onClick = onNavigateToTimeSig
                )
                FeedbackModeButton(
                    mode = state.feedbackMode,
                    onClick = onToggleFeedbackMode
                )
                TapTempoButton(onClick = onTapTempo)
            }

            PlayStopButton(
                isPlaying = state.isRunning,
                onClick = onTogglePlay,
            )
        }
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

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun MetronomeScreenPreview() {
    MetronomeScreen(
        state = MetronomeUiState(bpm = 120, isRunning = false),
        onTogglePlay = {},
        onRotaryScroll = {},
        onAdjustBpm = {},
        onTapTempo = {},
        onNavigateToTimeSig = {},
        onToggleFeedbackMode = {}
    )
}
