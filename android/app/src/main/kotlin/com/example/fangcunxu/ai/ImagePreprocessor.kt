package com.example.fangcunxu.ai

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 图像预处理器
 * 负责将 Bitmap 转换为模型输入的 ByteBuffer
 */
class ImagePreprocessor {
    
    /**
     * 调整图像大小并归一化
     * @param bitmap 原始图像
     * @param width 目标宽度
     * @param height 目标高度
     * @param normalize 是否归一化到 0-1
     */
    fun preprocessImage(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        normalize: Boolean = true
    ): ByteBuffer {
        // 创建缩放后的 bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        // 创建 ByteBuffer，大小为 宽×高×3(RGB)×4(float)
        val byteBuffer = ByteBuffer.allocateDirect(4 * width * height * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        // 遍历每个像素，提取 RGB 值
        val intValues = IntArray(width * height)
        scaledBitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        
        for (pixelValue in intValues) {
            // 提取 RGB 分量
            val r = (pixelValue shr 16) and 0xFF
            val g = (pixelValue shr 8) and 0xFF
            val b = pixelValue and 0xFF
            
            // 归一化到 0-1 或保持 0-255
            if (normalize) {
                byteBuffer.putFloat(r / 255.0f)
                byteBuffer.putFloat(g / 255.0f)
                byteBuffer.putFloat(b / 255.0f)
            } else {
                byteBuffer.putFloat(r.toFloat())
                byteBuffer.putFloat(g.toFloat())
                byteBuffer.putFloat(b.toFloat())
            }
        }
        
        byteBuffer.rewind()
        scaledBitmap.recycle()
        
        return byteBuffer
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
}
