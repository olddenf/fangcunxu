import os
import tensorflow as tf

# 模型文件列表
MODEL_FILES = [
    'nima_mobilenet.tflite',
    'mobilenet_v3_small.tflite',
    'yolo_v8n_int8.tflite'
]

def verify_model(model_path):
    """验证模型是否可以被TensorFlow Lite加载"""
    try:
        # 检查文件是否存在
        if not os.path.exists(model_path):
            print(f"❌ 模型文件不存在: {model_path}")
            return False
        
        # 检查文件大小
        file_size = os.path.getsize(model_path)
        print(f"📁 模型文件: {model_path} ({file_size:,} 字节)")
        
        # 尝试加载模型
        interpreter = tf.lite.Interpreter(model_path=model_path)
        interpreter.allocate_tensors()
        
        # 获取输入输出信息
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        
        print(f"✅ 模型加载成功")
        print(f"   输入: {len(input_details)} 个输入")
        for i, input_detail in enumerate(input_details):
            print(f"   输入 {i}: 形状={input_detail['shape']}, 类型={input_detail['dtype']}")
        print(f"   输出: {len(output_details)} 个输出")
        for i, output_detail in enumerate(output_details):
            print(f"   输出 {i}: 形状={output_detail['shape']}, 类型={output_detail['dtype']}")
        
        return True
    except Exception as e:
        print(f"❌ 模型验证失败: {str(e)}")
        return False

def main():
    print("开始验证AI模型...")
    print("=" * 60)
    
    success_count = 0
    total_count = len(MODEL_FILES)
    
    for model_file in MODEL_FILES:
        print(f"\n验证模型: {model_file}")
        print("-" * 40)
        if verify_model(model_file):
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"验证完成: {success_count}/{total_count} 个模型验证通过")
    
    if success_count == total_count:
        print("🎉 所有模型验证通过！")
    else:
        print("⚠️  部分模型验证失败，请检查")

if __name__ == "__main__":
    main()