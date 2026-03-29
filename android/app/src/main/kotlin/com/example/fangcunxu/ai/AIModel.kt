package com.example.fangcunxu.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter

/**
 * AI 推理引擎基类
 * 所有 AI 模型的基类，提供通用的加载和推理接口
 */
abstract class AIModel<T, R>(
    context: Context,
    private val modelPath: String,
    private val useGpu: Boolean = true
) {
    
    protected var interpreter: Interpreter? = null
    protected val modelLoader: ModelLoader = ModelLoader(context)
    protected val preprocessor: ImagePreprocessor = ImagePreprocessor()
    
    var isLoaded = false
        private set
    protected var loadTimeMs: Long = 0
    protected var useGpuAcceleration = false
    
    /**
     * 加载模型
     */
    fun loadModel() {
        try {
            Log.d("AIModel", "开始加载模型: $modelPath")
            
            // 验证模型文件是否存在
            val exists = modelLoader.validateModel(modelPath)
            if (!exists) {
                Log.e("AIModel", "模型文件不存在: $modelPath")
                isLoaded = false
                onModelLoadFailed(Exception("模型文件不存在: $modelPath"))
                return
            }
            
            // 获取模型文件大小
            val modelSize = modelLoader.getModelSize(modelPath)
            Log.d("AIModel", "模型文件大小: ${modelSize / 1024}KB")
            
            val modelBuffer = modelLoader.loadModel(modelPath)
            Log.d("AIModel", "模型文件加载成功")
            
            val result = modelLoader.createInterpreter(modelBuffer, useGpu)
            Log.d("AIModel", "Interpreter创建结果: ${result.state}")
            
            when (result.state) {
                ModelLoadState.SUCCESS -> {
                    interpreter = result.interpreter
                    isLoaded = true
                    loadTimeMs = result.loadTimeMs
                    useGpuAcceleration = result.useGpu
                    Log.d("AIModel", "模型加载成功，耗时: ${loadTimeMs}ms，GPU加速: $useGpuAcceleration")
                    onModelLoaded()
                }
                ModelLoadState.FAILED -> {
                    isLoaded = false
                    Log.e("AIModel", "模型加载失败: ${result.errorMessage}")
                    onModelLoadFailed(Exception(result.errorMessage ?: "Unknown error"))
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("AIModel", "模型加载异常", e)
            e.printStackTrace()
            isLoaded = false
            onModelLoadFailed(e)
        }
    }
    
    /**
     * 模型加载成功回调
     */
    protected open fun onModelLoaded() {}
    
    /**
     * 模型加载失败回调
     */
    protected open fun onModelLoadFailed(e: Exception) {}
    
    /**
     * 执行推理
     * @param input 输入数据
     * @return 推理结果
     */
    abstract fun infer(input: T): R
    
    /**
     * 获取模型加载信息
     */
    fun getLoadInfo(): String {
        return if (isLoaded) {
            val acceleration = if (useGpuAcceleration) "GPU" else "CPU/NNAPI"
            "Model loaded in ${loadTimeMs}ms using $acceleration"
        } else {
            "Model not loaded"
        }
    }
    
    /**
     * 释放资源
     */
    open fun close() {
        interpreter?.close()
        interpreter = null
        modelLoader.release()
        isLoaded = false
    }
}
