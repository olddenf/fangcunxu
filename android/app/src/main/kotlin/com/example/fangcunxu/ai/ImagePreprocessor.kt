package com.example.fangcunxu.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 图像预处理器
 * 负责将 Bitmap 转换为模型输入的 ByteBuffer
 */
class ImagePreprocessor {
    
    private var reusableBuffer: ByteBuffer? = null
    private var bufferSize: Int = 0
    
    /**
     * YUV 转 RGB Bitmap
     * @param image CameraX ImageProxy
     */
    fun yuvToRgb(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    private var reusableFloatBuffer: ByteBuffer? = null
    private var reusableByteBuffer: ByteBuffer? = null
    private var floatBufferSize: Int = 0
    private var byteBufferSize: Int = 0
    
    /**
     * 调整图像大小并归一化（返回浮点数缓冲区）
     * @param bitmap 原始图像
     * @param width 目标宽度
     * @param height 目标高度
     * @param normalize 是否归一化
     */
    fun preprocessImage(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        normalize: Boolean = true
    ): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        val requiredSize = 4 * width * height * 3
        if (reusableFloatBuffer == null || floatBufferSize != requiredSize) {
            reusableFloatBuffer = ByteBuffer.allocateDirect(requiredSize)
            reusableFloatBuffer?.order(ByteOrder.nativeOrder())
            floatBufferSize = requiredSize
        }
        
        reusableFloatBuffer?.rewind()
        
        val intValues = IntArray(width * height)
        scaledBitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        
        for (pixelValue in intValues) {
            val r = (pixelValue shr 16) and 0xFF
            val g = (pixelValue shr 8) and 0xFF
            val b = pixelValue and 0xFF
            
            if (normalize) {
                reusableFloatBuffer?.putFloat((r - 127.5f) / 127.5f)
                reusableFloatBuffer?.putFloat((g - 127.5f) / 127.5f)
                reusableFloatBuffer?.putFloat((b - 127.5f) / 127.5f)
            } else {
                reusableFloatBuffer?.putFloat(r.toFloat())
                reusableFloatBuffer?.putFloat(g.toFloat())
                reusableFloatBuffer?.putFloat(b.toFloat())
            }
        }
        
        reusableFloatBuffer?.rewind()
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        return reusableFloatBuffer!!
    }
    
    /**
     * 调整图像大小并返回8位整数缓冲区
     * @param bitmap 原始图像
     * @param width 目标宽度
     * @param height 目标高度
     */
    fun preprocessImageForInt8(
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        val requiredSize = width * height * 3
        if (reusableByteBuffer == null || byteBufferSize != requiredSize) {
            reusableByteBuffer = ByteBuffer.allocateDirect(requiredSize)
            reusableByteBuffer?.order(ByteOrder.nativeOrder())
            byteBufferSize = requiredSize
        }
        
        reusableByteBuffer?.rewind()
        
        val intValues = IntArray(width * height)
        scaledBitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        
        for (pixelValue in intValues) {
            val r = (pixelValue shr 16) and 0xFF
            val g = (pixelValue shr 8) and 0xFF
            val b = pixelValue and 0xFF
            
            reusableByteBuffer?.put(r.toByte())
            reusableByteBuffer?.put(g.toByte())
            reusableByteBuffer?.put(b.toByte())
        }
        
        reusableByteBuffer?.rewind()
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        return reusableByteBuffer!!
    }
    
    /**
     * 中心裁剪并缩放
     * @param bitmap 原始图像
     * @param targetSize 目标尺寸
     */
    fun centerCropAndResize(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = minOf(width, height)
        
        val x = (width - size) / 2
        val y = (height - size) / 2
        
        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
        return Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, false)
    }
    
    /**
     * 调整图像大小（不创建 ByteBuffer）
     * @param bitmap 原始图像
     * @param width 目标宽度
     * @param height 目标高度
     */
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }
    
    /**
     * 释放资源
     */
    fun release() {
        reusableFloatBuffer = null
        reusableByteBuffer = null
        floatBufferSize = 0
        byteBufferSize = 0
    }
}

/**
 * ImageProxy 扩展函数 - 转换为 Bitmap
 */
fun ImageProxy.convertToBitmap(): Bitmap? {
    val preprocessor = ImagePreprocessor()
    return preprocessor.yuvToRgb(this)
}