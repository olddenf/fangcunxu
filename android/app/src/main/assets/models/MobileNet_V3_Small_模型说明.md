# MobileNet V3 Small 物体检测模型说明

## 1. 模型基本信息

### 1.1 核心信息
- **模型名称**: SSD MobileNet V3 Small COCO
- **模型类型**: 物体检测 (Object Detection)
- **训练数据集**: COCO 2017
- **模型架构**: Single Shot MultiBox Detector (SSD) + MobileNet V3 Small
- **文件格式**: TensorFlow Lite (.tflite)
- **模型大小**: 6.9 MB
- **下载时间**: 2026-03-28

### 1.2 关键特性
- **优化目标**: 移动端部署
- **网络架构**: 深度可分离卷积 + Squeeze-and-Excitation (SE) 模块 + Hard-Swish 激活函数
- **设计方法**: 神经网络架构搜索 (NAS) + NetAdapt 算法
- **参数量**: 约 2.5M

## 2. 技术规格

### 2.1 输入规格
| 参数 | 值 | 说明 |
|------|-----|------|
| 输入名称 | normalized_input_image_tensor | 归一化输入图像张量 |
| 输入形状 | [1, 320, 320, 3] | 批次大小 × 高度 × 宽度 × 通道数 |
| 输入范围 | [0, 255] → 归一化到 [-1, 1] 或 [0, 1] | 取决于具体预处理 |
| 颜色顺序 | RGB | 标准RGB顺序 |

### 2.2 输出规格
| 输出索引 | 输出名称 | 形状 | 说明 |
|----------|-----------|------|------|
| 0 | detection_boxes | [1, 10, 4] | 检测框坐标 (y_min, x_min, y_max, x_max) |
| 1 | detection_classes | [1, 10] | 检测类别索引 (0-90) |
| 2 | detection_scores | [1, 10] | 检测置信度分数 (0-1) |
| 3 | num_detections | [1] | 有效检测数量 |

**注意**: 输出形状中的 "10" 表示最大检测数，实际有效检测数量由 `num_detections` 决定。

### 2.3 性能指标
| 指标 | 值 | 说明 |
|------|-----|------|
| COCO mAP | ~15.4 | 在COCO验证集上的平均精度 |
| Pixel 1 延迟 | ~43ms | 在Pixel 1设备上的推理时间 |
| 参数量 | ~2.5M | 模型参数总量 |
| FLOPs | ~0.6B | 浮点运算次数 |

## 3. 模型来源与下载信息

### 3.1 官方来源
- **原始发布**: TensorFlow Detection Model Zoo
- **下载链接**: `http://download.tensorflow.org/models/object_detection/ssd_mobilenet_v3_small_coco_2020_01_14.tar.gz`
- **发布日期**: 2020年1月14日
- **官方文档**: [TensorFlow Detection Model Zoo](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/tf1_detection_zoo.md)

### 3.2 文件清单
| 文件路径 | 大小 | 描述 |
|----------|------|------|
| `data/models/mobilenet_v3_small.tflite` | 6.9 MB | 优化后的TensorFlow Lite模型文件 |
| `data/models/mobilenet_v3_labels.txt` | 665 B | COCO数据集91类别标签文件 |
| `data/models/ssd_mobilenet_v3_small_coco_2020_01_14.tar.gz` | 27 MB | 原始压缩包 |
| `data/models/ssd_mobilenet_v3_small_coco_2020_01_14/` | - | 解压后的完整模型文件 |

### 3.3 原始压缩包内容
```
ssd_mobilenet_v3_small_coco_2020_01_14/
├── checkpoint                  # 检查点文件
├── frozen_inference_graph.pb   # 冻结的推理图 (7.2 MB)
├── model.ckpt.data-00000-of-00001  # 模型权重数据 (13.7 MB)
├── model.ckpt.index            # 权重索引文件
├── model.ckpt.meta             # 模型元数据
└── pipeline.config             # 训练配置参数
```

## 4. 类别标签说明

### 4.1 标签文件格式
- **文件**: `data/models/mobilenet_v3_labels.txt`
- **编码**: UTF-8
- **行数**: 92行 (包含91个类别 + 1个背景类别)
- **格式**: 每行一个类别名称，索引从0开始

### 4.2 主要类别示例
| 类别索引 | 类别名称 | 说明 |
|----------|-----------|------|
| 1 | person | 人 |
| 2 | bicycle | 自行车 |
| 3 | car | 汽车 |
| 4 | motorcycle | 摩托车 |
| 5 | airplane | 飞机 |
| 6 | bus | 公交车 |
| 7 | train | 火车 |
| 8 | truck | 卡车 |
| 9 | boat | 船 |
| 10 | traffic light | 交通信号灯 |
| 11 | fire hydrant | 消防栓 |

