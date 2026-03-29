package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.sqrt

class CompositionAnalyzer(private val context: Context) {
    
    // 维度权重
    private val dimensionWeights = mapOf(
        "balance" to 0.12f,
        "ruleOfThirds" to 0.15f,
        "symmetry" to 0.08f,
        "leadingLines" to 0.07f,
        "depth" to 0.10f,
        "framing" to 0.05f,
        "negativeSpace" to 0.08f,
        "tone" to 0.10f,
        "colorHarmony" to 0.08f,
        "saturation" to 0.05f,
        "sharpness" to 0.06f,
        "noise" to 0.06f
    )
    
    // 场景特定权重调整
    private val sceneWeightAdjustments = mapOf(
        "人像" to mapOf(
            "ruleOfThirds" to 0.20f,
            "negativeSpace" to 0.12f
        ),
        "风景" to mapOf(
            "leadingLines" to 0.12f,
            "depth" to 0.15f
        ),
        "建筑" to mapOf(
            "symmetry" to 0.15f,
            "framing" to 0.10f
        ),
        "微距" to mapOf(
            "negativeSpace" to 0.15f,
            "sharpness" to 0.12f
        ),
        "街拍" to mapOf(
            "balance" to 0.08f,
            "leadingLines" to 0.15f
        )
    )
    
    /**
     * 分析构图
     */
    fun analyzeComposition(bitmap: Bitmap, sceneType: String, objects: List<DetectionResult>): CompositionResult {
        val startTime = System.currentTimeMillis()
        
        // 计算各维度得分
        val dimensionScores = calculateDimensionScores(bitmap, sceneType, objects)
        
        // 计算总分
        val overallScore = calculateOverallScore(dimensionScores, sceneType, objects, bitmap)
        
        // 生成建议
        val recommendations = generateRecommendations(dimensionScores, sceneType, objects)
        
        val processingTimeMs = System.currentTimeMillis() - startTime
        
        return CompositionResult(
            overallScore = overallScore,
            dimensionScores = dimensionScores,
            sceneType = sceneType,
            detectedObjects = objects,
            recommendations = recommendations,
            processingTimeMs = processingTimeMs
        )
    }
    
    /**
     * 计算各维度得分
     */
    private fun calculateDimensionScores(bitmap: Bitmap, sceneType: String, objects: List<DetectionResult>): DimensionScores {
        return DimensionScores(
            balance = calculateBalance(objects, bitmap.width, bitmap.height),
            ruleOfThirds = calculateRuleOfThirds(objects, bitmap.width, bitmap.height),
            symmetry = calculateSymmetry(bitmap),
            leadingLines = calculateLeadingLines(objects, bitmap.width, bitmap.height),
            depth = calculateDepth(objects),
            framing = calculateFraming(objects, bitmap.width, bitmap.height),
            negativeSpace = calculateNegativeSpace(objects, bitmap.width, bitmap.height),
            tone = calculateTone(bitmap),
            colorHarmony = calculateColorHarmony(bitmap),
            saturation = calculateSaturation(bitmap),
            sharpness = calculateSharpness(bitmap),
            noise = calculateNoise(bitmap)
        )
    }
    
