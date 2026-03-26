# 下载 AI 模型文件的 PowerShell 命令

# 进入模型目录
cd "D:\Programs\tool\fangcunxu\android\app\src\main\assets\models"

# 1. 下载 NIMA MobileNet 模型 (构图评分)
Write-Host "正在下载 NIMA MobileNet 模型..."
Invoke-WebRequest -Uri "https://github.com/tensorflow/models/raw/master/research/aura/nima/tflite/nima_mobilenet.tflite" -OutFile "nima_mobilenet.tflite"

# 2. 下载 MobileNet V3 Small 模型 (场景识别)
Write-Host "正在下载 MobileNet V3 Small 模型..."
Invoke-WebRequest -Uri "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v3_small_100_224.tflite" -OutFile "mobilenet_v3_small.tflite"

# 3. 下载 YOLOv8n INT8 模型 (目标检测)
Write-Host "正在下载 YOLOv8n INT8 模型..."
Invoke-WebRequest -Uri "https://github.com/ultralytics/assets/releases/download/v8.1.0/yolov8n_int8.tflite" -OutFile "yolo_v8n_int8.tflite"

# 验证下载结果
Write-Host "\n下载完成，文件列表："
get-childitem | select-object Name, Length

Write-Host "\n模型文件已下载到：D:\Programs\tool\fangcunxu\android\app\src\main\assets\models\"