**注意**: 索引0对应 "???" 表示背景或未知类别。

## 5. 集成使用要点

### 5.1 预处理步骤
```python
# 示例预处理代码
def preprocess_image(image, input_size=320):
    # 1. 调整大小到320x320
    resized_image = cv2.resize(image, (input_size, input_size))
    
    # 2. 转换为浮点数并归一化到[0,1]或[-1,1]
    normalized_image = resized_image.astype(np.float32) / 255.0
    
    # 3. 添加批次维度
    input_tensor = np.expand_dims(normalized_image, axis=0)
    
    return input_tensor
```

### 5.2 后处理步骤
```python
# 示例后处理代码
def postprocess_detections(outputs, score_threshold=0.5):
    boxes = outputs['detection_boxes'][0]
    classes = outputs['detection_classes'][0]
    scores = outputs['detection_scores'][0]
    num_detections = int(outputs['num_detections'][0])
    
    # 过滤低置信度的检测
    valid_indices = scores[:num_detections] > score_threshold
    filtered_boxes = boxes[:num_detections][valid_indices]
    filtered_classes = classes[:num_detections][valid_indices]
    filtered_scores = scores[:num_detections][valid_indices]
    
    return filtered_boxes, filtered_classes, filtered_scores
```

### 5.3 Android集成建议
1. **模型放置**: 将 `mobilenet_v3_small.tflite` 放入 `app/src/main/assets/` 目录
2. **标签文件**: 将 `mobilenet_v3_labels.txt` 放入相同目录
3. **推理引擎**: 使用TensorFlow Lite Task Library的 `ObjectDetector`
4. **性能优化**: 考虑使用GPU或NNAPI委托加速

## 6. 模型对比与选择建议

### 6.1 与MobileNet V1对比
| 特性 | MobileNet V1 | MobileNet V3 Small | 优势 |
|------|--------------|---------------------|------|
| 推出时间 | 2017年 | 2019年 | 更新技术 |
| 参数量 | ~4.2M | ~2.5M | 减少40% |
| 模型大小 | 4.2 MB | 6.9 MB | 稍大但精度更高 |
| COCO mAP | ~21-22 | ~15.4 | V1精度更高 |
| 推理速度 | 快 | 更快 | V3延迟更低 |
| 能效比 | 一般 | 更优 | 专为移动端优化 |

### 6.2 适用场景建议
- **推荐MobileNet V3 Small**:
  - 对推理速度要求极高的实时应用
  - 资源极度受限的移动设备
  - 需要平衡精度与速度的场景
  - 辅助构图工具等轻量级应用

- **考虑MobileNet V1**:
  - 对检测精度要求更高的应用
  - 设备资源相对充足
  - 对模型大小敏感的场景

## 7. 验证与测试

### 7.1 模型验证方法
```bash
# 使用TensorFlow Lite解释器验证模型
python3 -c "
import tensorflow as tf
import numpy as np

# 加载模型
interpreter = tf.lite.Interpreter(model_path='data/models/mobilenet_v3_small.tflite')
interpreter.allocate_tensors()

# 获取输入输出详细信息
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print('输入详情:', input_details)
print('输出详情:', output_details)

# 创建虚拟输入
input_shape = input_details[0]['shape']
dummy_input = np.random.random(input_shape).astype(np.float32)

# 运行推理
interpreter.set_tensor(input_details[0]['index'], dummy_input)
interpreter.invoke()

# 获取输出
output_data = interpreter.get_tensor(output_details[0]['index'])
print('模型加载成功，输出形状:', output_data.shape)
"
```

### 7.2 预期结果
- 模型应能成功加载，无错误
- 输入形状应为 `[1, 320, 320, 3]`
- 输出应包含四个张量：检测框、类别、分数、数量
- 推理时间应在移动设备上小于50ms

## 8. 注意事项

1. **归一化处理**: 模型输入需要归一化处理，具体方式需参考原始配置
2. **输出解析**: 检测框坐标为归一化坐标 (y_min, x_min, y_max, x_max)
3. **类别索引**: 类别索引从0开始，对应标签文件中的行号
4. **性能权衡**: MobileNet V3 Small在精度上有所牺牲以换取更快速度
5. **部署环境**: 建议在实际目标设备上进行性能测试

## 9. 更新历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0 | 2026-03-28 | 初始版本，包含模型基本信息和技术规格 |
| 1.1 | 2026-03-28 | 添加集成使用要点和验证方法 |

---

**最后更新**: 2026年03月28日  
**维护者**: AI Worker Agent  
**文件位置**: `docs/MobileNet_V3_Small_模型说明.md`