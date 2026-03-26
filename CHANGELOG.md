# 更新日志

## [0.5.0.1] - 2026-03-26

### 新增
- 装备应用所需的AI模型文件
  - NIMA MobileNet 模型 (10.5 MB) - 用于构图评分
  - MobileNet V3 Small 模型 (5.2 MB) - 用于场景识别
  - YOLOv8n INT8 模型 (5.2 MB) - 用于目标检测

### 说明
- 所有模型文件已下载并验证可用
- 模型文件位于 `android/app/src/main/assets/models/` 目录
- 应用现在可以使用本地AI进行实时构图评估

## [0.5.0] - 2026-03-26

### 初始版本
- 项目基础架构搭建
- CameraX 相机功能集成
- TensorFlow Lite 框架集成
