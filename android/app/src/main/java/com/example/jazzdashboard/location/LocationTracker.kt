package com.example.jazzdashboard.location
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationTracker(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    private val _speedMph = MutableStateFlow(0f)
    val speedMph: StateFlow<Float> = _speedMph.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _locationName = MutableStateFlow("...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()


    @SuppressLint("MissingPermission")
    fun startTracking() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateDistanceMeters(0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    _isLocked.value = true
                    if (location.hasSpeed()) {
                        // Location speed is in m/s, convert to mph (1 m/s = 2.23694 mph)
                        _speedMph.value = (location.speed * 2.23694f)
                    }
                    
                    // Simple background geocoding
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                _locationName.value = address.locality ?: address.subAdminArea ?: "UNKNOWN"
                            }
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        _isLocked.value = false
        _speedMph.value = 0f
        _locationName.value = "..."
    }
}

