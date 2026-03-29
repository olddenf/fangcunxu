package com.example.fangcunxu

import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.Manifest
import android.Manifest.permission
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.ImageProxy
import com.example.fangcunxu.ai.AIEngine
import com.example.fangcunxu.ai.SceneType
import com.example.fangcunxu.ai.SceneRecognitionResult
import com.example.fangcunxu.ai.DetectionResult
import com.example.fangcunxu.ai.CompositionResult
import com.example.fangcunxu.ai.convertToBitmap
import com.example.fangcunxu.view.DetectionOverlayView
import kotlinx.coroutines.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var aiEngine: AIEngine
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private lateinit var tvSceneType: TextView
    private lateinit var tvSceneConfidence: TextView
    private lateinit var ivSceneIcon: ImageView
    private lateinit var detectionOverlay: DetectionOverlayView
    
    // 构图评分和建议UI组件
    private lateinit var tvCompositionScore: TextView
    private lateinit var tvCompositionGrade: TextView
    private lateinit var vScoreProgress: View
    private lateinit var tvSuggestion1: TextView
    private lateinit var tvSuggestion2: TextView
    private lateinit var tvSuggestion3: TextView
    
    private val REQUEST_CAMERA_PERMISSION = 100
    
    // 场景识别相关
    private var lastSceneUpdateTime = 0L
    private val SCENE_UPDATE_INTERVAL = 800L
    private val recentResults = mutableListOf<SceneRecognitionResult>()
    private val MAX_RECENT_RESULTS = 3
    
    // 目标检测相关
    private var lastDetectionTime = 0L
    private val DETECTION_INTERVAL = 300L // 目标检测更新间隔更短，更流畅
    private var latestDetections = listOf<DetectionResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate开始")
        
        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        Log.d("MainActivity", "初始化UI组件")
        
        tvSceneType = findViewById(R.id.tvSceneType)
        tvSceneConfidence = findViewById(R.id.tvSceneConfidence)
        ivSceneIcon = findViewById(R.id.ivSceneIcon)
        detectionOverlay = findViewById(R.id.detectionOverlay)
        
        // 初始化构图评分和建议UI组件
        tvCompositionScore = findViewById(R.id.tvCompositionScore)
        tvCompositionGrade = findViewById(R.id.tvCompositionGrade)
        vScoreProgress = findViewById(R.id.vScoreProgress)
        tvSuggestion1 = findViewById(R.id.tvSuggestion1)
        tvSuggestion2 = findViewById(R.id.tvSuggestion2)
        tvSuggestion3 = findViewById(R.id.tvSuggestion3)
        
        aiEngine = AIEngine(this)
        Log.d("MainActivity", "创建AI引擎")
        
        scope.launch {
            try {
                tvSceneType.text = "加载中..."
                tvSceneConfidence.text = ""
                Log.d("MainActivity", "开始加载AI模型")
                
                val success = aiEngine.loadAllModels()
                if (success) {
                    Toast.makeText(this@MainActivity, "场景识别模型加载成功", Toast.LENGTH_SHORT).show()
                    tvSceneType.text = "等待分析"
                    Log.d("MainActivity", "AI引擎初始化成功")
                } else {
                    Toast.makeText(this@MainActivity, "场景识别模型加载失败，请检查模型文件", Toast.LENGTH_SHORT).show()
                    tvSceneType.text = "模型未加载"
                    Log.d("MainActivity", "AI引擎初始化失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "场景识别模型加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                tvSceneType.text = "加载失败"
                Log.e("MainActivity", "AI引擎初始化异常", e)
            }
        }
        
        if (allPermissionsGranted()) {
            Log.d("MainActivity", "权限已授予，开始启动相机")
            startCamera()
        } else {
            Log.d("MainActivity", "申请相机权限")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
        
        findViewById<View>(R.id.btnCapture).setOnClickListener {
            takePhoto()
        }
        
        findViewById<View>(R.id.navGallery).setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        findViewById<View>(R.id.navCamera).setOnClickListener {
        }
        
        findViewById<View>(R.id.navSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        Log.d("MainActivity", "onCreate完成")
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({ 
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        val bitmap = image.convertToBitmap()
                        if (bitmap != null) {
                            analyzeImage(bitmap)
                        }
                        image.close()
                    }
                }
            
            imageCapture = ImageCapture.Builder()
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "相机启动失败：${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun analyzeImage(bitmap: Bitmap) {
        scope.launch {
            try {
                Log.d("MainActivity", "开始分析图像")
                
                val currentTime = System.currentTimeMillis()
                
                // 场景识别（间隔较长）
                if (currentTime - lastSceneUpdateTime >= SCENE_UPDATE_INTERVAL) {
                    lastSceneUpdateTime = currentTime
                    Log.d("MainActivity", "执行场景识别")
                    
                    try {
                        val sceneResult = withContext(Dispatchers.IO) {
                            Log.d("MainActivity", "开始场景识别")
                            val result = aiEngine.recognizeScene(bitmap)
                            Log.d("MainActivity", "场景识别结果: ${result.sceneType}, 置信度: ${result.confidence}")
                            result
                        }
                        
                        val smoothedResult = smoothSceneResult(sceneResult)
                        updateSceneUI(smoothedResult.sceneType, smoothedResult.confidence, smoothedResult.isReliable)
                        Log.d("MainActivity", "更新场景UI完成: ${smoothedResult.sceneType}")
                        
                        // 执行目标检测和构图分析
                        try {
                            val detections = withContext(Dispatchers.IO) {
                                Log.d("MainActivity", "开始目标检测")
                                val results = aiEngine.detectObjects(bitmap)
                                Log.d("MainActivity", "目标检测完成，检测到 ${results.size} 个目标")
                                results
                            }
                            
                            // 将检测结果映射到预览视图尺寸
                            val mappedDetections = mapDetectionsToPreview(detections)
                            latestDetections = mappedDetections
                            
                            // 更新检测框UI
                            updateDetectionOverlay(mappedDetections)
                            Log.d("MainActivity", "更新检测框UI完成")
                            
                            // 执行构图分析
                            try {
                                val compositionResult = withContext(Dispatchers.IO) {
                                    Log.d("MainActivity", "开始构图分析")
                                    val result = aiEngine.analyzeComposition(bitmap, smoothedResult.sceneType.displayName, mappedDetections)
                                    Log.d("MainActivity", "构图分析完成，评分: ${result.overallScore}")
                                    result
                                }
                                
                                // 更新构图评分和建议UI
                                updateCompositionUI(compositionResult)
                                Log.d("MainActivity", "更新构图UI完成")
                            } catch (e: Exception) {
                                Log.e("MainActivity", "构图分析失败", e)
                                e.printStackTrace()
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "目标检测失败", e)
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "场景识别失败", e)
                        e.printStackTrace()
                    }
                } else if (currentTime - lastDetectionTime >= DETECTION_INTERVAL) {
                    // 仅执行目标检测（更频繁）
                    lastDetectionTime = currentTime
                    Log.d("MainActivity", "仅执行目标检测")
                    
                    try {
                        val detections = withContext(Dispatchers.IO) {
                            Log.d("MainActivity", "开始目标检测")
                            val results = aiEngine.detectObjects(bitmap)
                            Log.d("MainActivity", "目标检测完成，检测到 ${results.size} 个目标")
                            results
                        }
                        
                        // 将检测结果映射到预览视图尺寸
                        val mappedDetections = mapDetectionsToPreview(detections)
                        latestDetections = mappedDetections
                        
                        // 更新检测框UI
                        updateDetectionOverlay(mappedDetections)
                        Log.d("MainActivity", "更新检测框UI完成")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "目标检测失败", e)
                        e.printStackTrace()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "分析过程异常", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 将检测结果映射到预览视图尺寸
     */
    private fun mapDetectionsToPreview(detections: List<DetectionResult>): List<DetectionResult> {
        val model = aiEngine.getObjectDetectionModel() ?: run {
            Log.e("MainActivity", "目标检测模型未加载")
            return emptyList()
        }
        
        val previewWidth = previewView.width
        val previewHeight = previewView.height
        
        Log.d("MainActivity", "预览尺寸: $previewWidth x $previewHeight")
        Log.d("MainActivity", "原始检测结果数量: ${detections.size}")
        
        if (previewWidth == 0 || previewHeight == 0) {
            Log.e("MainActivity", "预览尺寸为0，无法映射检测框")
            return emptyList()
        }
        
        val mappedDetections = detections.map { detection ->
            Log.d("MainActivity", "原始检测框: ${detection.boundingBox}")
            val mapped = model.mapDetectionToOriginal(detection, previewWidth, previewHeight)
            Log.d("MainActivity", "映射后检测框: ${mapped.boundingBox}")
            mapped
        }
        
        Log.d("MainActivity", "映射后检测结果数量: ${mappedDetections.size}")
        return mappedDetections
    }
    
    /**
     * 更新检测框叠加视图
     */
    private fun updateDetectionOverlay(detections: List<DetectionResult>) {
        val model = aiEngine.getObjectDetectionModel()
        val colors = mutableMapOf<String, Int>()
        
        // 为每个检测到的类别获取颜色
        detections.forEach { detection ->
            if (!colors.containsKey(detection.className)) {
                colors[detection.className] = model?.getCategoryColor(detection.className) ?: android.graphics.Color.GREEN
            }
        }
        
        detectionOverlay.updateDetections(detections, colors)
    }
    
    private fun updateSceneUI(sceneType: SceneType, confidence: Float, isReliable: Boolean) {
        tvSceneType.text = sceneType.displayName
        tvSceneConfidence.text = "${(confidence * 100).toInt()}%"
        
        val iconRes = getSceneIcon(sceneType)
        ivSceneIcon.setImageResource(iconRes)
        
        val textColor = if (isReliable) {
            ContextCompat.getColor(this, R.color.green_primary)
        } else {
            ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        tvSceneType.setTextColor(textColor)
        tvSceneConfidence.setTextColor(textColor)
    }
    
    /**
     * 更新构图评分和建议UI
     */
    private fun updateCompositionUI(compositionResult: CompositionResult) {
        val score = compositionResult.overallScore
        val recommendations = compositionResult.recommendations
        
        // 更新评分
        tvCompositionScore.text = score.toInt().toString()
        
        // 计算等级
        val grade = when {
            score >= 8.0f -> "优秀"
            score >= 7.0f -> "良好"
            score >= 6.0f -> "一般"
            score >= 5.0f -> "需改进"
            else -> "较差"
        }
        tvCompositionGrade.text = grade
        
        // 更新进度条
        val progressWeight = score / 10.0f
        vScoreProgress.layoutParams = LinearLayout.LayoutParams(
            0, 
            LinearLayout.LayoutParams.MATCH_PARENT,
            progressWeight
        )
        vScoreProgress.requestLayout()
        
        // 更新建议
        tvSuggestion1.text = if (recommendations.isNotEmpty()) "• ${recommendations[0]}" else "• 尝试将主体放置在三分法则的交点上"
        tvSuggestion2.text = if (recommendations.size > 1) "• ${recommendations[1]}" else "• 注意画面的平衡和对称性"
        tvSuggestion3.text = if (recommendations.size > 2) "• ${recommendations[2]}" else "• 寻找自然的视觉引导线"
    }
    
    private fun getSceneIcon(sceneType: SceneType): Int {
        return when (sceneType) {
            SceneType.PORTRAIT -> R.drawable.ic_camera
            SceneType.LANDSCAPE -> R.drawable.ic_camera
            SceneType.CITY -> R.drawable.ic_camera
            SceneType.NIGHT -> R.drawable.ic_camera
            SceneType.STREET -> R.drawable.ic_camera
            SceneType.FOOD -> R.drawable.ic_camera
            SceneType.ARCHITECTURE -> R.drawable.ic_camera
            SceneType.STILL_LIFE -> R.drawable.ic_camera
            SceneType.SPORTS -> R.drawable.ic_camera
            SceneType.MACRO -> R.drawable.ic_camera
            SceneType.ANIMAL -> R.drawable.ic_camera
            SceneType.INDOOR -> R.drawable.ic_camera
            SceneType.OUTDOOR -> R.drawable.ic_camera
            SceneType.GROUP -> R.drawable.ic_camera
            SceneType.PET -> R.drawable.ic_camera
            SceneType.UNKNOWN -> R.drawable.ic_camera
        }
    }
    
    private fun smoothSceneResult(newResult: SceneRecognitionResult): SceneRecognitionResult {
        recentResults.add(newResult)
        if (recentResults.size > MAX_RECENT_RESULTS) {
            recentResults.removeAt(0)
        }
        
        Log.d("MainActivity", "最近结果数量: ${recentResults.size}")
        
        val sceneTypeCounts = mutableMapOf<SceneType, Int>()
        for (result in recentResults) {
            val count = sceneTypeCounts[result.sceneType] ?: 0
            sceneTypeCounts[result.sceneType] = count + 1
            Log.d("MainActivity", "结果: ${result.sceneType}, 置信度: ${result.confidence}")
        }
        
        Log.d("MainActivity", "场景统计: $sceneTypeCounts")
        
        var dominantScene: SceneType = newResult.sceneType
        var maxCount = 0
        for ((sceneType, count) in sceneTypeCounts) {
            if (count > maxCount) {
                maxCount = count
                dominantScene = sceneType
            }
        }
        
        var totalConfidence = 0.0f
        for (result in recentResults) {
            totalConfidence += result.confidence
        }
        val avgConfidence = totalConfidence / recentResults.size
        
        Log.d("MainActivity", "主导场景: $dominantScene, 平均置信度: $avgConfidence")
        
        return SceneRecognitionResult(
            sceneType = dominantScene,
            confidence = avgConfidence,
            allProbabilities = newResult.allProbabilities,
            isReliable = avgConfidence >= 0.7f
        )
    }
    
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.convertToBitmap()
                    image.close()
                    
                    if (bitmap != null) {
                        Toast.makeText(this@MainActivity, "照片已保存", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "拍照失败：${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        scope.cancel()
        aiEngine.release()
    }
}