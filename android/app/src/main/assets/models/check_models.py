import os

# 模型文件列表
MODEL_FILES = [
    'nima_mobilenet.tflite',
    'mobilenet_v3_small.tflite',
    'yolo_v8n_int8.tflite'
]

# 模型文件大小阈值（字节）
MODEL_SIZE_THRESHOLDS = {
    'nima_mobilenet.tflite': 10 * 1024 * 1024,  # 10MB
    'mobilenet_v3_small.tflite': 5 * 1024 * 1024,  # 5MB
    'yolo_v8n_int8.tflite': 1.5 * 1024 * 1024  # 1.5MB
}

def check_model(model_file):
    """检查模型文件是否存在且大小合理"""
    try:
        # 检查文件是否存在
        if not os.path.exists(model_file):
            print(f"❌ 模型文件不存在: {model_file}")
            return False
        
        # 检查文件大小
        file_size = os.path.getsize(model_file)
        threshold = MODEL_SIZE_THRESHOLDS.get(model_file, 0)
        
        print(f"📁 模型文件: {model_file}")
        print(f"   大小: {file_size:,} 字节")
        
        if file_size >= threshold:
            print(f"✅ 大小符合要求 (阈值: {threshold:,} 字节)")
            return True
        else:
            print(f"⚠️  大小可能不足 (阈值: {threshold:,} 字节)")
            return False
    except Exception as e:
        print(f"❌ 检查失败: {str(e)}")
        return False

def main():
    print("开始检查AI模型文件...")
    print("=" * 60)
    
    success_count = 0
    total_count = len(MODEL_FILES)
    
    for model_file in MODEL_FILES:
        print(f"\n检查模型: {model_file}")
        print("-" * 40)
        if check_model(model_file):
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"检查完成: {success_count}/{total_count} 个模型文件检查通过")
    
    if success_count == total_count:
        print("🎉 所有模型文件检查通过！")
        print("📱 模型已准备就绪，可以在Android应用中使用")
    else:
        print("⚠️  部分模型文件检查失败，请检查")

if __name__ == "__main__":
    main()