package com.example.fangcunxu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fangcunxu.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置点击事件
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 语言与地区
        binding.languageSetting.setOnClickListener {
            Toast.makeText(this, "语言与地区设置", Toast.LENGTH_SHORT).show()
        }

        // 引导线控制
        binding.guidelineSetting.setOnClickListener {
            showGuidelineOptions()
        }

        // 默认相机
        binding.defaultCameraSetting.setOnClickListener {
            Toast.makeText(this, "默认相机设置", Toast.LENGTH_SHORT).show()
        }

        // 默认镜头
        binding.defaultLensSetting.setOnClickListener {
            Toast.makeText(this, "默认镜头设置", Toast.LENGTH_SHORT).show()
        }

        // 实时构图评分系统
        binding.realtimeScoreToggle.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "实时构图评分系统：${if (isChecked) "开启" else "关闭"}", Toast.LENGTH_SHORT).show()
        }

        // 意境色彩识别引擎
        binding.colorEngineSetting.setOnClickListener {
            Toast.makeText(this, "意境色彩识别引擎设置", Toast.LENGTH_SHORT).show()
        }

        // AI 场景优化
        binding.aiSceneToggle.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "AI 场景优化：${if (isChecked) "开启" else "关闭"}", Toast.LENGTH_SHORT).show()
        }

        // 版本号
        binding.versionSetting.setOnClickListener {
            Toast.makeText(this, "已是最新版本", Toast.LENGTH_SHORT).show()
        }

        // 用户许可协议
        binding.userAgreementSetting.setOnClickListener {
            Toast.makeText(this, "用户许可协议", Toast.LENGTH_SHORT).show()
        }

        // 隐私政策
        binding.privacyPolicySetting.setOnClickListener {
            Toast.makeText(this, "隐私政策", Toast.LENGTH_SHORT).show()
        }

        // 底部导航
        binding.navGallery.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.navCamera.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.navSettings.setOnClickListener {
            // 当前页面
        }
    }

    private fun showGuidelineOptions() {
        val options = arrayOf("三分法", "黄金分割", "黄金螺旋", "对角线", "关闭")
        android.app.AlertDialog.Builder(this)
            .setTitle("选择引导线类型")
            .setItems(options) { _, which ->
                binding.guidelineValue.text = options[which]
                Toast.makeText(this, "已选择：${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
