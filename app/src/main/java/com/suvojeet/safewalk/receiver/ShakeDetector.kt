package com.suvojeet.safewalk.receiver

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.suvojeet.safewalk.util.Constants
import kotlin.math.sqrt

class ShakeDetector(
    private val onShakeDetected: () -> Unit,
) : SensorEventListener {

    private var shakeTimestamp: Long = 0
    private var shakeCount: Int = 0
    private var sensitivity: Float = Constants.SHAKE_THRESHOLD_DEFAULT

    fun setSensitivity(value: Float) {
        sensitivity = value
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > sensitivity / SensorManager.GRAVITY_EARTH) {
            val now = System.currentTimeMillis()

            if (shakeTimestamp + Constants.SHAKE_SLOP_TIME_MS > now) {
                return
            }

            if (shakeTimestamp + Constants.SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0
            }

            shakeTimestamp = now
            shakeCount++

            if (shakeCount >= Constants.SHAKE_COUNT_TRIGGER) {
                shakeCount = 0
                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