    /**
     * 计算平衡性得分
     */
    private fun calculateBalance(objects: List<DetectionResult>, width: Int, height: Int): Float {
        if (objects.isEmpty()) return 7.0f
        
        // 将图像分为3×3网格
        val gridWidth = width / 3.0f
        val gridHeight = height / 3.0f
        
        // 计算每个网格的视觉重量
        val gridWeights = Array(3) { FloatArray(3) { 0.0f } }
        
        objects.forEach { obj ->
            val centerX = obj.boundingBox.centerX()
            val centerY = obj.boundingBox.centerY()
            val weight = obj.confidence
            
            val gridX = (centerX / gridWidth).toInt().coerceIn(0, 2)
            val gridY = (centerY / gridHeight).toInt().coerceIn(0, 2)
            
            gridWeights[gridY][gridX] += weight
        }
        
        // 计算左右、上下重量差
        val leftWeight = gridWeights[0][0] + gridWeights[1][0] + gridWeights[2][0]
        val rightWeight = gridWeights[0][2] + gridWeights[1][2] + gridWeights[2][2]
        val topWeight = gridWeights[0][0] + gridWeights[0][1] + gridWeights[0][2]
        val bottomWeight = gridWeights[2][0] + gridWeights[2][1] + gridWeights[2][2]
        
        val totalWeight = leftWeight + rightWeight
        val maxHorizontalImbalance = if (totalWeight > 0) abs(leftWeight - rightWeight) / totalWeight else 0.0f
        
        val totalVerticalWeight = topWeight + bottomWeight
        val maxVerticalImbalance = if (totalVerticalWeight > 0) abs(topWeight - bottomWeight) / totalVerticalWeight else 0.0f
        
        val maxImbalance = maxOf(maxHorizontalImbalance, maxVerticalImbalance)
        return 10.0f * (1.0f - maxImbalance)
    }
    
    /**
     * 计算三分法得分
     */
    private fun calculateRuleOfThirds(objects: List<DetectionResult>, width: Int, height: Int): Float {
        if (objects.isEmpty()) return 6.0f
        
        // 找出置信度最高的主体
        val mainObject = objects.maxByOrNull { it.confidence } ?: return 6.0f
        val centerX = mainObject.boundingBox.centerX()
        val centerY = mainObject.boundingBox.centerY()
        
        // 黄金分割点
        val thirdWidth = width / 3.0f
        val thirdHeight = height / 3.0f
        val goldenPoints = listOf(
            Pair(thirdWidth, thirdHeight),
            Pair(2 * thirdWidth, thirdHeight),
            Pair(thirdWidth, 2 * thirdHeight),
            Pair(2 * thirdWidth, 2 * thirdHeight)
        )
        
        // 计算到最近黄金分割点的距离
        val imageDiagonal = sqrt((width * width + height * height).toDouble()).toFloat()
        var minDistance = Float.MAX_VALUE
        
        goldenPoints.forEach { (x, y) ->
            val distance = sqrt((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y))
            if (distance < minDistance) {
                minDistance = distance
            }
        }
        
        val normalizedDistance = minDistance / imageDiagonal
        return if (normalizedDistance > 0.5f) 0.0f else 10.0f * (1.0f - 2.0f * normalizedDistance)
    }
    
    /**
     * 计算对称性得分
     */
    private fun calculateSymmetry(bitmap: Bitmap): Float {
        // 简化实现：计算左右对称差异
        val width = bitmap.width
        val height = bitmap.height
        val centerX = width / 2
        
        var totalDifference = 0
        var pixelCount = 0
        
        for (y in 0 until height) {
            for (x in 0 until centerX) {
                val leftPixel = bitmap.getPixel(x, y)
                val rightPixel = bitmap.getPixel(width - x - 1, y)
                
                val leftR = (leftPixel shr 16) and 0xFF
                val leftG = (leftPixel shr 8) and 0xFF
                val leftB = leftPixel and 0xFF
                
                val rightR = (rightPixel shr 16) and 0xFF
                val rightG = (rightPixel shr 8) and 0xFF
                val rightB = rightPixel and 0xFF
                
                totalDifference += abs(leftR - rightR) + abs(leftG - rightG) + abs(leftB - rightB)
                pixelCount += 3
            }
        }
        
        if (pixelCount == 0) return 7.0f
        
        val maxDifference = pixelCount * 255
        val differenceRatio = totalDifference.toFloat() / maxDifference
        return 10.0f * (1.0f - differenceRatio)
    }
    
