package com.example.fangcunxu.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SensorData(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val azimuth: Float = 0f,
    val lightLevel: Float = 0f,
    val isLevel: Boolean = false,
    val isDark: Boolean = false
)

class SensorManager(private val context: Context) {
    
    private val sensorManager: android.hardware.SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData
    
    private var accelerometerData = FloatArray(3)
    private var magnetometerData = FloatArray(3)
    
    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                accelerometerData = it.values.clone()
                updateOrientation()
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    private val magnetometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                magnetometerData = it.values.clone()
                updateOrientation()
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val lightLevel = it.values[0]
                val isDark = lightLevel < 10f
                _sensorData.value = _sensorData.value.copy(
                    lightLevel = lightLevel,
                    isDark = isDark
                )
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    private fun updateOrientation() {
        val rotationMatrix = FloatArray(9)
        val orientationValues = FloatArray(3)
        
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerData,
            magnetometerData
        )
        
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            
            val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
            val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            
            val isLevel = Math.abs(pitch) < 3f && Math.abs(roll) < 3f
            
            _sensorData.value = _sensorData.value.copy(
                pitch = pitch,
                roll = roll,
                azimuth = azimuth,
                isLevel = isLevel
            )
        }
    }
    
    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        magnetometer?.let {
            sensorManager.registerListener(
                magnetometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        lightSensor?.let {
            sensorManager.registerListener(
                lightSensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(accelerometerListener)
        sensorManager.unregisterListener(magnetometerListener)
        sensorManager.unregisterListener(lightSensorListener)
    }
    
    fun getTiltSuggestion(): String {
        val data = _sensorData.value
        
        return when {
            !data.isLevel && Math.abs(data.pitch) > 10f -> {
                "相机倾斜 ${Math.abs(data.pitch).toInt()}°，请保持水平"
            }
            !data.isLevel && Math.abs(data.roll) > 10f -> {
                "相机倾斜 ${Math.abs(data.roll).toInt()}°，请保持水平"
            }
            else -> "相机保持水平"
        }
    }
    
    fun getLightSuggestion(): String {
        val data = _sensorData.value
        
        return when {
            data.isDark -> "光线较暗，建议使用大光圈或提高 ISO"
            data.lightLevel > 1000f -> "光线充足，可以使用小光圈获得更大景深"
            else -> "光线适中"
        }
    }
}