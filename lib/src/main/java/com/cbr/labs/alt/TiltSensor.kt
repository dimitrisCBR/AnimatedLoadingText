package com.cbr.labs.alt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


interface TiltListener {

    fun onTilt(pitchRollRad: Pair<Double, Double>)
}

interface TiltSensor {

    fun addListener(tiltListener: TiltListener)

    fun register()

    fun unregister()
}

class TiltSensorImpl(context: Context) : SensorEventListener, TiltSensor {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationSensor: Sensor
    private var listeners = mutableListOf<TiltListener>()

    private val orientationAngles = FloatArray(3)

    init {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing here
    }

    override fun onSensorChanged(event: SensorEvent) {

        val rotationMatrix = FloatArray(9)

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val pitchInRad = if(!orientationAngles[1].isNaN()) orientationAngles[1].toDouble() else 0.0
        val rollInRad = if(!orientationAngles[2].isNaN()) orientationAngles[2].toDouble() else 0.0

        val pair = Pair(pitchInRad, rollInRad)
        listeners.forEach {
            it.onTilt(pair)
        }
    }

    override fun addListener(tiltListener: TiltListener) {
        listeners.add(tiltListener)
    }

    override fun register() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun unregister() {
        listeners.clear()
        sensorManager.unregisterListener(this, rotationSensor)
    }
}