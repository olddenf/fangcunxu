import os
import tensorflow as tf

# 定义模型路径
mobilenet_onnx = "mobilenetv3_small.onnx"
yolo_onnx = "yolov8n_onnx_tensorRT_rknn_horizon/yolov8_onnx/yolov8n_ZQ.onnx"

# 定义输出路径
mobilenet_tflite = "app/src/main/assets/models/mobilenet_v3_small.tflite"
yolo_tflite = "app/src/main/assets/models/yolo_v8n_int8.tflite"

print(f"TensorFlow version: {tf.__version__}")

def convert_onnx_to_tflite(onnx_path, tflite_path, model_name):
    """使用TensorFlow的ONNX导入功能转换模型"""
    print(f"\n=== 开始转换 {model_name} ===")
    print(f"输入文件: {onnx_path}")
    print(f"输出文件: {tflite_path}")
    
    try:
        # 加载ONNX模型
        print("加载ONNX模型...")
        import onnx
        onnx_model = onnx.load(onnx_path)
        print(f"ONNX模型加载成功，版本: {onnx.__version__}")
        
        # 转换为TensorFlow SavedModel
        print("转换为TensorFlow模型...")
        # 使用tf.saved_model.load加载ONNX模型
        # 注意：这需要TensorFlow 2.10+的版本
        from tensorflow.python.saved_model import load
        from tensorflow.python.onnx import onnx_import
        
        # 创建临时目录
        import tempfile
        import shutil
        with tempfile.TemporaryDirectory() as temp_dir:
            # 导出为SavedModel
            saved_model_dir = os.path.join(temp_dir, f"{model_name}_saved_model")
            
            # 尝试使用onnx-tf
            try:
                from onnx_tf.backend import prepare
                tf_rep = prepare(onnx_model)
                tf_rep.export_graph(saved_model_dir)
                print(f"使用onnx-tf转换成功")
            except Exception as e:
                print(f"onnx-tf转换失败: {e}")
                print("尝试使用tf-onnx导入...")
                
                # 尝试使用tf-onnx
                import tf2onnx
                # 这里需要先将ONNX转换为TensorFlow图
                # 这部分可能需要更复杂的处理
                print("tf-onnx导入需要更复杂的处理")
                return False
        
        # 转换为TFLite
        print("转换为TFLite...")
        converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        
        # 启用量化
        try:
            converter.target_spec.supported_types = [tf.float16]
            print("启用float16量化")
        except:
            print("使用默认float32格式")
        
        tflite_model = converter.convert()
        
        # 保存TFLite模型
        os.makedirs(os.path.dirname(tflite_path), exist_ok=True)
        with open(tflite_path, "wb") as f:
            f.write(tflite_model)
        
        file_size = os.path.getsize(tflite_path) / (1024 * 1024)
        print(f"TFLite模型保存成功: {tflite_path}")
        print(f"模型大小: {file_size:.2f} MB")
        
        # 验证TFLite模型
        print("验证TFLite模型...")
        interpreter = tf.lite.Interpreter(model_path=tflite_path)
        interpreter.allocate_tensors()
        print("TFLite模型验证成功")
        
        return True
        
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