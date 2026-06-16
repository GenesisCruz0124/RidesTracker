package com.ridestracker.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class CrashDetectionService @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _crashDetected = MutableStateFlow(false)
    val crashDetected: StateFlow<Boolean> = _crashDetected.asStateFlow()

    private var isActive = false

    companion object {
        private const val CRASH_THRESHOLD_G = 4.0f
        private const val G_FORCE = 9.81f
    }

    fun start() {
        if (!isActive) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            isActive = true
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        isActive = false
        _crashDetected.value = false
    }

    fun acknowledgeCrash() {
        _crashDetected.value = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z) / G_FORCE
        if (magnitude > CRASH_THRESHOLD_G && !_crashDetected.value) {
            _crashDetected.value = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