    /**
     * 计算引导线得分
     */
    private fun calculateLeadingLines(objects: List<DetectionResult>, width: Int, height: Int): Float {
        // 简化实现：检查是否有线性物体
        val hasLinearObjects = objects.any { obj ->
            val box = obj.boundingBox
            val aspectRatio = box.width() / box.height()
            aspectRatio > 3.0f || aspectRatio < 1.0f / 3.0f
        }
        
        // 检查是否有指向主体的线条
        if (objects.size < 2) return if (hasLinearObjects) 7.0f else 5.5f
        
        val mainObject = objects.maxByOrNull { it.confidence } ?: return 5.5f
        val mainCenterX = mainObject.boundingBox.centerX()
        val mainCenterY = mainObject.boundingBox.centerY()
        
        var leadingLinesCount = 0
        
        objects.forEach { obj ->
            if (obj != mainObject) {
                val objCenterX = obj.boundingBox.centerX()
                val objCenterY = obj.boundingBox.centerY()
                
                // 检查是否指向主体
                val dx = mainCenterX - objCenterX
                val dy = mainCenterY - objCenterY
                val length = sqrt(dx * dx + dy * dy)
                
                if (length > 0) {
                    // 简单判断：如果物体在主体的左上方、左下方、右上方或右下方
                    val angle = Math.atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI
                    if (angle in -45.0..45.0 || angle in 135.0..225.0) {
                        leadingLinesCount++
                    }
                }
            }
        }
        
        val leadingLinesRatio = leadingLinesCount.toFloat() / (objects.size - 1)
        return 5.0f + 5.0f * leadingLinesRatio
    }
    
    /**
     * 计算层次感得分
     */
    private fun calculateDepth(objects: List<DetectionResult>): Float {
        if (objects.size < 2) return 6.0f
        
        // 根据检测框大小判断物体远近
        val sizes = objects.map { it.boundingBox.width() * it.boundingBox.height() }
        val maxSize = sizes.maxOrNull() ?: 0.0f
        val minSize = sizes.minOrNull() ?: 0.0f
        
        if (maxSize == 0.0f) return 6.0f
        
        // 计算大小差异
        val sizeRatio = minSize / maxSize
        val sizeDifferenceScore = if (sizeRatio < 0.1f) 10.0f else 5.0f + 5.0f * (1.0f - sizeRatio)
        
        // 计算物体重叠关系
        var overlapCount = 0
        for (i in 0 until objects.size) {
            for (j in i + 1 until objects.size) {
                if (objects[i].boundingBox.intersects(
                    objects[j].boundingBox.left,
                    objects[j].boundingBox.top,
                    objects[j].boundingBox.right,
                    objects[j].boundingBox.bottom
                )) {
                    overlapCount++
                }
            }
        }
        
        val overlapScore = if (overlapCount > 0) 8.0f else 6.0f
        
        return (sizeDifferenceScore * 0.7f + overlapScore * 0.3f)
    }
    
    /**
     * 计算框架与边框得分
     */
    private fun calculateFraming(objects: List<DetectionResult>, width: Int, height: Int): Float {
        if (objects.isEmpty()) return 4.0f
        
        // 检测图像边缘区域的物体（外环20%区域）
        val edgeThreshold = 0.2f
        val edgeWidth = width * edgeThreshold
        val edgeHeight = height * edgeThreshold
        
        val edgeObjects = objects.filter { obj ->
            val box = obj.boundingBox
            box.left < edgeWidth || box.right > width - edgeWidth ||
            box.top < edgeHeight || box.bottom > height - edgeHeight
        }
        
        if (edgeObjects.isEmpty()) return 4.0f
        
        // 分析是否形成框架
        val hasLeftEdge = edgeObjects.any { it.boundingBox.left < edgeWidth }
        val hasRightEdge = edgeObjects.any { it.boundingBox.right > width - edgeWidth }
        val hasTopEdge = edgeObjects.any { it.boundingBox.top < edgeHeight }
        val hasBottomEdge = edgeObjects.any { it.boundingBox.bottom > height - edgeHeight }
        
        val frameCompleteness = (if (hasLeftEdge) 1 else 0) +
                               (if (hasRightEdge) 1 else 0) +
                               (if (hasTopEdge) 1 else 0) +
                               (if (hasBottomEdge) 1 else 0)
        
        return (frameCompleteness / 4.0f) * 10.0f
    }
    
