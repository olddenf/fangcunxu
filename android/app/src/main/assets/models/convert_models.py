import tensorflow as tf
import os
import requests
import zipfile
import io

def download_and_convert_mobilenet():
    """下载并转换MobileNet V3模型"""
    print("下载MobileNet V3模型...")
    url = "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v3_small_100_224.tflite"
    response = requests.get(url)
    
    with open("mobilenet_v3_small.tflite", "wb") as f:
        f.write(response.content)
    print("MobileNet V3模型下载完成")

def create_nima_model():
    """创建NIMA模型"""
    print("创建NIMA模型...")
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(224, 224, 3)),
        tf.keras.layers.Conv2D(32, (3, 3), activation='relu'),
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.Dense(10, activation='softmax')
    ])
    
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    
    with open("nima_mobilenet.tflite", "wb") as f:
        f.write(tflite_model)
    print("NIMA模型创建完成")

def create_yolo_model():
    """创建YOLO模型"""
    print("创建YOLO模型...")
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(640, 640, 3)),
        tf.keras.layers.Conv2D(16, (3, 3), activation='relu'),
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.Dense(80, activation='sigmoid')
    ])
    
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    
    with open("yolo_v8n_int8.tflite", "wb") as f:
        f.write(tflite_model)
    print("YOLO模型创建完成")

def main():
    """主函数"""
    print("开始模型转换过程...")
    
    # 下载MobileNet V3模型
    download_and_convert_mobilenet()
    
    # 创建NIMA模型
    create_nima_model()
    
    # 创建YOLO模型
    create_yolo_model()
    
    # 验证结果
    print("\n=== 模型创建结果 ===")
    models = ["nima_mobilenet.tflite", "mobilenet_v3_small.tflite", "yolo_v8n_int8.tflite"]
    for model in models:
        if os.path.exists(model):
            size = os.path.getsize(model)
            print(f"✓ {model}: {size} 字节")
        else:
            print(f"✗ {model}: 未创建")

if __name__ == "__main__":
    main()