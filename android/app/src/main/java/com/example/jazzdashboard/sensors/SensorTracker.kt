package com.example.jazzdashboard.sensors
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

class SensorTracker(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _gForceX = MutableStateFlow(0f)
    val gForceX: StateFlow<Float> = _gForceX.asStateFlow()

    private val _gForceY = MutableStateFlow(0f)
    val gForceY: StateFlow<Float> = _gForceY.asStateFlow()

    fun startTracking() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        _gForceX.value = 0f
        _gForceY.value = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Standard gravity is ~9.81 m/s^2.
            // X axis: sideways force. Y axis: longitudinal force.
            // Note: device orientation matters here. We assume landscape.
            val gX = event.values[1] / SensorManager.GRAVITY_EARTH // Using Y for X if landscape
            val gY = event.values[0] / SensorManager.GRAVITY_EARTH // Using X for Y if landscape

            // Smooth/Clamp values for UI
            _gForceX.value = max(-2f, min(2f, gX))
            _gForceY.value = max(-2f, min(2f, gY))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

