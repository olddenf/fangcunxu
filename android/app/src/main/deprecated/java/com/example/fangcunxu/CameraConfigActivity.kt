package com.example.fangcunxu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fangcunxu.camera.CameraConfigManager

class CameraConfigActivity : AppCompatActivity() {
    private lateinit var btnBack: Button
    private lateinit var tvGridType: TextView
    private lateinit var switchGuideLines: Switch
    private lateinit var switchScoreDisplay: Switch
    private lateinit var tvResolution: TextView
    private lateinit var tvFrameRate: TextView
    private lateinit var tvCameraType: TextView
    private lateinit var tvAnalysisFreq: TextView
    private lateinit var tvSuggestionDetail: TextView
    private lateinit var tvVersion: TextView
    private lateinit var llPrivacy: LinearLayout
    private lateinit var llGuide: LinearLayout
    private lateinit var llFeedback: LinearLayout
    private lateinit var btnCameraConfig: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_config)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvGridType = findViewById(R.id.tvGridType)
        switchGuideLines = findViewById(R.id.switchGuideLines)
        switchScoreDisplay = findViewById(R.id.switchScoreDisplay)
        tvResolution = findViewById(R.id.tvResolution)
        tvFrameRate = findViewById(R.id.tvFrameRate)
        tvCameraType = findViewById(R.id.tvCameraType)
        tvAnalysisFreq = findViewById(R.id.tvAnalysisFreq)
        tvSuggestionDetail = findViewById(R.id.tvSuggestionDetail)
        tvVersion = findViewById(R.id.tvVersion)
        llPrivacy = findViewById(R.id.llPrivacy)
        llGuide = findViewById(R.id.llGuide)
        llFeedback = findViewById(R.id.llFeedback)
        btnCameraConfig = findViewById(R.id.btnCameraConfig)
    }

    private fun setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }

        // 默认网格类型
        tvGridType.setOnClickListener {
            showGridTypeDialog()
        }

        // 分辨率
        tvResolution.setOnClickListener {
            showResolutionDialog()
        }

        // 帧率
        tvFrameRate.setOnClickListener {
            showFrameRateDialog()
        }

        // 前后摄像头
        tvCameraType.setOnClickListener {
            showCameraTypeDialog()
        }

        // 分析频率
        tvAnalysisFreq.setOnClickListener {
            showAnalysisFreqDialog()
        }

        // 建议详细程度
        tvSuggestionDetail.setOnClickListener {
            showSuggestionDetailDialog()
        }

        // 隐私政策
        llPrivacy.setOnClickListener {
            Toast.makeText(this, "隐私政策功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 使用指南
        llGuide.setOnClickListener {
            Toast.makeText(this, "使用指南功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 反馈
        llFeedback.setOnClickListener {
            Toast.makeText(this, "反馈功能开发中", Toast.LENGTH_SHORT).show()
        }

        // 相机配置入口
        btnCameraConfig.setOnClickListener {
            // 跳转到相机配置页面
            val intent = Intent(this, CameraConfigDetailActivity::class.java)
            startActivity(intent)
        }
    }

    // 显示网格类型选择对话框
    private fun showGridTypeDialog() {
        val options = arrayOf("三分线", "黄金分割", "对角线")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择网格类型")
        builder.setItems(options) { dialog, which ->
            tvGridType.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }

    // 显示分辨率选择对话框
    private fun showResolutionDialog() {
        val options = arrayOf("720p", "1080p", "4K")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择分辨率")
        builder.setItems(options) { dialog, which ->
            tvResolution.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }

    // 显示帧率选择对话框
    private fun showFrameRateDialog() {
        val options = arrayOf("30fps", "60fps", "120fps")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择帧率")
        builder.setItems(options) { dialog, which ->
            tvFrameRate.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }

    // 显示摄像头类型选择对话框
    private fun showCameraTypeDialog() {
        val options = arrayOf("后置", "前置")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择摄像头")
        builder.setItems(options) { dialog, which ->
            tvCameraType.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }

    // 显示分析频率选择对话框
    private fun showAnalysisFreqDialog() {
        val options = arrayOf("低", "中", "高")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择分析频率")
        builder.setItems(options) { dialog, which ->
            tvAnalysisFreq.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }

    // 显示建议详细程度选择对话框
    private fun showSuggestionDetailDialog() {
        val options = arrayOf("简洁", "详细", "专业")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择建议详细程度")
        builder.setItems(options) { dialog, which ->
            tvSuggestionDetail.text = options[which]
            dialog.dismiss()
        }
        builder.show()
    }
}

// 相机配置详情页面
class CameraConfigDetailActivity : AppCompatActivity() {
    private lateinit var cameraConfigManager: CameraConfigManager
    private lateinit var spinnerCamera: Spinner
    private lateinit var spinnerLens: Spinner
    private lateinit var tvRecommendedParams: TextView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_config_detail)

        cameraConfigManager = CameraConfigManager(this)
        initViews()
        setupSpinners()
        loadCurrentConfig()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        spinnerCamera = findViewById(R.id.spinnerCamera)
        spinnerLens = findViewById(R.id.spinnerLens)
        tvRecommendedParams = findViewById(R.id.tvRecommendedParams)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupSpinners() {
        // 相机预设
        val cameraPresets = cameraConfigManager.getCameraPresets()
        val cameraNames = cameraPresets.map { it.cameraModel }
        val cameraAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cameraNames)
        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCamera.adapter = cameraAdapter

        // 镜头预设
        val lensPresets = cameraConfigManager.getLensPresets()
        val lensNames = lensPresets.map { it.lensModel }
        val lensAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lensNames)
        lensAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLens.adapter = lensAdapter
    }

    private fun loadCurrentConfig() {
        val config = cameraConfigManager.getCurrentConfig()
        
        // 加载相机和镜头选择
        val cameraPresets = cameraConfigManager.getCameraPresets()
        val cameraIndex = cameraPresets.indexOfFirst { it.cameraModel == config.cameraSettings.cameraModel }
        if (cameraIndex >= 0) {
            spinnerCamera.setSelection(cameraIndex)
        }

        val lensPresets = cameraConfigManager.getLensPresets()
        val lensIndex = lensPresets.indexOfFirst { it.lensModel == config.lensSettings.lensModel }
        if (lensIndex >= 0) {
            spinnerLens.setSelection(lensIndex)
        }

        // 更新推荐参数显示
        updateRecommendedParams()
    }
    
    // 更新推荐参数显示
    private fun updateRecommendedParams() {
        val config = cameraConfigManager.getCurrentConfig()
        if (config.cameraSettings.cameraModel.isNotEmpty() && config.lensSettings.lensModel.isNotEmpty()) {
            tvRecommendedParams.text = "📷 推荐参数\n\n" +
                    "相机: ${config.cameraSettings.cameraModel}\n" +
                    "镜头: ${config.lensSettings.lensModel}\n\n" +
                    "基于当前构图，系统将自动生成最佳曝光参数\n\n" +
                    "• 焦距: ${config.lensSettings.focalLengthMin}mm - ${config.lensSettings.focalLengthMax}mm\n" +
                    "• 光圈范围: f/${config.lensSettings.apertureMin} - f/${config.lensSettings.apertureMax}"
        } else {
            tvRecommendedParams.text = "请选择相机和镜头型号，系统将根据构图情况自动生成推荐参数"
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnSave.setOnClickListener {
            saveConfig()
        }
        
        // 相机选择变化监听
        spinnerCamera.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateRecommendedParams()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
        
        // 镜头选择变化监听
        spinnerLens.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateRecommendedParams()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }

    private fun saveConfig() {
        try {
            // 获取相机和镜头选择
            val cameraPresets = cameraConfigManager.getCameraPresets()
            val selectedCamera = cameraPresets[spinnerCamera.selectedItemPosition]
            
            val lensPresets = cameraConfigManager.getLensPresets()
            val selectedLens = lensPresets[spinnerLens.selectedItemPosition]

            // 保存配置
            cameraConfigManager.saveCameraSettings(selectedCamera)
            cameraConfigManager.saveLensSettings(selectedLens)

            Toast.makeText(this, "配置保存成功", Toast.LENGTH_SHORT).show()
            
            // 返回上一页
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}