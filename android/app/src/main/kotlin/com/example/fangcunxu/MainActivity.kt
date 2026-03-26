package com.example.fangcunxu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var aiEngine: AIEngine
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val REQUEST_CAMERA_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化相机预览视图
        previewView = findViewById(R.id.previewView)
        
        // 初始化相机执行器
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // 初始化 AI 引擎
        aiEngine = AIEngine(this, useMockData = true) // 使用模拟数据
        
        // 加载 AI 模型
        scope.launch {
            val success = aiEngine.loadAllModels()
            if (success) {
                Toast.makeText(this@MainActivity, "AI 引擎初始化成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "AI 引擎初始化失败，使用模拟数据", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 检查相机权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
        
        // 设置相机选择按钮点击事件
        findViewById<View>(R.id.btnCameraSelect).setOnClickListener {
            Toast.makeText(this, "相机选择功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 设置拍照按钮点击事件
        findViewById<View>(R.id.btnCapture).setOnClickListener {
            takePhoto()
        }
        
        // 设置镜头选择按钮点击事件
        findViewById<View>(R.id.btnLensSelect).setOnClickListener {
            Toast.makeText(this, "镜头选择功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 设置底部导航栏按钮点击事件
        // 画卷
        findViewById<View>(R.id.navGallery).setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        // 瞬境（当前页面）
        findViewById<View>(R.id.navCamera).setOnClickListener {
            // 已经在当前页面
        }
        
        // 规序
        findViewById<View>(R.id.navSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({ 
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            // 配置预览
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // 配置图像分析
            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        // 在这里处理图像分析
                        val bitmap = image.toBitmap()
                        if (bitmap != null) {
                            // 使用 AI 引擎进行分析
                            scope.launch {
                                try {
                                    // 进行场景识别
                                    val sceneResult = aiEngine.recognizeScene(bitmap)
                                    println("场景识别结果: ${sceneResult.sceneType}, 置信度: ${sceneResult.confidence}")
                                    
                                    // 进行目标检测
                                    val detectionResults = aiEngine.detectObjects(bitmap)
                                    println("目标检测结果: ${detectionResults.size} 个物体")
                                    detectionResults.forEach { result ->
                                        println("物体: ${result.className}, 置信度: ${result.confidence}")
                                    }
                                    
                                    // 进行 NIMA 评分
                                    val nimaResult = aiEngine.getNIMAScore(bitmap)
                                    println("NIMA 评分: ${nimaResult.score}, 归一化评分: ${nimaResult.normalizedScore}")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        image.close()
                    }
                }
            
            // 配置图像捕获
            imageCapture = ImageCapture.Builder()
                .build()
            
            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // 解绑之前的所有用例
                cameraProvider.unbindAll()
                
                // 绑定相机和用例
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
    
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        aiEngine.release()
        scope.cancel()
    }
    
    private fun takePhoto() {
        val imageCapture = this::imageCapture.get()
        
        // 创建时间戳文件名
        val name = java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", java.util.Locale.US)
            .format(java.util.Date())
        
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/FangCunXu")
            }
        }
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "照片已保存", Toast.LENGTH_SHORT).show()
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "拍照失败：${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    /**
     * 将 ImageProxy 转换为 Bitmap
     */
    private fun ImageProxy.toBitmap(): Bitmap? {
        val image = this.image ?: return null
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
