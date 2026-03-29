import os
import tensorflow as tf
import numpy as np

# 模型文件列表
MODEL_FILES = [
    'mobilenet_v3_small.tflite',
    'yolo_v8n_int8.tflite'
]

def validate_model(model_path):
    """全面验证模型"""
    print(f"\n🔍 验证模型: {model_path}")
    print("-" * 60)
    
    try:
        # 1. 检查文件存在性
        if not os.path.exists(model_path):
            print(f"❌ 文件不存在: {model_path}")
            return False
        
        # 2. 检查文件大小
        file_size = os.path.getsize(model_path)
        print(f"📁 文件大小: {file_size:,} 字节 ({file_size/1024/1024:.2f} MB)")
        
        # 3. 检查文件修改时间
        import datetime
        mod_time = os.path.getmtime(model_path)
        mod_time_str = datetime.datetime.fromtimestamp(mod_time).strftime('%Y-%m-%d %H:%M:%S')
        print(f"🕒 修改时间: {mod_time_str}")
        
        # 4. 加载模型
        print("🚀 加载模型...")
        interpreter = tf.lite.Interpreter(model_path=model_path)
        interpreter.allocate_tensors()
        print("✅ 模型加载成功")
        
        # 5. 获取模型信息
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        
        print(f"📊 模型信息:")
        print(f"   输入数量: {len(input_details)}")
        for i, input_detail in enumerate(input_details):
            print(f"   输入 {i}:")
            print(f"     形状: {input_detail['shape']}")
            print(f"     类型: {input_detail['dtype']}")
            print(f"     名称: {input_detail.get('name', 'N/A')}")
        
        print(f"   输出数量: {len(output_details)}")
        for i, output_detail in enumerate(output_details):
            print(f"   输出 {i}:")
            print(f"     形状: {output_detail['shape']}")
            print(f"     类型: {output_detail['dtype']}")
            print(f"     名称: {output_detail.get('name', 'N/A')}")
        
        # 6. 进行推理测试
        print("🧪 进行推理测试...")
        
        # 为每个输入创建随机数据
        input_data = []
        for input_detail in input_details:
            input_shape = input_detail['shape']
            input_dtype = input_detail['dtype']
            
            # 创建随机输入数据
            if input_dtype == np.float32:
                data = np.random.rand(*input_shape).astype(np.float32)
            elif input_dtype == np.int8:
                data = np.random.randint(-128, 127, size=input_shape, dtype=np.int8)
            else:
                data = np.random.rand(*input_shape).astype(input_dtype)
            
            input_data.append(data)
            interpreter.set_tensor(input_detail['index'], data)
        
        # 执行推理
        interpreter.invoke()
        print("✅ 推理执行成功")
        
        # 获取输出
        outputs = []
        for i, output_detail in enumerate(output_details):
            output = interpreter.get_tensor(output_detail['index'])
            outputs.append(output)
            print(f"   输出 {i} 形状: {output.shape}")
            print(f"   输出 {i} 类型: {output.dtype}")
            print(f"   输出 {i} 示例值: {output.flatten()[:5]}...")
        
        print("\n🎉 模型验证通过！")
        return True
        
    except Exception as e:
        print(f"❌ 验证失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def main():
    print("🎯 开始全面验证TFLite模型")
    print("=" * 60)
    
    success_count = 0
    total_count = len(MODEL_FILES)
    
    for model_file in MODEL_FILES:
        if validate_model(model_file):
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"📋 验证结果: {success_count}/{total_count} 个模型验证通过")
    
    if success_count == total_count:
        print("🎊 所有模型验证通过！可以放心使用。")
    else:
        print("⚠️  部分模型验证失败，需要检查。")

if __name__ == "__main__":
    main()