    /**
     * 计算负空间得分
     */
    private fun calculateNegativeSpace(objects: List<DetectionResult>, width: Int, height: Int): Float {
        if (objects.isEmpty()) return 8.0f
        
        // 计算主体面积
        val mainObject = objects.maxByOrNull { it.confidence } ?: return 8.0f
        val mainArea = mainObject.boundingBox.width() * mainObject.boundingBox.height()
        val totalArea = width * height.toFloat()
        val mainRatio = mainArea / totalArea
        
        // 理想比例
        val idealRatio = when {
            objects.any { it.className == "person" } -> 0.4f // 人像
            objects.any { it.className == "car" || it.className == "truck" } -> 0.3f // 风景
            else -> 0.5f // 其他
        }
        
        val ratioDifference = abs(mainRatio - idealRatio) / idealRatio
        return 10.0f * (1.0f - ratioDifference)
    }
    
    /**
     * 计算影调得分
     */
    private fun calculateTone(bitmap: Bitmap): Float {
        // 简化实现：计算亮度分布
        val width = bitmap.width
        val height = bitmap.height
        var totalBrightness = 0
        var pixelCount = 0
        
        for (y in 0 until height step 10) {
            for (x in 0 until width step 10) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val brightness = (r + g + b) / 3
                totalBrightness += brightness
                pixelCount++
            }
        }
        
        if (pixelCount == 0) return 6.5f
        
        val avgBrightness = totalBrightness / pixelCount
        
        // 曝光合理度（理想亮度在100-150之间）
        val exposureScore = if (avgBrightness in 100..150) 5.0f else {
            val distance = minOf(abs(avgBrightness - 125), 125)
            5.0f * (1.0f - distance / 125.0f)
        }
        
        // 对比度得分（简化）
        val contrastScore = 5.0f // 暂定为固定值
        
