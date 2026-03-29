package com.example.fangcunxu.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlin.math.exp

/**
 * 场景识别结果
 */
data class SceneRecognitionResult(
    val sceneType: SceneType,
    val confidence: Float,
    val allProbabilities: Map<SceneType, Float>,
    val isReliable: Boolean
)

/**
 * 场景类型枚举
 */
enum class SceneType(val displayName: String, val description: String) {
    PORTRAIT("人像", "人物肖像摄影"),
    LANDSCAPE("风景", "自然风光摄影"),
    CITY("城市", "城市街景摄影"),
    NIGHT("夜景", "夜间摄影"),
    STREET("街拍", "街头摄影"),
    FOOD("美食", "食物摄影"),
    ARCHITECTURE("建筑", "建筑摄影"),
    STILL_LIFE("静物", "静物摄影"),
    SPORTS("运动", "运动摄影"),
    MACRO("微距", "微距摄影"),
    ANIMAL("动物", "动物摄影"),
    INDOOR("室内", "室内场景"),
    OUTDOOR("户外", "户外场景"),
    GROUP("群体", "多人场景"),
    PET("宠物", "宠物摄影"),
    UNKNOWN("未知", "无法识别场景")
}

/**
 * 场景识别模型 (MobileNet V3 Small)
 * 输入：224x224 RGB 图像
 * 输出：10 个场景类型的概率分布
 */
