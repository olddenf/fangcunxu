package com.example.fangcunxu.ai

import android.graphics.RectF

/**
 * 构图分析结果
 */
class CompositionResult(
    val overallScore: Float, // 总分（0-10）
    val dimensionScores: DimensionScores, // 各维度得分
    val sceneType: String, // 场景类型
    val detectedObjects: List<DetectionResult>, // 检测到的物体
    val recommendations: List<String>, // 改进建议
    val processingTimeMs: Long // 处理时间
)

/**
 * 各维度得分
 */
class DimensionScores(
    val balance: Float, // 平衡性（0-10）
    val ruleOfThirds: Float, // 三分法（0-10）
    val symmetry: Float, // 对称性（0-10）
    val leadingLines: Float, // 引导线（0-10）
    val depth: Float, // 层次感（0-10）
    val framing: Float, // 框架与边框（0-10）
    val negativeSpace: Float, // 负空间（0-10）
    val tone: Float, // 影调（0-10）
    val colorHarmony: Float, // 色彩和谐（0-10）
    val saturation: Float, // 饱和度（0-10）
    val sharpness: Float, // 清晰度（0-10）
    val noise: Float // 噪点（0-10）
)

/**
 * 场景类型
 */
enum class SceneCategory(val displayName: String) {
    PORTRAIT("人像"),
    LANDSCAPE("风景"),
    ARCHITECTURE("建筑"),
    MACRO("微距"),
    STREET("街拍"),
    OTHER("其他")
}

/**
 * 构图建议
 */
class CompositionSuggestion(
    val type: String, // 建议类型
    val title: String, // 建议标题
    val description: String, // 建议描述
    val priority: Int // 优先级（1-5，1最高）
)