        return exposureScore + contrastScore
    }
    
    /**
     * 计算色彩和谐得分
     */
    private fun calculateColorHarmony(bitmap: Bitmap): Float {
        // 简化实现：计算主色调
        val width = bitmap.width
        val height = bitmap.height
        val colorCounts = mutableMapOf<Int, Int>()
        
        for (y in 0 until height step 20) {
            for (x in 0 until width step 20) {
                val pixel = bitmap.getPixel(x, y)
                val color = Color.rgb(
                    (Color.red(pixel) / 32) * 32,
                    (Color.green(pixel) / 32) * 32,
                    (Color.blue(pixel) / 32) * 32
                )
                colorCounts[color] = colorCounts.getOrDefault(color, 0) + 1
            }
        }
        
        // 取前3种主色调
        val topColors = colorCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        
        if (topColors.size < 2) return 7.5f
        
        // 计算色调差异
        var totalDifference = 0
        for (i in 0 until topColors.size - 1) {
            for (j in i + 1 until topColors.size) {
                val color1 = topColors[i]
                val color2 = topColors[j]
                totalDifference += abs(Color.red(color1) - Color.red(color2))
                totalDifference += abs(Color.green(color1) - Color.green(color2))
                totalDifference += abs(Color.blue(color1) - Color.blue(color2))
            }
        }
        
        val maxDifference = topColors.size * (topColors.size - 1) / 2 * 3 * 255
        val differenceRatio = if (maxDifference > 0) totalDifference.toFloat() / maxDifference else 0.0f
        
        // 适中的色彩差异得分最高
        return if (differenceRatio in 0.3f..0.7f) 10.0f else 7.5f - 5.0f * abs(differenceRatio - 0.5f)
    }
    
    /**
     * 计算饱和度得分
     */
    private fun calculateSaturation(bitmap: Bitmap): Float {
        // 简化实现：计算色彩鲜艳程度
        val width = bitmap.width
        val height = bitmap.height
        var totalSaturation = 0
        var pixelCount = 0
        
        for (y in 0 until height step 10) {
            for (x in 0 until width step 10) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val max = maxOf(r, g, b)
                val min = minOf(r, g, b)
                val saturation = if (max > 0) (max - min) / max.toFloat() else 0.0f
                totalSaturation += (saturation * 255).toInt()
                pixelCount++
            }
        }
        
        if (pixelCount == 0) return 8.0f
        
        val avgSaturation = totalSaturation / pixelCount / 255.0f
        
        // 理想饱和度
        val idealSaturation = 0.6f
        val saturationDifference = abs(avgSaturation - idealSaturation) / idealSaturation
        
        return 10.0f * (1.0f - saturationDifference)
    }
    
    /**
     * 计算清晰度得分
     */
    private fun calculateSharpness(bitmap: Bitmap): Float {
        // 简化实现：计算边缘强度
        val width = bitmap.width
        val height = bitmap.height
        var totalEdgeStrength = 0
        var pixelCount = 0
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = bitmap.getPixel(x, y)
                val left = bitmap.getPixel(x - 1, y)
                val right = bitmap.getPixel(x + 1, y)
                val top = bitmap.getPixel(x, y - 1)
                val bottom = bitmap.getPixel(x, y + 1)
                
                val centerBrightness = (Color.red(center) + Color.green(center) + Color.blue(center)) / 3
                val leftBrightness = (Color.red(left) + Color.green(left) + Color.blue(left)) / 3
                val rightBrightness = (Color.red(right) + Color.green(right) + Color.blue(right)) / 3
                val topBrightness = (Color.red(top) + Color.green(top) + Color.blue(top)) / 3
                val bottomBrightness = (Color.red(bottom) + Color.green(bottom) + Color.blue(bottom)) / 3
                
                val edgeStrength = abs(centerBrightness - leftBrightness) +
                                  abs(centerBrightness - rightBrightness) +
                                  abs(centerBrightness - topBrightness) +
                                  abs(centerBrightness - bottomBrightness)
                
                totalEdgeStrength += edgeStrength
                pixelCount++
            }
        }
        
        if (pixelCount == 0) return 9.0f
        
        val avgEdgeStrength = totalEdgeStrength.toFloat() / pixelCount
        val normalizedSharpness = minOf(avgEdgeStrength / 255.0f, 1.0f)
        
        return 10.0f * normalizedSharpness
    }
    
    /**
     * 计算噪点得分
     */
    private fun calculateNoise(bitmap: Bitmap): Float {
        // 简化实现：计算平滑区域的局部方差
        val width = bitmap.width
        val height = bitmap.height
        var totalVariance = 0.0
        var regionCount = 0
        
        val blockSize = 20
        for (y in 0 until height step blockSize) {
            for (x in 0 until width step blockSize) {
                val blockWidth = minOf(blockSize, width - x)
                val blockHeight = minOf(blockSize, height - y)
                
                var sum = 0
                var sumSquared = 0
                var count = 0
                
                for (by in 0 until blockHeight) {
                    for (bx in 0 until blockWidth) {
                        val pixel = bitmap.getPixel(x + bx, y + by)
                        val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                        sum += brightness
                        sumSquared += brightness * brightness
                        count++
                    }
                }
                
                if (count > 0) {
                    val mean = sum.toDouble() / count
                    val variance = (sumSquared.toDouble() / count) - (mean * mean)
                    totalVariance += variance
                    regionCount++
                }
            }
        }
        
        if (regionCount == 0) return 8.5f
        
        val avgVariance = totalVariance / regionCount
        val noiseLevel = minOf(avgVariance / 1000.0, 1.0)
        
        return 10.0f * (1.0f - noiseLevel.toFloat())
    }
    
    /**
     * 计算总分
     */
    private fun calculateOverallScore(
        dimensionScores: DimensionScores,
        sceneType: String,
        objects: List<DetectionResult>,
        bitmap: Bitmap
    ): Float {
        // 获取场景特定权重调整
        val weightAdjustments = sceneWeightAdjustments.getOrDefault(sceneType, emptyMap())
        
        // 计算加权得分
        var weightedScore = 0.0f
        var totalWeight = 0.0f
        
        dimensionWeights.forEach { (dimension, weight) ->
            val adjustedWeight = weightAdjustments.getOrDefault(dimension, weight)
            val score = when (dimension) {
                "balance" -> dimensionScores.balance
                "ruleOfThirds" -> dimensionScores.ruleOfThirds
                "symmetry" -> dimensionScores.symmetry
                "leadingLines" -> dimensionScores.leadingLines
                "depth" -> dimensionScores.depth
                "framing" -> dimensionScores.framing
                "negativeSpace" -> dimensionScores.negativeSpace
                "tone" -> dimensionScores.tone
                "colorHarmony" -> dimensionScores.colorHarmony
                "saturation" -> dimensionScores.saturation
                "sharpness" -> dimensionScores.sharpness
                "noise" -> dimensionScores.noise
                else -> 0.0f
            }
            weightedScore += score * adjustedWeight
            totalWeight += adjustedWeight
        }
        
        // 场景额外加分
        val sceneBonus = calculateSceneBonus(sceneType, objects, bitmap)
        
        return minOf((weightedScore / totalWeight) + sceneBonus, 10.0f)
    }
    
    /**
     * 计算场景额外加分
     */
    private fun calculateSceneBonus(sceneType: String, objects: List<DetectionResult>, bitmap: Bitmap): Float {
        var bonus = 0.0f
        
        when (sceneType) {
            "人像" -> {
                // 面部位置：面部中心应在上1/3线附近
                val hasPerson = objects.any { it.className == "person" }
                if (hasPerson) bonus += 2.0f
                
                // 背景虚化：简化实现
                bonus += 1.0f
            }
            "风景" -> {
                // 地平线位置：简化实现
                bonus += 2.0f
                
                // 前景兴趣点
                if (objects.size > 1) bonus += 1.0f
            }
            "建筑" -> {
                // 垂直线条：简化实现
                bonus += 2.0f
                
                // 透视校正：简化实现
                bonus += 1.0f
            }
            "微距" -> {
                // 主体比例
                if (objects.isNotEmpty()) bonus += 2.0f
                
                // 细节清晰度
                bonus += 1.0f
            }
            "街拍" -> {
                // 动态感：简化实现
                bonus += 2.0f
                
                // 故事性：简化实现
                bonus += 1.0f
            }
        }
        
        return minOf(bonus, 3.0f)
    }
    
    /**
     * 生成改进建议
     */
    private fun generateRecommendations(
        dimensionScores: DimensionScores,
        sceneType: String,
        objects: List<DetectionResult>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // 基于低分维度生成建议
        if (dimensionScores.ruleOfThirds < 6.0f) {
            recommendations.add("主体偏离黄金分割点，建议调整主体位置")
        }
        
        if (dimensionScores.balance < 6.0f) {
            recommendations.add("画面视觉重量分布不均，建议调整物体布局")
        }
        
        if (dimensionScores.leadingLines < 6.0f) {
            recommendations.add("缺乏视觉引导线，建议寻找自然线条引导视线")
        }
        
        if (dimensionScores.depth < 6.0f) {
            recommendations.add("画面层次感不足，建议添加前景元素")
        }
        
        if (dimensionScores.tone < 6.0f) {
            recommendations.add("影调不理想，建议调整曝光补偿")
        }
        
        if (dimensionScores.colorHarmony < 6.0f) {
            recommendations.add("色彩搭配不够和谐，建议调整色彩平衡")
        }
        
        // 场景特定建议
        when (sceneType) {
            "人像" -> {
                if (objects.any { it.className == "person" }) {
                    recommendations.add("人像摄影建议：将人物眼睛放在画面上三分之一位置")
                }
            }
            "风景" -> {
                recommendations.add("风景摄影建议：注意地平线位置，可放在画面三分之一处")
            }
            "建筑" -> {
                recommendations.add("建筑摄影建议：确保垂直线条与画面边缘平行")
            }
            "微距" -> {
                recommendations.add("微距摄影建议：确保主体占据画面40-60%比例")
            }
            "街拍" -> {
                recommendations.add("街拍建议：捕捉动态瞬间，增强画面故事性")
            }
        }
        
        // 最多返回3个建议
        return recommendations.take(3)
    }
}