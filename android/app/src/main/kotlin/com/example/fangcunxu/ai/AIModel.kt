package com.example.fangcunxu.ai

import org.tensorflow.lite.Interpreter

/**
 * AI 推理引擎基类
 * 所有 AI 模型的基类，提供通用的加载和推理接口
 */
abstract class AIModel<T, R> {
    
    protected var interpreter: Interpreter? = null
    protected val modelLoader: ModelLoader
    protected val preprocessor: ImagePreprocessor
    
    protected var isLoaded = false
    
    constructor(context: android.content.Context, modelPath: String) {
        this.modelLoader = ModelLoader(context)
        this.preprocessor = ImagePreprocessor()
        loadModel(modelPath)
    }
    
    /**
     * 加载模型
     */
    private fun loadModel(modelPath: String) {
        try {
            val modelBuffer = modelLoader.loadModel(modelPath)
            interpreter = modelLoader.createInterpreter(modelBuffer)
            isLoaded = true
            onModelLoaded()
        } catch (e: Exception) {
            e.printStackTrace()
            isLoaded = false
            onModelLoadFailed(e)
        }
    }
    
    /**
     * 模型加载成功回调
     */
    protected open fun onModelLoaded() {
        // 子类可以重写
    }
    
    /**
     * 模型加载失败回调
     */
    protected open fun onModelLoadFailed(e: Exception) {
        // 子类可以重写
    }
    
    /**
     * 执行推理
     * @param input 输入数据
     * @return 推理结果
     */
    abstract fun infer(input: T): R
    
    /**
     * 释放资源
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        isLoaded = false
    }
}
