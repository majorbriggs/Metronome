package com.majorbriggs.metronome.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.majorbriggs.metronome.presentation.theme.MetronomeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MetronomeViewModel by viewModels()
    private var isAmbient by mutableStateOf(false)

    private val ambientObserver = AmbientLifecycleObserver(
        this,
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
            }
            override fun onExitAmbient() {
                isAmbient = false
            }
            override fun onUpdateAmbient() {}
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        lifecycle.addObserver(ambientObserver)

        setContent {
            MetronomeTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()

                if (isAmbient) {
                    AmbientMetronomeScreen(state = state)
                } else {
                    val navController = rememberSwipeDismissableNavController()
                    SwipeDismissableNavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MetronomeScreen(
                                state = state,
                                onTogglePlay = viewModel::togglePlayPause,
                                onBpmChange = viewModel::setBpm,
                                onTapTempo = viewModel::onTapTempo,
                                onNavigateToTimeSig = { navController.navigate("time_sig") },
                                onNavigateToIndication = { navController.navigate("indication") }
                            )
                        }
                        composable("time_sig") {
                            TimeSignatureScreen(
                                selectedSig = state.timeSignature,
                                onSelect = { sig ->
                                    viewModel.setTimeSignature(sig)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("indication") {
                            IndicationScreen(
                                selectedMode = state.feedbackMode,
                                onSelect = { mode ->
                                    viewModel.setFeedbackMode(mode)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
