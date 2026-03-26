import requests
import os

# 下载NIMA模型（PyTorch格式）
nima_url = "https://github.com/yu4u/nima.pytorch/releases/download/v1.0/weights.pth"
response = requests.get(nima_url)
with open("nima_weights.pth", "wb") as f:
    f.write(response.content)

# 下载MobileNet V3模型（TensorFlow格式）
mobilenet_url = "https://storage.googleapis.com/tfhub-modules/tensorflow/mobilenet_v3_small_100_224/1.tar.gz"
response = requests.get(mobilenet_url)
with open("mobilenet_v3_small.tar.gz", "wb") as f:
    f.write(response.content)

# 下载YOLOv8n模型（PyTorch格式）
yolo_url = "https://github.com/ultralytics/assets/releases/download/v8.1.0/yolov8n.pt"
response = requests.get(yolo_url)
with open("yolov8n.pt", "wb") as f:
    f.write(response.content)