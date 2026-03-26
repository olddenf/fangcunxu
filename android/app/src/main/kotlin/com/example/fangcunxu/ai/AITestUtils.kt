package com.example.fangcunxu.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.io.InputStream

/**
 * AI 引擎测试工具
 * 用于测试模型加载和推理功能
 */
class AITestUtils {
    
    companion object {
        private const val TAG = "AITestUtils"
        
        /**
         * 从资源加载测试图像
         */
        fun loadTestImage(inputStream: InputStream): Bitmap {
            return BitmapFactory.decodeStream(inputStream)
        }
        
        /**
         * 测试 NIMA 模型
         */
        suspend fun testNIMAModel(aiEngine: AIEngine, testBitmap: Bitmap) {
            try {
                Log.d(TAG, "开始测试 NIMA 模型...")
                val startTime = System.currentTimeMillis()
                
                val result = aiEngine.getNIMAScoreAsync(testBitmap)
                
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                Log.d(TAG, "NIMA 评分完成，耗时：${duration}ms")
                Log.d(TAG, "原始评分：${result.score}")
                Log.d(TAG, "归一化评分：${result.normalizedScore}")
                Log.d(TAG, "等级：${result.rating}")
                Log.d(TAG, "评分分布：${result.distribution.joinToString(", ")}")
                
            } catch (e: Exception) {
                Log.e(TAG, "NIMA 测试失败", e)
            }
        }
        
        /**
         * 测试场景识别模型
         */
        suspend fun testSceneModel(aiEngine: AIEngine, testBitmap: Bitmap) {
            try {
                Log.d(TAG, "开始测试场景识别模型...")
                val startTime = System.currentTimeMillis()
                
                val result = aiEngine.recognizeSceneAsync(testBitmap)
                
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                Log.d(TAG, "场景识别完成，耗时：${duration}ms")
                Log.d(TAG, "识别结果：${result.sceneType}")
                Log.d(TAG, "置信度：${result.confidence}")
                Log.d(TAG, "所有概率：${result.allProbabilities}")
                
            } catch (e: Exception) {
                Log.e(TAG, "场景识别测试失败", e)
            }
        }
        
        /**
         * 测试目标检测模型
         */
        suspend fun testDetectionModel(aiEngine: AIEngine, testBitmap: Bitmap) {
            try {
                Log.d(TAG, "开始测试目标检测模型...")
                val startTime = System.currentTimeMillis()
                
                val results = aiEngine.detectObjectsAsync(testBitmap)
                
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                Log.d(TAG, "目标检测完成，耗时：${duration}ms")
                Log.d(TAG, "检测到 ${results.size} 个物体")
                results.forEachIndexed { index, detection ->
                    Log.d(TAG, "物体 $index: ${detection.label}, 置信度：${detection.confidence}, 位置：${detection.boundingBox}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "目标检测测试失败", e)
            }
        }
        
        /**
         * 运行所有测试
         */
        suspend fun runAllTests(aiEngine: AIEngine, testBitmap: Bitmap) {
            Log.d(TAG, "========== 开始 AI 引擎测试 ==========")
            
            testNIMAModel(aiEngine, testBitmap)
            delay(100)
            
            testSceneModel(aiEngine, testBitmap)
            delay(100)
            
            testDetectionModel(aiEngine, testBitmap)
            
            Log.d(TAG, "========== AI 引擎测试完成 ==========")
        }
    }
}
