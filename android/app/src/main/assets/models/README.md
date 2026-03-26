# AI 模型文件说明

本目录用于存放 TensorFlow Lite 模型文件。

## 需要的模型文件

请将以下三个模型文件放入此目录：

### 1. NIMA MobileNet (构图评分)
- **文件名**: `nima_mobilenet.tflite`
- **大小**: ~4.2MB
- **用途**: 美学评分 (1-10 分)
- **输入**: 224x224 RGB 图像
- **输出**: 10 维评分分布

**下载方式**:
```bash
# 从 Google Research 官方 GitHub 下载
wget https://github.com/tensorflow/models/raw/master/research/aura/nima/tflite/nima_mobilenet.tflite
```

### 2. MobileNet V3 Small (场景识别)
- **文件名**: `mobilenet_v3_small.tflite`
- **大小**: ~5MB
- **用途**: 场景分类 (10 类)
- **输入**: 224x224 RGB 图像
- **输出**: 10 类概率分布

**下载方式**:
```bash
# 从 TensorFlow Hub 下载并转换
# 或使用预转换的 TFLite 模型
wget https://tfhub.dev/tensorflow/lite-model/mobilenet_v3_small_100_224/1/metadata/1?tf-hub-format=compressed
```

### 3. YOLOv8n INT8 (目标检测)
- **文件名**: `yolo_v8n_int8.tflite`
- **大小**: ~6MB
- **用途**: 物体检测 (COCO 80 类)
- **输入**: 640x640 RGB 图像
- **输出**: 检测框列表

**下载方式**:
```bash
# 使用 Ultralytics 导出
from ultralytics import YOLO
model = YOLO('yolov8n.pt')
model.export(format='tflite', int8=True)
```

## 目录结构

```
app/src/main/assets/
└── models/
    ├── nima_mobilenet.tflite      # NIMA 评分模型
    ├── mobilenet_v3_small.tflite  # 场景识别模型
    └── yolo_v8n_int8.tflite       # 目标检测模型
```

## 验证模型

启动应用后，AI 引擎会自动加载这些模型。如果模型加载失败，会在 Logcat 中显示错误信息。

## 注意事项

1. **不要提交模型文件到 Git** - 模型文件较大，应使用 Git LFS 或在 .gitignore 中忽略
2. **确保模型格式正确** - 必须是 TFLite 格式 (.tflite)
3. **检查模型输入输出** - 确保与代码中的预处理逻辑匹配
