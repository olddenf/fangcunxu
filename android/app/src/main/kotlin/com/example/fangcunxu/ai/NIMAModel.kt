package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * NIMA 构图评分结果
 */
data class NIMAResult(
    val score: Float,        // 原始评分 (1-10)
    val normalizedScore: Int, // 归一化评分 (0-100)
    val rating: Rating,      // 等级
    val distribution: List<Float> // 评分分布
)

/**
 * 评分等级
 */
enum class Rating {
    EXCELLENT,  // 优秀 (90+)
    GOOD,       // 良好 (70-89)
    NEEDS_IMPROVEMENT  // 需改进 (<70)
}

/**
 * NIMA 构图评分模型
 * 输入：224x224 RGB 图像
 * 输出：10 维评分分布 (对应 1-10 分的概率)
 */
class NIMAModel(context: Context) : 
    AIModel<Bitmap, NIMAResult>(context, "models/nima_mobilenet.tflite") {
    
    companion object {
        private const val INPUT_SIZE = 224
        private const val NUM_BUCKETS = 10 // 10 个评分等级
    }
    
    /**
     * 执行 NIMA 构图评分推理
     */
    override fun infer(input: Bitmap): NIMAResult {
        if (!isLoaded || interpreter == null) {
            throw IllegalStateException("Model not loaded")
        }
        
        // 预处理图像
        val inputBuffer = preprocessor.preprocessImage(input, INPUT_SIZE, INPUT_SIZE, normalize = true)
        
        // 准备输出缓冲区 (10 个评分桶的概率分布)
        val outputBuffer = Array(1) { FloatArray(NUM_BUCKETS) }
        
        // 执行推理
        interpreter?.run(inputBuffer, outputBuffer[0])
        
        val distribution = outputBuffer[0].toList()
        
        // 计算平均评分：sum((i+1) * prob for i, prob in enumerate(distribution))
        var meanScore = 0f
        distribution.forEachIndexed { index, probability ->
            meanScore += (index + 1) * probability
        }
        
        // 归一化到 0-100 分
        // 公式：(score - 1) * (100 / 9)
        val normalizedScore = ((meanScore - 1) * (100f / 9f)).toInt().coerceIn(0, 100)
        
        // 确定等级
        val rating = when {
            normalizedScore >= 90 -> Rating.EXCELLENT
            normalizedScore >= 70 -> Rating.GOOD
            else -> Rating.NEEDS_IMPROVEMENT
        }
        
        return NIMAResult(
            score = meanScore,
            normalizedScore = normalizedScore,
            rating = rating,
            distribution = distribution
        )
    }
}
