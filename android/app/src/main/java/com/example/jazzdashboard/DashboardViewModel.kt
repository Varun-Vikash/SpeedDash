package com.example.jazzdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import kotlinx.coroutines.launch

enum class AppMode {
    INTRO, ECO, SPORT
}

data class DashboardState(
    val mode: AppMode = AppMode.INTRO,
    val speed: Int = 0,
    val rpm: Float = 800f,
    val gear: String = "P",
    val gpsEnabled: Boolean = false,
    val gpsLocked: Boolean = false,
    val locationName: String = "...",
    val gForceX: Float = 0f,
    val gForceY: Float = 0f,
    val stopwatchRunning: Boolean = false,
    val stopwatchElapsedMs: Long = 0L,
    val lastLapTime: String = "LAST: --:--.--",
    val isSwiping: Boolean = false,
    // Telemetry (static values matching HTML)
    val waterTemp: Int = 90,
    val oilTemp: Int = 102,
    val boostPressure: Float = 0.8f,
    val voltage: Float = 14.2f
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    fun setMode(newMode: AppMode) {
        _uiState.value = _uiState.value.copy(mode = newMode)
        // Reset gear based on mode, matching HTML line 720
        if (newMode == AppMode.ECO) {
            _uiState.value = _uiState.value.copy(gear = "P", rpm = 800f, speed = 0)
        } else if (newMode == AppMode.SPORT) {
            _uiState.value = _uiState.value.copy(gear = "N", rpm = 800f, speed = 0)
        }
    }

    fun toggleGps() {
        val newState = !_uiState.value.gpsEnabled
        _uiState.value = _uiState.value.copy(gpsEnabled = newState, gpsLocked = false)
    }

    fun updateSpeed(speedMph: Float) {
        val speedKmh = (speedMph * 1.60934).toInt().coerceAtLeast(0)
        if (!_uiState.value.isSwiping) {
            updateDisplayFromSpeed(speedKmh)
        }
    }

    fun updateGpsLocked(isLocked: Boolean) {
        _uiState.value = _uiState.value.copy(gpsLocked = isLocked)
    }

    fun updateLocationName(name: String) {
        _uiState.value = _uiState.value.copy(locationName = name)
    }

    /**
     * Port of HTML updateDisplay() lines 719-742.
     * Calculates gear and RPM from speed based on current mode.
     */
    private fun updateDisplayFromSpeed(roundedSpeed: Int) {
        val mode = _uiState.value.mode
        var gear: String
        var rpm: Float

        if (roundedSpeed < 1) {
            // Stationary
            gear = if (mode == AppMode.ECO) "P" else "N"
            rpm = 800f + (Random.nextFloat() * 40f - 20f)
        } else if (mode == AppMode.ECO) {
            // ECO: always D, RPM follows specific curve (HTML line 721)
            gear = "D"
            rpm = if (roundedSpeed < 40) {
                800f + (roundedSpeed * 45f)
            } else {
                2400f + ((roundedSpeed - 40) * 18f)
            }
        } else {
            // SPORT: 7-speed manual (HTML line 723)
            when {
                roundedSpeed < 45 -> { gear = "1"; rpm = roundedSpeed * 155f }
                roundedSpeed < 75 -> { gear = "2"; rpm = roundedSpeed * 95f }
                roundedSpeed < 105 -> { gear = "3"; rpm = roundedSpeed * 70f }
                roundedSpeed < 135 -> { gear = "4"; rpm = roundedSpeed * 55f }
                roundedSpeed < 160 -> { gear = "5"; rpm = roundedSpeed * 45f }
                roundedSpeed < 185 -> { gear = "6"; rpm = roundedSpeed * 40f }
                else -> { gear = "7"; rpm = roundedSpeed * 36f }
            }
        }

        // RPM cap at 7200 + random jitter (HTML line 725)
        if (rpm > 7200f) {
            rpm = 7200f + (Random.nextFloat() * 200f - 100f)
        }
        if (rpm < 800f && roundedSpeed > 1) {
            rpm = 800f
        }

        _uiState.value = _uiState.value.copy(
            speed = roundedSpeed,
            rpm = rpm,
            gear = gear
        )
    }

    fun updateGForce(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(gForceX = x, gForceY = y)
    }

    // Stopwatch logic
    fun toggleStopwatch() {
        val isRunning = _uiState.value.stopwatchRunning
        if (!isRunning) {
            _uiState.value = _uiState.value.copy(stopwatchRunning = true)
            startStopwatchTimer()
        } else {
            _uiState.value = _uiState.value.copy(stopwatchRunning = false)
        }
    }

    fun resetStopwatch() {
        if (_uiState.value.stopwatchRunning) {
            // If running, RESET acts as LAP (HTML line 376-379)
            val display = formatTimeString(_uiState.value.stopwatchElapsedMs)
            _uiState.value = _uiState.value.copy(lastLapTime = "LAP: $display")
        } else {
            _uiState.value = _uiState.value.copy(
                stopwatchElapsedMs = 0L,
                lastLapTime = "LAST: --:--.--"
            )
        }
    }

    private fun formatTimeString(ms: Long): String {
        val m = (ms / 60000).toInt()
        val s = ((ms % 60000) / 1000).toInt()
        val cs = ((ms % 1000) / 10).toInt()
        return String.format(java.util.Locale.US, "%02d:%02d.%02d", m, s, cs)
    }

    private fun startStopwatchTimer() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis() - _uiState.value.stopwatchElapsedMs
            while (_uiState.value.stopwatchRunning) {
                delay(30)
                _uiState.value = _uiState.value.copy(
                    stopwatchElapsedMs = System.currentTimeMillis() - startTime
                )
            }
        }
    }

    // Simulated Swipe (matching HTML startNeedleSwipe lines 449-513)
    fun startSwipeAnimation() {
        if (_uiState.value.isSwiping) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSwiping = true)
            val duration = 1500L
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= duration) break

                val progress = elapsed.toFloat() / duration
                var v = if (progress < 0.5f) progress * 2f else 2f - (progress * 2f)
                v = v * (1f - (v - 1f) * (v - 1f))
                v = v.coerceAtLeast(0f)

                val swipeRpm = v * 8000f
                val swipeSpeed = (v * 188f).toInt()

                _uiState.value = _uiState.value.copy(rpm = swipeRpm, speed = swipeSpeed)
                delay(16) // ~60fps
            }

            _uiState.value = _uiState.value.copy(isSwiping = false, rpm = 800f, speed = 0)
        }
    }
}
