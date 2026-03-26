package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * AI 模型加载器
 * 负责从 assets 加载 TFLite 模型文件
 */
class ModelLoader(private val context: Context) {
    
    /**
     * 加载模型文件
     * @param modelPath assets 中的模型路径
     */
    fun loadModel(modelPath: String): MappedByteBuffer {
        try {
            val fileDescriptor = context.assets.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            // 如果模型文件不存在，返回一个空的 MappedByteBuffer
            // 这将在模型推理时抛出异常，但至少可以让应用启动
            val buffer = ByteBuffer.allocateDirect(1024)
            buffer.order(ByteOrder.nativeOrder())
            return buffer
        }
    }
    
    /**
     * 创建 Interpreter 实例
     * @param modelBuffer 模型数据缓冲区
     */
    fun createInterpreter(modelBuffer: MappedByteBuffer): Interpreter {
        val options = Interpreter.Options().apply {
            setNumThreads(4) // 使用 4 个线程加速推理
        }
        return Interpreter(modelBuffer, options)
    }
}
