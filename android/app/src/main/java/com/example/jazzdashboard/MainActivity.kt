package com.example.jazzdashboard
import com.example.jazzdashboard.sensors.SensorTracker
import com.example.jazzdashboard.location.LocationTracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.jazzdashboard.ui.DashboardApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var locationTracker: LocationTracker
    private lateinit var sensorTracker: SensorTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep Screen On & Fullscreen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Immersive Mode
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        locationTracker = LocationTracker(this)
        sensorTracker = SensorTracker(this)

        setContent {
            DashboardApp(viewModel)
        }

        // Request GPS Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }

        // Observe GPS toggle state to start/stop tracking
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val hasLocationPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || 
                                            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (state.gpsEnabled && hasLocationPermission) {
                    locationTracker.startTracking()
                } else {
                    locationTracker.stopTracking()
                }
            }
        }

        // Observe Mode to start/stop Sensor tracking (only needed in SPORT)
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.mode == AppMode.SPORT) {
                    sensorTracker.startTracking()
                } else {
                    sensorTracker.stopTracking()
                }
            }
        }

        // Observe Location/Sensor flows and update ViewModel
        lifecycleScope.launch {
            locationTracker.speedMph.collect { speed ->
                viewModel.updateSpeed(speed)
            }
        }

        lifecycleScope.launch {
            locationTracker.isLocked.collect { isLocked ->
                viewModel.updateGpsLocked(isLocked)
            }
        }

        lifecycleScope.launch {
            locationTracker.locationName.collect { name ->
                viewModel.updateLocationName(name)
            }
        }

        lifecycleScope.launch {
            sensorTracker.gForceX.collect { x ->
                viewModel.updateGForce(x, sensorTracker.gForceY.value)
            }
        }
        
        lifecycleScope.launch {
            sensorTracker.gForceY.collect { y ->
                viewModel.updateGForce(sensorTracker.gForceX.value, y)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, if UI says GPS is enabled, start it.
            if (viewModel.uiState.value.gpsEnabled) {
                locationTracker.startTracking()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
        sensorTracker.stopTracking()
    }
}

