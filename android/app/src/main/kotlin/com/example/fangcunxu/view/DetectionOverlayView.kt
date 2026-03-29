package com.example.fangcunxu.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.fangcunxu.ai.DetectionResult

/**
 * 检测框叠加视图
 * 用于在相机预览上绘制目标检测框
 */
class DetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val detections = mutableListOf<DetectionResult>()
    private val categoryColors = mutableMapOf<String, Int>()

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val labelPadding = 12f
    private val cornerRadius = 8f

    /**
     * 更新检测结果
     */
    fun updateDetections(
        detections: List<DetectionResult>,
        colors: Map<String, Int> = emptyMap()
    ) {
        this.detections.clear()
        this.detections.addAll(detections)
        this.categoryColors.clear()
        this.categoryColors.putAll(colors)
        invalidate()
    }

    /**
     * 清除检测框
     */
    fun clearDetections() {
        detections.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (detection in detections) {
            drawDetection(canvas, detection)
        }
    }

    /**
     * 绘制单个检测结果
     */
    private fun drawDetection(canvas: Canvas, detection: DetectionResult) {
        val color = categoryColors[detection.className] ?: Color.GREEN

        // 绘制检测框
        boxPaint.color = color
        canvas.drawRect(detection.boundingBox, boxPaint)

        // 绘制标签背景
        val labelText = "${detection.className} ${(detection.confidence * 100).toInt()}%"

        // 计算文字尺寸
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()

        val labelLeft = detection.boundingBox.left
        val labelTop = detection.boundingBox.top - textHeight - labelPadding * 3
        val labelRight = labelLeft + textWidth + labelPadding * 2
        val labelBottom = detection.boundingBox.top

        // 确保标签在视图内
        val adjustedLabelTop: Float
        val adjustedLabelBottom: Float

        if (labelTop < 0) {
            // 标签在框上方放不下，放到框下方
            adjustedLabelTop = detection.boundingBox.bottom
            adjustedLabelBottom = detection.boundingBox.bottom + textHeight + labelPadding * 3
        } else {
            adjustedLabelTop = labelTop
            adjustedLabelBottom = labelBottom
        }

        // 绘制标签背景
        textBackgroundPaint.color = color
        canvas.drawRoundRect(
            labelLeft,
            adjustedLabelTop,
            labelRight,
            adjustedLabelBottom,
            cornerRadius,
            cornerRadius,
            textBackgroundPaint
        )

        // 绘制标签文字
        val textX = labelLeft + labelPadding
        val textY = adjustedLabelBottom - labelPadding * 1.5f
        canvas.drawText(labelText, textX, textY, textPaint)
    }

    /**
     * 设置检测框线条宽度
     */
    fun setStrokeWidth(width: Float) {
        boxPaint.strokeWidth = width
        invalidate()
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(size: Float) {
        textPaint.textSize = size
        invalidate()
    }
}
