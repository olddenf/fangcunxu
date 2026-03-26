package com.example.fangcunxu.camera

import android.content.Context
import android.content.SharedPreferences

/**
 * 相机配置管理器
 * 管理相机和镜头的配置参数
 */
class CameraConfigManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("camera_config", Context.MODE_PRIVATE)
    
    // 相机参数
    data class CameraSettings(
        val cameraModel: String = "",
        val sensorSize: String = "",
        val maxAperture: Double = 0.0,
        val maxISO: Int = 0,
        val maxShutterSpeed: Double = 0.0
    )
    
    // 镜头参数
    data class LensSettings(
        val lensModel: String = "",
        val focalLengthMin: Double = 0.0,
        val focalLengthMax: Double = 0.0,
        val apertureMin: Double = 0.0,
        val apertureMax: Double = 0.0
    )
    
    // 当前配置
    data class CurrentConfig(
        val cameraSettings: CameraSettings,
        val lensSettings: LensSettings,
        val currentFocalLength: Double = 0.0,
        val currentAperture: Double = 0.0,
        val currentShutterSpeed: Double = 0.0,
        val currentISO: Int = 0
    )
    
    // 相机预设列表
    private val cameraPresets = mapOf(
        "Canon EOS R5" to CameraSettings(
            cameraModel = "Canon EOS R5",
            sensorSize = "Full Frame",
            maxAperture = 1.2,
            maxISO = 102400,
            maxShutterSpeed = 1.0/8000.0
        ),
        "Sony A7 IV" to CameraSettings(
            cameraModel = "Sony A7 IV",
            sensorSize = "Full Frame",
            maxAperture = 1.2,
            maxISO = 51200,
            maxShutterSpeed = 1.0/8000.0
        ),
        "Nikon Z7 II" to CameraSettings(
            cameraModel = "Nikon Z7 II",
            sensorSize = "Full Frame",
            maxAperture = 1.2,
            maxISO = 51200,
            maxShutterSpeed = 1.0/8000.0
        )
    )
    
    // 镜头预设列表
    private val lensPresets = mapOf(
        "Canon RF 50mm f/1.2L USM" to LensSettings(
            lensModel = "Canon RF 50mm f/1.2L USM",
            focalLengthMin = 50.0,
            focalLengthMax = 50.0,
            apertureMin = 1.2,
            apertureMax = 16.0
        ),
        "Sony FE 24-70mm f/2.8 GM" to LensSettings(
            lensModel = "Sony FE 24-70mm f/2.8 GM",
            focalLengthMin = 24.0,
            focalLengthMax = 70.0,
            apertureMin = 2.8,
            apertureMax = 22.0
        ),
        "Nikon NIKKOR Z 85mm f/1.4 S" to LensSettings(
            lensModel = "Nikon NIKKOR Z 85mm f/1.4 S",
            focalLengthMin = 85.0,
            focalLengthMax = 85.0,
            apertureMin = 1.4,
            apertureMax = 16.0
        )
    )
    
    // 保存相机配置
    fun saveCameraSettings(cameraSettings: CameraSettings) {
        with(preferences.edit()) {
            putString("camera_model", cameraSettings.cameraModel)
            putString("sensor_size", cameraSettings.sensorSize)
            putFloat("max_aperture", cameraSettings.maxAperture.toFloat())
            putInt("max_iso", cameraSettings.maxISO)
            putFloat("max_shutter_speed", cameraSettings.maxShutterSpeed.toFloat())
            apply()
        }
    }
    
    // 保存镜头配置
    fun saveLensSettings(lensSettings: LensSettings) {
        with(preferences.edit()) {
            putString("lens_model", lensSettings.lensModel)
            putFloat("focal_length_min", lensSettings.focalLengthMin.toFloat())
            putFloat("focal_length_max", lensSettings.focalLengthMax.toFloat())
            putFloat("aperture_min", lensSettings.apertureMin.toFloat())
            putFloat("aperture_max", lensSettings.apertureMax.toFloat())
            apply()
        }
    }
    
    // 保存当前拍摄参数
    fun saveCurrentSettings(focalLength: Double, aperture: Double, shutterSpeed: Double, iso: Int) {
        with(preferences.edit()) {
            putFloat("current_focal_length", focalLength.toFloat())
            putFloat("current_aperture", aperture.toFloat())
            putFloat("current_shutter_speed", shutterSpeed.toFloat())
            putInt("current_iso", iso)
            apply()
        }
    }
    
    // 获取当前配置
    fun getCurrentConfig(): CurrentConfig {
        val cameraSettings = CameraSettings(
            cameraModel = preferences.getString("camera_model", "") ?: "",
            sensorSize = preferences.getString("sensor_size", "") ?: "",
            maxAperture = preferences.getFloat("max_aperture", 0f).toDouble(),
            maxISO = preferences.getInt("max_iso", 0),
            maxShutterSpeed = preferences.getFloat("max_shutter_speed", 0f).toDouble()
        )
        
        val lensSettings = LensSettings(
            lensModel = preferences.getString("lens_model", "") ?: "",
            focalLengthMin = preferences.getFloat("focal_length_min", 0f).toDouble(),
            focalLengthMax = preferences.getFloat("focal_length_max", 0f).toDouble(),
            apertureMin = preferences.getFloat("aperture_min", 0f).toDouble(),
            apertureMax = preferences.getFloat("aperture_max", 0f).toDouble()
        )
        
        return CurrentConfig(
            cameraSettings = cameraSettings,
            lensSettings = lensSettings,
            currentFocalLength = preferences.getFloat("current_focal_length", 0f).toDouble(),
            currentAperture = preferences.getFloat("current_aperture", 0f).toDouble(),
            currentShutterSpeed = preferences.getFloat("current_shutter_speed", 0f).toDouble(),
            currentISO = preferences.getInt("current_iso", 0)
        )
    }
    
    // 获取相机预设列表
    fun getCameraPresets(): List<CameraSettings> {
        return cameraPresets.values.toList()
    }
    
    // 获取镜头预设列表
    fun getLensPresets(): List<LensSettings> {
        return lensPresets.values.toList()
    }
    
    // 重置配置
    fun resetConfig() {
        preferences.edit().clear().apply()
    }
    
    // 检查配置是否完整
    fun isConfigComplete(): Boolean {
        val config = getCurrentConfig()
        return config.cameraSettings.cameraModel.isNotEmpty() && 
               config.lensSettings.lensModel.isNotEmpty()
    }
    
    // 根据焦距获取推荐的构图方式
    fun getRecommendedComposition(focalLength: Double): String {
        return when {
            focalLength < 35 -> "广角镜头：适合风景、建筑，注意边缘畸变"
            focalLength in 35.0..70.0 -> "标准镜头：适合人像、街拍，接近人眼视角"
            focalLength in 85.0..135.0 -> "中长焦镜头：适合人像特写，背景虚化效果好"
            else -> "长焦镜头：适合远距离拍摄，压缩空间感"
        }
    }
    
    // 根据光圈值获取景深建议
    fun getDepthOfFieldSuggestion(aperture: Double): String {
        return when {
            aperture < 2.0 -> "大光圈：适合人像、微距，背景虚化效果好"
            aperture in 2.0..4.0 -> "中等光圈：适合大多数场景，平衡清晰度和虚化"
            else -> "小光圈：适合风景、建筑，景深大，前后都清晰"
        }
    }
}