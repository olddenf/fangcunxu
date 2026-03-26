# 创建一个简单的 TFLite 模型文件
import numpy as np
import tensorflow as tf

# 创建一个简单的模型
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(224, 224, 3)),
    tf.keras.layers.Conv2D(32, (3, 3), activation='relu'),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(10, activation='softmax')
])

# 编译模型
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# 保存为 TFLite 模型
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# 保存模型文件
with open('nima_mobilenet.tflite', 'wb') as f:
    f.write(tflite_model)

with open('mobilenet_v3_small.tflite', 'wb') as f:
    f.write(tflite_model)

with open('yolo_v8n_int8.tflite', 'wb') as f:
    f.write(tflite_model)

print("模型文件创建成功！")