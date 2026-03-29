#!/usr/bin/env python3
"""
验证AI引擎配置和模型文件
"""

import os
import sys

# 检查模型文件是否存在
def check_model_files():
    # 直接在当前目录检查
    model_files = [
        'mobilenet_v3_small.tflite',
        'yolo_v8n_int8.tflite'
    ]
    
    print("🔍 检查模型文件...")
    print("=" * 50)
    
    all_exist = True
    for model_file in model_files:
        if os.path.exists(model_file):
            size = os.path.getsize(model_file)
            print(f"✅ {model_file} - {size:,} 字节")
        else:
            print(f"❌ {model_file} - 不存在")
            all_exist = False
    
    print("=" * 50)
    return all_exist

# 检查AI引擎配置
def check_ai_engine_config():
    print("🔧 检查AI引擎配置...")
    print("=" * 50)
    
    # 检查Kotlin文件是否存在
    kotlin_files = [
        'AIEngine.kt',
        'SceneRecognitionModel.kt',
        'ObjectDetectionModel.kt',
        'CompositionRuleEngine.kt',
        'OverallScoreCalculator.kt',
        'AIEngineTest.kt'
    ]
    
    ai_dir = os.path.join('..', '..', '..', '..', 'kotlin', 'com', 'example', 'fangcunxu', 'ai')
    
    all_exist = True
    for kotlin_file in kotlin_files:
        file_path = os.path.join(ai_dir, kotlin_file)
        if os.path.exists(file_path):
            print(f"✅ {kotlin_file} - 存在")
        else:
            print(f"❌ {kotlin_file} - 不存在")
            all_exist = False
    
    print("=" * 50)
    return all_exist

# 主函数
def main():
    print("🎯 验证AI模型推理层配置")
    print("=" * 60)
    
    model_check = check_model_files()
    config_check = check_ai_engine_config()
    
    print("\n📋 验证结果")
    print("=" * 60)
    
    if model_check and config_check:
        print("🎉 所有检查通过！AI模型推理层配置完成")
        print("\n📌 已实现的组件:")
        print("  • MobileNet V3 (场景识别)")
        print("  • YOLOv8n (目标检测)")
        print("  • 构图评分引擎 (规则引擎)")
        print("  • AI引擎测试工具")
    else:
        print("⚠️  部分检查失败，请检查配置")
    
    print("=" * 60)

if __name__ == "__main__":
    main()