class SceneRecognitionModel(context: Context) : 
    AIModel<Bitmap, SceneRecognitionResult>(context, "models/mobilenet_v3_small.tflite", useGpu = false) {
    
    companion object {
        private const val INPUT_SIZE = 320
        private const val CONFIDENCE_THRESHOLD = 0.3f
        private const val CACHE_DURATION_MS = 500L
        
        private val LABELS = listOf(
            "???", // 0: background
            "person", // 1
            "bicycle", // 2
            "car", // 3
            "motorcycle", // 4
            "airplane", // 5
            "bus", // 6
            "train", // 7
            "truck", // 8
            "boat", // 9
            "traffic light", // 10
            "fire hydrant", // 11
            "???", // 12
            "stop sign", // 13
            "parking meter", // 14
            "bench", // 15
            "bird", // 16
            "cat", // 17
            "dog", // 18
            "horse", // 19
            "sheep", // 20
            "cow", // 21
            "elephant", // 22
            "bear", // 23
            "zebra", // 24
            "giraffe", // 25
            "???", // 26
            "backpack", // 27
            "umbrella", // 28
            "???", // 29
            "???", // 30
            "handbag", // 31
            "tie", // 32
            "suitcase", // 33
            "frisbee", // 34
            "skis", // 35
            "snowboard", // 36
            "sports ball", // 37
            "kite", // 38
            "baseball bat", // 39
            "baseball glove", // 40
            "skateboard", // 41
            "surfboard", // 42
            "tennis racket", // 43
            "bottle", // 44
            "???", // 45
            "wine glass", // 46
            "cup", // 47
            "fork", // 48
            "knife", // 49
            "spoon", // 50
            "bowl", // 51
            "banana", // 52
            "apple", // 53
            "sandwich", // 54
            "orange", // 55
            "broccoli", // 56
            "carrot", // 57
            "hot dog", // 58
            "pizza", // 59
            "donut", // 60
            "cake", // 61
            "chair", // 62
            "couch", // 63
            "potted plant", // 64
            "bed", // 65
            "???", // 66
            "dining table", // 67
            "???", // 68
            "???", // 69
            "toilet", // 70
            "???", // 71
            "tv", // 72
            "laptop", // 73
            "mouse", // 74
            "remote", // 75
            "keyboard", // 76
            "cell phone", // 77
            "microwave", // 78
            "oven", // 79
            "toaster", // 80
            "sink", // 81
            "refrigerator", // 82
            "???", // 83
            "book", // 84
            "clock", // 85
            "vase", // 86
            "scissors", // 87
            "teddy bear", // 88
            "hair drier", // 89
            "toothbrush" // 90
        )
    }
    
    private var lastResult: SceneRecognitionResult? = null
    private var lastResultTime: Long = 0
    
    /**
     * 执行场景识别推理
     */
    override fun infer(input: Bitmap): SceneRecognitionResult {
        if (!isLoaded || interpreter == null) {
            // 模型未加载时返回模拟结果
            Log.e("SceneRecognitionModel", "模型未加载")
            return getMockSceneRecognitionResult()
        }
        
        try {
            val currentTime = System.currentTimeMillis()
            if (lastResult != null && (currentTime - lastResultTime) < CACHE_DURATION_MS) {
                return lastResult!!
            }
            
            Log.d("SceneRecognitionModel", "开始预处理图像")
            val inputBuffer = preprocessor.preprocessImageForInt8(input, INPUT_SIZE, INPUT_SIZE)
            Log.d("SceneRecognitionModel", "预处理完成")
            
            // 检查模型输入输出信息
            val inputDetails = interpreter?.getInputTensor(0)
            val outputDetails = interpreter?.getOutputTensor(0)
            Log.d("SceneRecognitionModel", "输入张量: $inputDetails")
            Log.d("SceneRecognitionModel", "输出张量: $outputDetails")
            
            // 准备输出缓冲区
            val outputBoxes = Array(1) { Array(100) { FloatArray(4) } } // detection_boxes: [1, 100, 4]
            val outputClasses = Array(1) { FloatArray(100) } // detection_classes: [1, 100]
            val outputScores = Array(1) { FloatArray(100) } // detection_scores: [1, 100]
            val outputNumDetections = FloatArray(1) // num_detections: [1]
            
            val outputs = mapOf(
                0 to outputBoxes,
                1 to outputClasses,
                2 to outputScores,
                3 to outputNumDetections
            )
            
            Log.d("SceneRecognitionModel", "开始推理")
            interpreter?.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)
            Log.d("SceneRecognitionModel", "推理完成")
            
            // 解析输出
            val numDetections = outputNumDetections[0].toInt()
            Log.d("SceneRecognitionModel", "检测数量: $numDetections")

            val detectedObjects = mutableListOf<Pair<String, Float>>()

            // 显示前10个检测结果，不管置信度如何
            for (i in 0 until minOf(10, numDetections)) {
                val score = outputScores[0][i]
                val classIndex = outputClasses[0][i].toInt()
                val className = if (classIndex < LABELS.size) LABELS[classIndex] else "???"
                Log.d("SceneRecognitionModel", "检测结果 $i: $className, 置信度: $score")
                
                if (score >= CONFIDENCE_THRESHOLD) {
                    detectedObjects.add(Pair(className, score))
                    Log.d("SceneRecognitionModel", "添加到检测列表: $className, 置信度: $score")
                }
            }

            Log.d("SceneRecognitionModel", "最终检测到的物体数量: ${detectedObjects.size}")
            
            // 根据检测结果推断场景类型
            val (sceneType, confidence) = inferSceneFromObjects(detectedObjects)
            Log.d("SceneRecognitionModel", "推断场景: $sceneType, 置信度: $confidence")
            
            val allProbs = mutableMapOf<SceneType, Float>()
            allProbs[sceneType] = confidence
            
            val isReliable = confidence >= CONFIDENCE_THRESHOLD
            
            val result = SceneRecognitionResult(
                sceneType = sceneType,
                confidence = confidence,
                allProbabilities = allProbs,
                isReliable = isReliable
            )
            
            lastResult = result
            lastResultTime = currentTime
            
            return result
        } catch (e: Exception) {
            // 推理失败时返回模拟结果
            Log.e("SceneRecognitionModel", "推理失败", e)
            e.printStackTrace()
            return getMockSceneRecognitionResult()
        }
    }
    
    /**
     * 根据检测到的物体推断场景类型
     */
    private fun inferSceneFromObjects(detectedObjects: List<Pair<String, Float>>): Pair<SceneType, Float> {
        if (detectedObjects.isEmpty()) {
            return Pair(SceneType.UNKNOWN, 0.0f)
        }

        // 场景类型映射
        val objectToScene = mapOf(
            // 人物相关
            "person" to SceneType.PORTRAIT,
            
            // 动物相关
            "bird" to SceneType.ANIMAL,
            "cat" to SceneType.PET,
            "dog" to SceneType.PET,
            "horse" to SceneType.ANIMAL,
            "sheep" to SceneType.ANIMAL,
            "cow" to SceneType.ANIMAL,
            "elephant" to SceneType.ANIMAL,
            "bear" to SceneType.ANIMAL,
            "zebra" to SceneType.ANIMAL,
            "giraffe" to SceneType.ANIMAL,
            
            // 交通工具
            "bicycle" to SceneType.STREET,
            "car" to SceneType.CITY,
            "motorcycle" to SceneType.STREET,
            "bus" to SceneType.CITY,
            "train" to SceneType.CITY,
            "truck" to SceneType.CITY,
            "boat" to SceneType.LANDSCAPE,
            "airplane" to SceneType.LANDSCAPE,
            
            // 户外场景
            "bench" to SceneType.STREET,
            "traffic light" to SceneType.STREET,
            "fire hydrant" to SceneType.STREET,
            "stop sign" to SceneType.STREET,
            "parking meter" to SceneType.STREET,
            "kite" to SceneType.OUTDOOR,
            
            // 室内物品
            "potted plant" to SceneType.INDOOR,
            "chair" to SceneType.INDOOR,
            "couch" to SceneType.INDOOR,
            "dining table" to SceneType.INDOOR,
            "bed" to SceneType.INDOOR,
            "toilet" to SceneType.INDOOR,
            "tv" to SceneType.INDOOR,
            "laptop" to SceneType.INDOOR,
            "mouse" to SceneType.INDOOR,
            "remote" to SceneType.INDOOR,
            "keyboard" to SceneType.INDOOR,
            "cell phone" to SceneType.INDOOR,
            "microwave" to SceneType.INDOOR,
            "oven" to SceneType.INDOOR,
            "toaster" to SceneType.INDOOR,
            "sink" to SceneType.INDOOR,
            "refrigerator" to SceneType.INDOOR,
            "book" to SceneType.INDOOR,
            "clock" to SceneType.INDOOR,
            "vase" to SceneType.INDOOR,
            "teddy bear" to SceneType.INDOOR,
            "scissors" to SceneType.INDOOR,
            "hair drier" to SceneType.INDOOR,
            "toothbrush" to SceneType.INDOOR,
            "bottle" to SceneType.INDOOR,
            
            // 食物相关
            "wine glass" to SceneType.FOOD,
            "cup" to SceneType.FOOD,
            "fork" to SceneType.FOOD,
            "knife" to SceneType.FOOD,
            "spoon" to SceneType.FOOD,
            "bowl" to SceneType.FOOD,
            "banana" to SceneType.FOOD,
            "apple" to SceneType.FOOD,
            "sandwich" to SceneType.FOOD,
            "orange" to SceneType.FOOD,
            "broccoli" to SceneType.FOOD,
            "carrot" to SceneType.FOOD,
            "hot dog" to SceneType.FOOD,
            "pizza" to SceneType.FOOD,
            "donut" to SceneType.FOOD,
            "cake" to SceneType.FOOD,
            
            // 运动相关
            "sports ball" to SceneType.SPORTS,
            "skateboard" to SceneType.SPORTS,
            "surfboard" to SceneType.SPORTS,
            "tennis racket" to SceneType.SPORTS,
            "frisbee" to SceneType.SPORTS,
            "skis" to SceneType.SPORTS,
            "snowboard" to SceneType.SPORTS,
            "baseball bat" to SceneType.SPORTS,
            "baseball glove" to SceneType.SPORTS,
            
            // 其他
            "backpack" to SceneType.STREET,
            "umbrella" to SceneType.STREET,
            "handbag" to SceneType.STREET,
            "tie" to SceneType.PORTRAIT,
            "suitcase" to SceneType.CITY
        )

        // 统计场景出现次数和置信度
        val sceneScores = mutableMapOf<SceneType, Float>()
        var totalScore = 0.0f

        for ((objectName, score) in detectedObjects) {
            val scene = objectToScene[objectName] ?: SceneType.UNKNOWN
            sceneScores[scene] = (sceneScores[scene] ?: 0.0f) + score
            totalScore += score
        }

        if (totalScore == 0.0f) {
            return Pair(SceneType.UNKNOWN, 0.0f)
        }

        // 找到得分最高的场景
        var bestScene = SceneType.UNKNOWN
        var bestScore = 0.0f

        for ((scene, score) in sceneScores) {
            val normalizedScore = score / totalScore
            if (normalizedScore > bestScore) {
                bestScore = normalizedScore
                bestScene = scene
            }
        }

        // 如果最佳场景是UNKNOWN，尝试根据检测到的物体类型推断
        if (bestScene == SceneType.UNKNOWN) {
            // 检查是否有人物
            val hasPerson = detectedObjects.any { it.first == "person" }
            // 检查是否有动物
            val hasAnimal = detectedObjects.any { it.first in listOf("bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe") }
            // 检查是否有室内物品
            val hasIndoorObjects = detectedObjects.any { objectToScene[it.first] == SceneType.INDOOR }
            // 检查是否有户外物品
            val hasOutdoorObjects = detectedObjects.any { objectToScene[it.first] in listOf(SceneType.STREET, SceneType.CITY, SceneType.LANDSCAPE) }
            // 检查是否有食物
            val hasFood = detectedObjects.any { objectToScene[it.first] == SceneType.FOOD }
            
            when {
                hasPerson -> bestScene = SceneType.PORTRAIT
                hasAnimal -> bestScene = if (detectedObjects.any { it.first in listOf("cat", "dog") }) SceneType.PET else SceneType.ANIMAL
                hasFood -> bestScene = SceneType.FOOD
                hasIndoorObjects -> bestScene = SceneType.INDOOR
                hasOutdoorObjects -> bestScene = SceneType.OUTDOOR
                else -> bestScene = SceneType.UNKNOWN
            }
        }
        
        // 检查是否有多个人物
        val personCount = detectedObjects.count { it.first == "person" }
        if (personCount >= 2) {
            bestScene = SceneType.GROUP
        }

        return Pair(bestScene, bestScore)
    }
    
    /**
     * Softmax 函数
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expValues = logits.map { exp(it - maxLogit) }
        val sumExp = expValues.sum()
        return expValues.map { it / sumExp }.toFloatArray()
    }
    
    /**
     * 获取模拟的场景识别结果
     */
    private fun getMockSceneRecognitionResult(): SceneRecognitionResult {
        // 返回UNKNOWN而不是随机结果
        val confidence = 0.0f
        
        // 生成模拟的概率分布
        val allProbs = mutableMapOf<SceneType, Float>()
        allProbs[SceneType.UNKNOWN] = 1.0f
        
        return SceneRecognitionResult(
            sceneType = SceneType.UNKNOWN,
            confidence = confidence,
            allProbabilities = allProbs,
            isReliable = false
        )
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        lastResult = null
        lastResultTime = 0
    }
    
    override fun close() {
        super.close()
        clearCache()
    }
}
