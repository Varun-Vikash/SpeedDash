package com.example.jazzdashboard.ui
import com.example.jazzdashboard.ui.screens.SportScreen
import com.example.jazzdashboard.ui.screens.IntroScreen
import com.example.jazzdashboard.ui.screens.EcoScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.jazzdashboard.AppMode
import com.example.jazzdashboard.DashboardViewModel
import com.example.jazzdashboard.ui.theme.BackgroundDark
import com.example.jazzdashboard.ui.theme.JazzDashboardTheme

@Composable
fun DashboardApp(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()

    JazzDashboardTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundDark
        ) {
            Crossfade(targetState = state.mode, label = "Mode Switch") { mode ->
                when (mode) {
                    AppMode.INTRO -> {
                        IntroScreen(
                            onStartEngine = { viewModel.setMode(AppMode.ECO) }
                        )
                    }
                    AppMode.ECO -> {
                        EcoScreen(
                            state = state,
                            onModeChange = { viewModel.setMode(it) },
                            onToggleGps = { viewModel.toggleGps() },
                            onStopEngine = { viewModel.setMode(AppMode.INTRO) }
                        )
                    }
                    AppMode.SPORT -> {
                        SportScreen(
                            state = state,
                            onModeChange = { viewModel.setMode(it) },
                            onSimulateSwipe = { viewModel.startSwipeAnimation() },
                            onToggleStopwatch = { viewModel.toggleStopwatch() },
                            onResetStopwatch = { viewModel.resetStopwatch() },
                            onToggleGps = { viewModel.toggleGps() }
                        )
                    }
                }
            }
        }
    }
}



