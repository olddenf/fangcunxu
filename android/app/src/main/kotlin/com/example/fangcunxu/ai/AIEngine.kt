package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * AI 引擎管理器
 * 统一管理所有 AI 模型的加载和推理
 */
class AIEngine(private val context: Context, private val useMockData: Boolean = true) {
    
    // 模型实例缓存
    private val models = ConcurrentHashMap<String, Any>()
    
    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 加载状态
    var isLoading = false
        private set
    
    var isReady = false
        private set
    
    /**
     * 异步加载所有模型
     */
    suspend fun loadAllModels(): Boolean = withContext(Dispatchers.IO) {
        isLoading = true
        try {
            if (useMockData) {
                // 使用模拟数据，不需要加载真实模型
                isReady = true
                true
            } else {
                // 加载 NIMA 模型
                models["nima"] = NIMAModel(context)
                
                // 加载场景识别模型
                models["scene"] = SceneRecognitionModel(context)
                
                // 加载目标检测模型
                models["detection"] = ObjectDetectionModel(context)
                
                isReady = true
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加载失败，自动切换到模拟数据模式
            isReady = useMockData
            useMockData
        } finally {
            isLoading = false
        }
    }
    
    /**
     * 获取 NIMA 评分
     * @param bitmap 输入图像
     * @return NIMA 评分结果
     */
    fun getNIMAScore(bitmap: Bitmap): NIMAResult {
        if (useMockData) {
            return getMockNIMAResult()
        }
        val model = models["nima"] as? NIMAModel
            ?: throw IllegalStateException("NIMA model not loaded")
        return model.infer(bitmap)
    }
    
    /**
     * 获取模拟的 NIMA 评分结果
     */
    private fun getMockNIMAResult(): NIMAResult {
        return NIMAResult(
            score = 7.5f,
            normalizedScore = 72,
            rating = Rating.GOOD,
            distribution = List(10) { i -> if (i == 6) 0.5f else 0.05f }
        )
    }
    
    /**
     * 获取场景识别结果
     * @param bitmap 输入图像
     * @return 场景识别结果
     */
    fun recognizeScene(bitmap: Bitmap): SceneRecognitionResult {
        if (useMockData) {
            return getMockSceneRecognitionResult()
        }
        val model = models["scene"] as? SceneRecognitionModel
            ?: throw IllegalStateException("Scene model not loaded")
        return model.infer(bitmap)
    }
    
    /**
     * 获取模拟的场景识别结果
     */
    private fun getMockSceneRecognitionResult(): SceneRecognitionResult {
        val sceneTypes = listOf(
            SceneType.PORTRAIT, SceneType.LANDSCAPE, SceneType.CITY,
            SceneType.NIGHT, SceneType.STREET, SceneType.FOOD,
            SceneType.ARCHITECTURE, SceneType.STILL_LIFE, SceneType.SPORTS, SceneType.MACRO
        )
        val randomScene = sceneTypes.random()
        val allProbs = mutableMapOf<SceneType, Float>()
        sceneTypes.forEachIndexed { index, sceneType ->
            allProbs[sceneType] = if (sceneType == randomScene) 0.8f else 0.2f / 9f
        }
        return SceneRecognitionResult(
            sceneType = randomScene,
            confidence = 0.8f,
            allProbabilities = allProbs
        )
    }
    
    /**
     * 获取目标检测结果
     * @param bitmap 输入图像
     * @return 检测结果列表
     */
    fun detectObjects(bitmap: Bitmap): List<DetectionResult> {
        if (useMockData) {
            return getMockDetectionResults()
        }
        val model = models["detection"] as? ObjectDetectionModel
            ?: throw IllegalStateException("Detection model not loaded")
        return model.infer(bitmap)
    }
    
    /**
     * 获取模拟的目标检测结果
     */
    private fun getMockDetectionResults(): List<DetectionResult> {
        val commonObjects = listOf("person", "car", "bicycle", "dog", "cat", "chair", "table")
        val results = mutableListOf<DetectionResult>()
        
        // 生成 1-3 个随机检测结果
        val objectCount = (1..3).random()
        for (i in 0 until objectCount) {
            val className = commonObjects.random()
            val confidence = (0.7f..0.95f).random()
            val x1 = (0.1f..0.3f).random()
            val y1 = (0.1f..0.3f).random()
            val x2 = (0.7f..0.9f).random()
            val y2 = (0.7f..0.9f).random()
            
            results.add(DetectionResult(
                className = className,
                confidence = confidence,
                x1 = x1,
                y1 = y1,
                x2 = x2,
                y2 = y2
            ))
        }
        
        return results
    }
    
    /**
     * 异步执行 NIMA 评分
     */
    suspend fun getNIMAScoreAsync(bitmap: Bitmap): NIMAResult = withContext(Dispatchers.IO) {
        getNIMAScore(bitmap)
    }
    
    /**
     * 异步执行场景识别
     */
    suspend fun recognizeSceneAsync(bitmap: Bitmap): SceneRecognitionResult = withContext(Dispatchers.IO) {
        recognizeScene(bitmap)
    }
    
    /**
     * 异步执行目标检测
     */
    suspend fun detectObjectsAsync(bitmap: Bitmap): List<DetectionResult> = withContext(Dispatchers.IO) {
        detectObjects(bitmap)
    }
    
    /**
     * 释放所有资源
     */
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
