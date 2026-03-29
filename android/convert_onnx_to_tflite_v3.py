import os
import tensorflow as tf
import onnx

# 定义模型路径
mobilenet_onnx = "mobilenetv3_small.onnx"
yolo_onnx = "yolov8n_onnx_tensorRT_rknn_horizon/yolov8_onnx/yolov8n_ZQ.onnx"

# 定义输出路径
mobilenet_tflite = "app/src/main/assets/models/mobilenet_v3_small.tflite"
yolo_tflite = "app/src/main/assets/models/yolo_v8n_int8.tflite"

print(f"TensorFlow version: {tf.__version__}")
print(f"ONNX version: {onnx.__version__}")

def convert_onnx_to_tflite(onnx_path, tflite_path, model_name):
    """将ONNX模型转换为TFLite格式"""
    print(f"\n=== 开始转换 {model_name} ===")
    print(f"输入文件: {onnx_path}")
    print(f"输出文件: {tflite_path}")
    
    try:
        # 加载ONNX模型
        print("加载ONNX模型...")
        onnx_model = onnx.load(onnx_path)
        print(f"ONNX模型加载成功，版本: {onnx.__version__}")
        
        # 检查模型输入输出
        print("检查模型输入输出...")
        for input in onnx_model.graph.input:
            print(f"输入: {input.name}, 形状: {input.type.tensor_type.shape}")
        for output in onnx_model.graph.output:
            print(f"输出: {output.name}, 形状: {output.type.tensor_type.shape}")
        
        # 尝试使用tf.experimental.tensorrt导入
        print("尝试使用TensorFlow直接导入...")
        
        # 创建输入张量
        import numpy as np
        input_shape = [1, 3, 224, 224]  # MobileNet V3输入形状
        if "yolo" in model_name.lower():
            input_shape = [1, 3, 640, 640]  # YOLOv8输入形状
        
        # 创建临时输入数据
        input_data = np.random.rand(*input_shape).astype(np.float32)
        
        # 尝试使用tf-onnx
        print("尝试使用tf2onnx...")
        import tf2onnx
        
        # 这里我们需要先将ONNX模型转换为TensorFlow图
        # 然后再转换为TFLite
        print("tf2onnx主要用于反向转换，不适合当前任务")
        
        # 尝试使用ONNX Runtime进行推理
        print("尝试使用ONNX Runtime验证模型...")
        import onnxruntime as rt
        sess = rt.InferenceSession(onnx_path)
        input_name = sess.get_inputs()[0].name
        output_name = sess.get_outputs()[0].name
        
        # 运行推理
        result = sess.run([output_name], {input_name: input_data})
        print(f"ONNX模型推理成功，输出形状: {result[0].shape}")
        
        # 现在尝试使用TensorFlow的ONNX导入
        print("尝试使用TensorFlow的ONNX导入...")
        
        # 注意：TensorFlow 2.10+支持直接导入ONNX模型
        # 但这需要特定的API
        try:
            # 尝试使用tf.saved_model.load加载ONNX模型
            # 这是一个实验性API
            print("尝试使用实验性ONNX导入API...")
            
            # 这里我们需要使用tf-onnx的转换功能
            # 或者使用其他方法
            print("当前环境下无法直接导入ONNX模型")
            
        except Exception as e:
            print(f"ONNX导入失败: {e}")
        
        print("转换失败：当前环境无法直接将ONNX模型转换为TFLite")
        return False
        
    except Exception as e:
        print(f"转换失败: {e}")
        import traceback
        traceback.print_exc()
        return False

# 转换MobileNet V3模型
if os.path.exists(mobilenet_onnx):
    convert_onnx_to_tflite(
        mobilenet_onnx, 
        mobilenet_tflite, 
        "mobilenet_v3"
    )
else:
    print(f"MobileNet V3 ONNX模型不存在: {mobilenet_onnx}")

# 转换YOLOv8模型
if os.path.exists(yolo_onnx):
    convert_onnx_to_tflite(
        yolo_onnx, 
        yolo_tflite, 
        "yolov8n"
    )
else:
    print(f"YOLOv8 ONNX模型不存在: {yolo_onnx}")

print("\n=== 转换完成 ===")