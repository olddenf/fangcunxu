package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import java.nio.ByteBuffer
import kotlin.math.exp

/**
 * 场景识别结果
 */
data class SceneRecognitionResult(
    val sceneType: SceneType,
    val confidence: Float,
    val allProbabilities: Map<SceneType, Float>
)

/**
 * 场景类型枚举
 */
enum class SceneType {
    PORTRAIT,      // 人像
    LANDSCAPE,     // 风景
    CITY,          // 城市
    NIGHT,         // 夜景
    STREET,        // 街拍
    FOOD,          // 美食
    ARCHITECTURE,  // 建筑
    STILL_LIFE,    // 静物
    SPORTS,        // 运动
    MACRO          // 微距
}

/**
 * 场景识别模型 (MobileNet V3 Small)
 * 输入：224x224 RGB 图像
 * 输出：10 个场景类型的概率分布
 */
class SceneRecognitionModel(context: Context) : 
    AIModel<Bitmap, SceneRecognitionResult>(context, "models/mobilenet_v3_small.tflite") {
    
    companion object {
        private const val INPUT_SIZE = 224
        private val LABELS = listOf(
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
    }
    
    /**
     * 执行场景识别推理
     */
    override fun infer(input: Bitmap): SceneRecognitionResult {
        if (!isLoaded || interpreter == null) {
            throw IllegalStateException("Model not loaded")
        }
        
        // 预处理图像
        val inputBuffer = preprocessor.preprocessImage(input, INPUT_SIZE, INPUT_SIZE, normalize = true)
        
        // 准备输出缓冲区 (10 个类别的概率)
        val outputBuffer = Array(1) { FloatArray(10) }
        
        // 执行推理
        interpreter?.run(inputBuffer, outputBuffer[0])
        
        // Softmax 归一化
        val probabilities = softmax(outputBuffer[0])
        
        // 找到最大概率的类别
        var maxIndex = 0
        var maxProb = probabilities[0]
        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProb) {
                maxIndex = i
                maxProb = probabilities[i]
            }
        }
        
        // 构建结果
        val allProbs = mutableMapOf<SceneType, Float>()
        LABELS.forEachIndexed { index, sceneType ->
            allProbs[sceneType] = probabilities[index]
        }
        
        return SceneRecognitionResult(
            sceneType = LABELS[maxIndex],
            confidence = maxProb,
            allProbabilities = allProbs
        )
    }
    
    /**
     * Softmax 函数
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expValues = logits.map { exp(it - maxLogit) }
        val sumExp = expValues.sum()
        return expValues.map { it / sumExp }.toFloatArray()
    }
}
