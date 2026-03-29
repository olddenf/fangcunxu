package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 检测结果数据类
 */
data class DetectionResult(
    val className: String,
    val confidence: Float,
    val boundingBox: RectF
)

/**
 * YOLOv8 目标检测模型
 * 使用 float16 版本（更快）
 */
class ObjectDetectionModel(context: Context) :
    AIModel<Bitmap, List<DetectionResult>>(context, "models/yolov8n_float16.tflite", useGpu = true) {

    companion object {
        private const val INPUT_SIZE = 640
        private const val CONFIDENCE_THRESHOLD = 0.20f // 降低阈值以检测更多物体
        private const val IOU_THRESHOLD = 0.40f // 降低IoU阈值以更好地处理重叠
        private const val MAX_DETECTIONS = 50 // 减少最大检测数量以提高性能

        // COCO 数据集 80 个类别
        private val LABELS = listOf(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train",
            "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter",
            "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear",
            "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase",
            "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
            "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
            "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut",
            "cake", "chair", "couch", "potted plant", "bed", "dining table", "toilet",
            "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave",
            "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase",
            "scissors", "teddy bear", "hair drier", "toothbrush"
        )

        // 类别颜色映射
        private val CATEGORY_COLORS = mapOf(
            "person" to 0xFF2196F3.toInt(),      // 蓝色
            "bicycle" to 0xFF4CAF50.toInt(),     // 绿色
            "car" to 0xFFF44336.toInt(),         // 红色
            "motorcycle" to 0xFFFF9800.toInt(),  // 橙色
            "airplane" to 0xFF9C27B0.toInt(),    // 紫色
            "bus" to 0xFF00BCD4.toInt(),         // 青色
            "train" to 0xFF795548.toInt(),       // 棕色
            "truck" to 0xFF607D8B.toInt(),       // 蓝灰色
            "boat" to 0xFF3F51B5.toInt(),        // 靛蓝色
            "bird" to 0xFF8BC34A.toInt(),        // 浅绿色
            "cat" to 0xFFFF5722.toInt(),         // 深橙色
            "dog" to 0xFFE91E63.toInt(),         // 粉红色
            "horse" to 0xFF9E9E9E.toInt(),       // 灰色
            "sheep" to 0xFFCDDC39.toInt(),       // 黄绿色
            "cow" to 0xFF673AB7.toInt(),         // 深紫色
            "elephant" to 0xFFFFEB3B.toInt(),    // 黄色
            "bear" to 0xFF5D4037.toInt(),        // 深棕色
            "zebra" to 0xFF000000.toInt(),       // 黑色
            "giraffe" to 0xFFFFC107.toInt()      // 琥珀色
        )
    }

    private var inputBuffer: ByteBuffer? = null

    override fun onModelLoaded() {
        Log.d("ObjectDetectionModel", "YOLOv8模型加载成功")
        // 预分配输入缓冲区
        val inputSize = 4 * INPUT_SIZE * INPUT_SIZE * 3
        inputBuffer = ByteBuffer.allocateDirect(inputSize)
        inputBuffer?.order(ByteOrder.nativeOrder())
    }

    override fun infer(input: Bitmap): List<DetectionResult> {
        if (!isLoaded || interpreter == null) {
            Log.e("ObjectDetectionModel", "模型未加载")
            return emptyList()
        }

        return try {
            // 预处理图像
            val processedBuffer = preprocessImage(input)

            // 准备输出缓冲区
            // YOLOv8输出: [1, 84, 8400] - 84 = 4(box) + 80(classes), 8400 = 80x80 + 40x40 + 20x20
            val outputShape = intArrayOf(1, 84, 8400)
            val outputBuffer = Array(1) { Array(84) { FloatArray(8400) } }

            // 运行推理
            Log.d("ObjectDetectionModel", "开始目标检测推理")
            interpreter?.run(processedBuffer, outputBuffer)
            Log.d("ObjectDetectionModel", "目标检测推理完成")

            // 解析检测结果
            val detections = parseDetections(outputBuffer[0])
            Log.d("ObjectDetectionModel", "检测到 ${detections.size} 个目标")

            detections
        } catch (e: Exception) {
            Log.e("ObjectDetectionModel", "推理失败", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 预处理图像
     * 使用保持宽高比的缩放方式
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        inputBuffer?.rewind()

        // 计算保持宽高比的缩放
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val scale = INPUT_SIZE.toFloat() / maxOf(bitmapWidth, bitmapHeight)
        
        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()
        
        // 创建缩放后的图像
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        
        // 创建640x640的画布，用灰色填充（YOLOv8推荐的padding颜色）
        val paddedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(paddedBitmap)
        canvas.drawColor(android.graphics.Color.GRAY) // 使用灰色填充
        
        // 将缩放后的图像居中绘制
        val offsetX = (INPUT_SIZE - scaledWidth) / 2
        val offsetY = (INPUT_SIZE - scaledHeight) / 2
        canvas.drawBitmap(scaledBitmap, offsetX.toFloat(), offsetY.toFloat(), null)

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        paddedBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixelValue in intValues) {
            val r = (pixelValue shr 16) and 0xFF
            val g = (pixelValue shr 8) and 0xFF
            val b = pixelValue and 0xFF

            // 归一化到 [0, 1]
            inputBuffer?.putFloat(r / 255.0f)
            inputBuffer?.putFloat(g / 255.0f)
            inputBuffer?.putFloat(b / 255.0f)
        }

        scaledBitmap.recycle()
        paddedBitmap.recycle()

        return inputBuffer!!
    }

    /**
     * 解析检测结果
     */
    private fun parseDetections(output: Array<FloatArray>): List<DetectionResult> {
        val detections = mutableListOf<DetectionResult>()

        // 遍历所有候选框
        for (i in 0 until 8400) {
            // 获取边界框坐标
            val x = output[0][i]
            val y = output[1][i]
            val w = output[2][i]
            val h = output[3][i]

            // 获取类别置信度
            var maxConfidence = 0f
            var classIndex = 0

            for (c in 0 until 80) {
                val confidence = output[4 + c][i]
                if (confidence > maxConfidence) {
                    maxConfidence = confidence
                    classIndex = c
                }
            }

            // 过滤低置信度
            if (maxConfidence >= CONFIDENCE_THRESHOLD) {
                val className = LABELS[classIndex]

                // 转换为左上角和右下角坐标（YOLOv8输出是归一化值，需要乘以输入尺寸）
                val centerX = x * INPUT_SIZE
                val centerY = y * INPUT_SIZE
                val width = w * INPUT_SIZE
                val height = h * INPUT_SIZE
                
                val left = centerX - width / 2
                val top = centerY - height / 2
                val right = centerX + width / 2
                val bottom = centerY + height / 2

                val rect = RectF(
                    left.coerceIn(0f, INPUT_SIZE.toFloat()),
                    top.coerceIn(0f, INPUT_SIZE.toFloat()),
                    right.coerceIn(0f, INPUT_SIZE.toFloat()),
                    bottom.coerceIn(0f, INPUT_SIZE.toFloat())
                )

                detections.add(DetectionResult(className, maxConfidence, rect))
            }
        }

        // 应用NMS（非极大值抑制）
        return applyNMS(detections)
    }

    /**
     * 非极大值抑制
     */
    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        if (detections.isEmpty()) return emptyList()

        // 按置信度排序
        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val results = mutableListOf<DetectionResult>()

        while (sorted.isNotEmpty() && results.size < MAX_DETECTIONS) {
            val best = sorted[0]
            results.add(best)
            sorted.removeAt(0)

            // 移除与最佳框重叠度高的框
            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val detection = iterator.next()
                if (best.className == detection.className &&
                    calculateIoU(best.boundingBox, detection.boundingBox) > IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return results
    }

    /**
     * 计算IoU（交并比）
     */
    private fun calculateIoU(rect1: RectF, rect2: RectF): Float {
        val left = maxOf(rect1.left, rect2.left)
        val top = maxOf(rect1.top, rect2.top)
        val right = minOf(rect1.right, rect2.right)
        val bottom = minOf(rect1.bottom, rect2.bottom)

        if (left >= right || top >= bottom) return 0f

        val intersection = (right - left) * (bottom - top)
        val area1 = (rect1.right - rect1.left) * (rect1.bottom - rect1.top)
        val area2 = (rect2.right - rect2.left) * (rect2.bottom - rect2.top)
        val union = area1 + area2 - intersection

        return if (union > 0) intersection / union else 0f
    }

    /**
     * 获取类别的颜色
     */
    fun getCategoryColor(className: String): Int {
        return CATEGORY_COLORS[className] ?: 0xFF00FF00.toInt() // 默认绿色
    }

    /**
     * 将检测框坐标从模型输入尺寸映射到原始图像尺寸
     */
    fun mapDetectionToOriginal(
        detection: DetectionResult,
        originalWidth: Int,
        originalHeight: Int
    ): DetectionResult {
        val scaleX = originalWidth.toFloat() / INPUT_SIZE
        val scaleY = originalHeight.toFloat() / INPUT_SIZE

        val mappedRect = RectF(
            detection.boundingBox.left * scaleX,
            detection.boundingBox.top * scaleY,
            detection.boundingBox.right * scaleX,
            detection.boundingBox.bottom * scaleY
        )

        return detection.copy(boundingBox = mappedRect)
    }

    override fun close() {
        super.close()
        inputBuffer = null
    }
}
