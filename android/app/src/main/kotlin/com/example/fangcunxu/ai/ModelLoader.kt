package com.example.fangcunxu.ai

import android.content.Context
import android.os.Build
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * 模型加载状态
 */
enum class ModelLoadState {
    LOADING,
    SUCCESS,
    FAILED
}

/**
 * 模型加载结果
 */
data class ModelLoadResult(
    val state: ModelLoadState,
    val interpreter: Interpreter? = null,
    val errorMessage: String? = null,
    val loadTimeMs: Long = 0,
    val useGpu: Boolean = false
)

/**
 * AI 模型加载器
 * 负责从 assets 加载 TFLite 模型文件
 */
class ModelLoader(private val context: Context) {
    
    private val compatList = CompatibilityList()
    
    /**
     * 加载模型文件
     * @param modelPath assets 中的模型路径
     */
    fun loadModel(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * 创建 Interpreter 实例（带 GPU 加速）
     * @param modelBuffer 模型数据缓冲区
     * @param useGpu 是否使用 GPU 加速
     */
    fun createInterpreter(modelBuffer: MappedByteBuffer, useGpu: Boolean = true): ModelLoadResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                
                if (useGpu && compatList.isDelegateSupportedOnThisDevice) {
                    val gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                } else {
                    setUseNNAPI(true)
                }
            }
            
            val interpreter = Interpreter(modelBuffer, options)
            val loadTime = System.currentTimeMillis() - startTime
            
            ModelLoadResult(
                state = ModelLoadState.SUCCESS,
                interpreter = interpreter,
                loadTimeMs = loadTime,
                useGpu = useGpu && compatList.isDelegateSupportedOnThisDevice
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ModelLoadResult(
                state = ModelLoadState.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * 验证模型文件是否存在
     * @param modelPath assets 中的模型路径
     */
    fun validateModel(modelPath: String): Boolean {
        return try {
            val fileDescriptor = context.assets.openFd(modelPath)
            fileDescriptor.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取模型文件大小
     * @param modelPath assets 中的模型路径
     */
    fun getModelSize(modelPath: String): Long {
        return try {
            val fileDescriptor = context.assets.openFd(modelPath)
            val size = fileDescriptor.declaredLength
            fileDescriptor.close()
            size
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
    }
}
