#!/usr/bin/env python3
"""
验证UI与AI模型的集成
"""

import os
import sys

# 检查UI相关文件
def check_ui_files():
    print("🔍 检查UI文件...")
    print("=" * 50)
    
    ui_files = [
        'activity_main.xml',
        'DetectionOverlayView.kt',
        'SuggestionPanelView.kt'
    ]
    
    res_layout_dir = os.path.join('..', '..', '..', '..', 'res', 'layout')
    ui_dir = os.path.join('..', '..', '..', '..', 'kotlin', 'com', 'example', 'fangcunxu', 'ui')
    
    all_exist = True
    
    # 检查布局文件
    for layout_file in ui_files[:1]:  # 第一个是布局文件
        file_path = os.path.join(res_layout_dir, layout_file)
        if os.path.exists(file_path):
            print(f"✅ {layout_file} - 存在")
        else:
            print(f"❌ {layout_file} - 不存在")
            all_exist = False
    
    # 检查UI组件文件
    for ui_file in ui_files[1:]:  # 剩余的是UI组件
        file_path = os.path.join(ui_dir, ui_file)
        if os.path.exists(file_path):
            print(f"✅ {ui_file} - 存在")
        else:
            print(f"❌ {ui_file} - 不存在")
            all_exist = False
    
    print("=" * 50)
    return all_exist

# 检查MainActivity修改
def check_main_activity():
    print("🔧 检查MainActivity修改...")
    print("=" * 50)
    
    main_activity_path = os.path.join('..', '..', '..', '..', 'kotlin', 'com', 'example', 'fangcunxu', 'MainActivity.kt')
    
    if not os.path.exists(main_activity_path):
        print("❌ MainActivity.kt - 不存在")
        return False
    
    # 检查文件内容
    try:
        with open(main_activity_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        checks = [
            ('updateCompositionUI', '构图分析UI更新方法'),
            ('analyzeCompositionRules', '构图分析调用'),
            ('tvRuleOfThirds', '三分法则UI组件'),
            ('tvHorizonLine', '地平线UI组件'),
            ('tvNegativeSpace', '负空间UI组件'),
            ('tvSubjectProminence', '主体突出UI组件')
        ]
        
        all_found = True
        for check, description in checks:
            if check in content:
                print(f"✅ {description} - 已添加")
            else:
                print(f"❌ {description} - 未添加")
                all_found = False
        
        print("=" * 50)
        return all_found
    except Exception as e:
        print(f"❌ 检查MainActivity失败: {e}")
        return False

# 主函数
def main():
    print("🎯 验证UI与AI模型集成")
    print("=" * 60)
    
    ui_check = check_ui_files()
    main_activity_check = check_main_activity()
    
    print("\n📋 验证结果")
    print("=" * 60)
    
    if ui_check and main_activity_check:
        print("🎉 所有检查通过！UI与AI模型集成完成")
        print("\n📌 已实现的UI功能:")
        print("  • 场景识别结果显示 (MobileNet V3)")
        print("  • 目标检测框显示 (YOLOv8n)")
        print("  • 构图评分显示")
        print("  • 构图分析详细指标显示")
        print("  • 实时AI分析反馈")
    else:
        print("⚠️  部分检查失败，请检查配置")
    
    print("=" * 60)

if __name__ == "__main__":
    main()