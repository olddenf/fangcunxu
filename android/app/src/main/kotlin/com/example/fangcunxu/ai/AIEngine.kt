package com.example.fangcunxu.ai

import android.util.Log
import android.graphics.Bitmap
import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import com.example.fangcunxu.ai.SceneRecognitionModel
import com.example.fangcunxu.ai.SceneRecognitionResult
import com.example.fangcunxu.ai.AIModel
import com.example.fangcunxu.ai.DetectionResult
import com.example.fangcunxu.ai.CompositionAnalyzer
import com.example.fangcunxu.ai.CompositionResult

class AIEngine(private val context: Context) {
    
    private val models = ConcurrentHashMap<String, Any>()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    var isLoading = false
        private set
    
    var isReady = false
        private set
    
    suspend fun loadAllModels(): Boolean = withContext(Dispatchers.IO) {
        isLoading = true
        try {
            Log.d("AIEngine", "开始加载AI模型")
            
            // 加载场景识别模型
            val sceneModel = SceneRecognitionModel(context)
            Log.d("AIEngine", "加载场景识别模型")
            sceneModel.loadModel()
            Log.d("AIEngine", "场景识别模型加载完成")
            
            // 加载目标检测模型
            val objectDetectionModel = ObjectDetectionModel(context)
            Log.d("AIEngine", "加载目标检测模型")
            objectDetectionModel.loadModel()
            Log.d("AIEngine", "目标检测模型加载完成")
            
            // 初始化构图分析器
            val compositionAnalyzer = CompositionAnalyzer(context)
            Log.d("AIEngine", "构图分析器初始化完成")
            
            // 检查模型是否加载成功
            val sceneLoaded = sceneModel.isLoaded
            val detectionLoaded = objectDetectionModel.isLoaded
            
            if (sceneLoaded && detectionLoaded) {
                models["scene"] = sceneModel
                models["object_detection"] = objectDetectionModel
                models["composition"] = compositionAnalyzer
                isReady = true
                Log.d("AIEngine", "所有模型加载成功")
                true
            } else {
                isReady = false
                if (!sceneLoaded) Log.e("AIEngine", "场景识别模型加载失败")
                if (!detectionLoaded) Log.e("AIEngine", "目标检测模型加载失败")
                false
            }
        } catch (e: Exception) {
            Log.e("AIEngine", "模型加载失败", e)
            e.printStackTrace()
            isReady = false
            false
        } finally {
            isLoading = false
            Log.d("AIEngine", "模型加载过程完成")
        }
    }
    
    fun recognizeScene(bitmap: Bitmap): SceneRecognitionResult {
        val model = models["scene"] as? SceneRecognitionModel
        return if (model != null) {
            model.infer(bitmap)
        } else {
            // 模型未加载时返回模拟结果
            getMockSceneRecognitionResult()
        }
    }
    
    suspend fun recognizeSceneAsync(bitmap: Bitmap): SceneRecognitionResult = withContext(Dispatchers.IO) {
        recognizeScene(bitmap)
    }
    
    /**
     * 目标检测
     */
    fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        val model = models["object_detection"] as? ObjectDetectionModel
        return if (model != null) {
            model.infer(bitmap)
        } else {
            emptyList()
        }
    }
    
    suspend fun detectObjectsAsync(bitmap: Bitmap): List<DetectionResult> = withContext(Dispatchers.IO) {
        detectObjects(bitmap)
    }
    
    /**
     * 分析构图
     */
    fun analyzeComposition(bitmap: Bitmap, sceneType: String, objects: List<DetectionResult>): CompositionResult {
        val analyzer = models["composition"] as? CompositionAnalyzer
        return if (analyzer != null) {
            analyzer.analyzeComposition(bitmap, sceneType, objects)
        } else {
            // 返回默认结果
            val defaultScores = DimensionScores(
                balance = 6.0f,
                ruleOfThirds = 6.0f,
                symmetry = 6.0f,
                leadingLines = 6.0f,
                depth = 6.0f,
                framing = 6.0f,
                negativeSpace = 6.0f,
                tone = 6.0f,
                colorHarmony = 6.0f,
                saturation = 6.0f,
                sharpness = 6.0f,
                noise = 6.0f
            )
            CompositionResult(
                overallScore = 6.0f,
                dimensionScores = defaultScores,
                sceneType = sceneType,
                detectedObjects = objects,
                recommendations = listOf("尝试将主体放置在三分法则的交点上"),
                processingTimeMs = 0
            )
        }
    }
    
    suspend fun analyzeCompositionAsync(bitmap: Bitmap, sceneType: String, objects: List<DetectionResult>): CompositionResult = withContext(Dispatchers.IO) {
        analyzeComposition(bitmap, sceneType, objects)
    }
    
    /**
     * 获取目标检测模型（用于获取颜色映射等）
     */
    fun getObjectDetectionModel(): ObjectDetectionModel? {
        return models["object_detection"] as? ObjectDetectionModel
    }
    
    private fun getMockSceneRecognitionResult(): SceneRecognitionResult {
        // 返回UNKNOWN而不是随机结果
        val confidence = 0.0f
        
        // 生成模拟的概率分布
        val sceneTypes = listOf(
            SceneType.PORTRAIT,
            SceneType.LANDSCAPE,
            SceneType.CITY,
            SceneType.NIGHT,
            SceneType.STREET,
            SceneType.FOOD,
            SceneType.ARCHITECTURE,
            SceneType.STILL_LIFE,
            SceneType.SPORTS,
            SceneType.MACRO
        )
        
        val allProbs = mutableMapOf<SceneType, Float>()
        sceneTypes.forEach {
            allProbs[it] = 1.0f / sceneTypes.size
        }
        
        return SceneRecognitionResult(
            sceneType = SceneType.UNKNOWN,
            confidence = confidence,
            allProbabilities = allProbs,
            isReliable = false
        )
    }
    
    fun release() {
        scope.cancel()
        models.values.forEach { model ->
            when (model) {
                is AIModel<*, *> -> model.close()
            }
        }
        models.clear()
        isReady = false
    }
}