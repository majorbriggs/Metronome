package com.majorbriggs.metronome.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.majorbriggs.metronome.presentation.feedbackmode.FeedbackModeScreen
import com.majorbriggs.metronome.presentation.main.AmbientMetronomeScreen
import com.majorbriggs.metronome.presentation.main.MetronomeScreen
import com.majorbriggs.metronome.presentation.main.MetronomeViewModel
import com.majorbriggs.metronome.presentation.theme.MetronomeTheme
import com.majorbriggs.metronome.presentation.timesignature.TimeSignatureScreen
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

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

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
                                onNavigateToFeedbackMode = { navController.navigate("feedback_mode") }
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
                        composable("feedback_mode") {
                            FeedbackModeScreen(
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
