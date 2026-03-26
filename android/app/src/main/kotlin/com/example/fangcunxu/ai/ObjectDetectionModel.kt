package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import java.nio.ByteBuffer

/**
 * 目标检测结果
 */
data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF
)

/**
 * 目标检测模型 (YOLOv8n)
 * 输入：640x640 RGB 图像
 * 输出：物体检测列表 [{label, bbox, confidence}]
 */
class ObjectDetectionModel(context: Context) : 
    AIModel<Bitmap, List<DetectionResult>>(context, "models/yolo_v8n_int8.tflite") {
    
    companion object {
        private const val INPUT_SIZE = 640
        private const val NUM_CLASSES = 80 // COCO 数据集 80 类
        private const val CONFIDENCE_THRESHOLD = 0.5f
        private const val IOU_THRESHOLD = 0.45f
        
        // COCO 数据集类别标签
        val LABELS = listOf(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
            "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
            "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
            "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
            "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
            "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
            "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
        )
    }
    
    /**
     * 执行目标检测推理
     */
    override fun infer(input: Bitmap): List<DetectionResult> {
        if (!isLoaded || interpreter == null) {
            throw IllegalStateException("Model not loaded")
        }
        
        // 预处理图像
        val inputBuffer = preprocessor.preprocessImage(input, INPUT_SIZE, INPUT_SIZE, normalize = true)
        
        // YOLOv8n 输出格式：[1, 84, 8400] 或 [1, 25200, 85]
        // 这里假设输出是 [1, 84, 8400] 格式
        // 简化处理：假设输出已经是检测框
        val outputBuffer = Array(1) { Array(84) { FloatArray(8400) } }
        interpreter?.run(inputBuffer, outputBuffer)
        
        // 解析 YOLO 输出 (简化版本)
        return parseYOLOOutput(outputBuffer, input.width, input.height)
    }
    
    /**
     * 解析 YOLO 输出
     */
    private fun parseYOLOOutput(
        output: Array<Array<FloatArray>>,
        imageWidth: Int,
        imageHeight: Int
    ): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()
        
        // 遍历所有检测框
        for (i in 0 until 8400) {
            // 获取置信度 (假设在索引 4)
            val confidence = output[0][4][i]
            
            if (confidence > CONFIDENCE_THRESHOLD) {
                // 找到最大概率的类别
                var maxClassIndex = 0
                var maxClassProb = 0f
                for (c in 0 until NUM_CLASSES) {
                    val prob = output[0][5 + c][i]
                    if (prob > maxClassProb) {
                        maxClassProb = prob
                        maxClassIndex = c
                    }
                }
                
                val totalConfidence = confidence * maxClassProb
                if (totalConfidence > CONFIDENCE_THRESHOLD) {
                    // 获取边界框坐标 (中心点 + 宽高)
                    val x = output[0][0][i]
                    val y = output[0][1][i]
                    val w = output[0][2][i]
                    val h = output[0][3][i]
                    
                    // 转换为 (x1, y1, x2, y2) 格式
                    val x1 = ((x - w / 2) / INPUT_SIZE * imageWidth).coerceIn(0f, imageWidth.toFloat())
                    val y1 = ((y - h / 2) / INPUT_SIZE * imageHeight).coerceIn(0f, imageHeight.toFloat())
                    val x2 = ((x + w / 2) / INPUT_SIZE * imageWidth).coerceIn(0f, imageWidth.toFloat())
                    val y2 = ((y + h / 2) / INPUT_SIZE * imageHeight).coerceIn(0f, imageHeight.toFloat())
                    
                    detections.add(
                        DetectionResult(
                            label = LABELS[maxClassIndex],
                            confidence = totalConfidence,
                            boundingBox = RectF(x1, y1, x2, y2)
                        )
                    )
                }
            }
        }
        
        // 应用 NMS (非极大值抑制)
        return applyNMS(detections)
    }
    
    /**
     * 非极大值抑制
     */
    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selectedDetections = mutableListOf<DetectionResult>()
        val used = BooleanArray(sortedDetections.size)
        
        for (i in sortedDetections.indices) {
            if (!used[i]) {
                selectedDetections.add(sortedDetections[i])
                
                for (j in (i + 1) until sortedDetections.size) {
                    if (!used[j]) {
                        val iou = calculateIoU(
                            sortedDetections[i].boundingBox,
                            sortedDetections[j].boundingBox
                        )
                        if (iou > IOU_THRESHOLD) {
                            used[j] = true
                        }
                    }
                }
            }
        }
        
        return selectedDetections
    }
    
    /**
     * 计算 IoU (交并比)
     */
    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionArea = calculateIntersectionArea(box1, box2)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        val unionArea = box1Area + box2Area - intersectionArea
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }
    
    /**
     * 计算交集面积
     */
    private fun calculateIntersectionArea(box1: RectF, box2: RectF): Float {
        val x1 = maxOf(box1.left, box2.left)
        val y1 = maxOf(box1.top, box2.top)
        val x2 = minOf(box1.right, box2.right)
        val y2 = minOf(box1.bottom, box2.bottom)
        return if (x1 < x2 && y1 < y2) (x2 - x1) * (y2 - y1) else 0f
    }
}
