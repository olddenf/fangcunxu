package com.example.fangcunxu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        
        // 设置按钮点击事件
        // 画卷按钮
        findViewById<View>(R.id.navGallery).setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        // 拍照按钮
        findViewById<View>(R.id.btnCapture).setOnClickListener {
            // 模拟拍照效果
            val flashEffect = findViewById<View>(R.id.flashEffect)
            flashEffect.alpha = 0.5f
            flashEffect.postDelayed({ flashEffect.alpha = 0f }, 200)
        }
        
        // 相机选择按钮
        findViewById<View>(R.id.btnCameraSelect).setOnClickListener {
            Toast.makeText(this, "相机选择功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 镜头切换按钮
        findViewById<View>(R.id.btnSwitchCamera).setOnClickListener {
            Toast.makeText(this, "镜头切换功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        // 检查相机权限
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // 相机权限已授予
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 相机权限已授予
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
