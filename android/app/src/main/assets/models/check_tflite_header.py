import os

def check_tflite_header(file_path):
    """检查TFLite文件头部是否正确"""
    try:
        with open(file_path, 'rb') as f:
            # 读取文件前16字节
            header = f.read(16)
            
        print(f"\n检查文件: {file_path}")
        print("-" * 50)
        
        # 打印十六进制表示
        hex_header = ' '.join(f'{b:02X}' for b in header)
        print(f"文件头部 (前16字节): {hex_header}")
        
        # TFLite文件的魔术数字是 'TFL3' (0x54 0x46 0x4C 0x33)
        tflite_magic = b'TFL3'
        
        # 检查前4字节后是否包含TFLite魔术数字
        if len(header) >= 8 and header[4:8] == tflite_magic:
            print("✅ 文件头部正确: 从第5字节开始包含TFLite魔术数字 'TFL3'")
            print(f"   前4字节: {header[:4].hex()}")
            print(f"   TFLite魔术数字位置: 第5-8字节")
            return True
        elif header.startswith(tflite_magic):
            print("✅ 文件头部正确: 包含TFLite魔术数字 'TFL3'")
            return True
        else:
            print("❌ 文件头部错误: 缺少TFLite魔术数字")
            print(f"   期望: TFL3 (54 46 4C 33)")
            print(f"   实际前8字节: {header[:8].hex()}")
            return False
            
    except Exception as e:
        print(f"❌ 检查失败: {str(e)}")
        return False

def main():
    model_files = [
        'mobilenet_v3_small.tflite',
        'yolo_v8n_int8.tflite'
    ]
    
    print("🔍 检查TFLite文件头部")
    print("=" * 50)
    
    for model_file in model_files:
        check_tflite_header(model_file)
    
    print("\n" + "=" * 50)
    print("检查完成")

if __name__ == "__main__":
    main()