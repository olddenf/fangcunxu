# 项目文件整理报告

**整理日期**: 2026 年 3 月 25 日  
**整理目标**: 清理废弃文件，优化项目结构

---

## 一、整理概览

### 当前使用的文件结构

#### Kotlin 源代码
- ✅ `MainActivity.kt` - 主页面（相机预览）
- ✅ `SettingsActivity.kt` - 设置页面

#### Layout 布局
- ✅ `activity_main.xml` - 主页面布局
- ✅ `activity_settings.xml` - 设置页布局

#### Drawable 资源
**图标类**:
- `camera.xml` - 相机图标
- `lens.xml` - 镜头图标
- `gallery.xml` - 画卷图标
- `settings.xml` - 设置图标
- `brain.xml` - AI 大脑图标
- `magic_wand.xml` - 魔法棒图标
- `sparkle.xml` - 闪光图标
- `language.xml` - 语言图标
- `grid.xml` - 网格图标
- `arrow_right.xml` - 右箭头图标
- `ic_back.xml` - 返回图标
- `ic_camera.xml`, `ic_camera_nav.xml` - 相机图标变体
- `ic_lens.xml`, `ic_lens_simple.xml` - 镜头图标变体
- `ic_gallery.xml` - 画廊图标
- `ic_settings.xml` - 设置图标

**背景类**:
- `bottom_controls_background.xml` - 底部控制区背景
- `capture_button_background.xml` - 拍照按钮背景
- `gradient_bottom.xml` - 底部渐变
- `list_item_background.xml` - 列表项背景

**其他**:
- `logo_transparent.png` - 透明 Logo
- `grid_lines_rule_of_thirds.xml` - 三分法网格线（简化版）
- `result_background.xml` - 结果面板背景（简化版）
- `nav_bar_background.xml` - 导航栏背景（简化版）

---

## 二、已废弃文件（移至 deprecated 文件夹）

### 1. 废弃的 Activity
- ❌ `CameraActivity.kt` - 旧相机 Activity
- ❌ `GalleryActivity.kt` - 旧画廊 Activity
- ❌ `CameraConfigActivity.kt` - 旧配置 Activity
- ❌ `CameraConfigManager.kt` - 相机配置管理器

### 2. 废弃的 Layout
- ❌ `activity_camera.xml` - 旧相机布局
- ❌ `activity_camera_config.xml` - 旧配置布局
- ❌ `activity_camera_config_detail.xml` - 旧配置详情布局
- ❌ `activity_gallery.xml` - 旧画廊布局

### 3. 废弃的 Drawable
**评分相关**:
- `ai_score_panel_background.xml`
- `score_panel_background.xml`
- `score_bar_background.xml`
- `score_progress_drawable.xml`

**网格线相关**:
- `grid_lines.xml`
- `grid_lines_diagonal.xml`
- `grid_lines_golden_ratio.xml`
- `grid_lines_rule_of_thirds.xml` (原始版本)
- `grid_overlay.xml`

**按钮和控件**:
- `capture_button.xml`
- `camera_background.xml`
- `control_area_background.xml`
- `flash_button_background.xml`
- `button_ripple.xml`
- `underline.xml`

**图标**:
- `ic_bounding_box.xml`
- `ic_clock_counter_clockwise.xml`
- `ic_image_square.xml`

**Logo**:
- `logo.png`
- `logo_original.png`

**其他**:
- `launch_background.xml`
- `nav_bar_background.xml` (原始版本)
- `result_background.xml` (原始版本)

### 4. 废弃的动画
- `anim/fade_in.xml`
- `animator/button_press.xml`

### 5. 废弃的资源
- `assets/model_prompt.txt` - AI 模型提示词

---

## 三、AndroidManifest 变更

### 保留的 Activity
```xml
<activity android:name=".MainActivity" />
<activity android:name=".SettingsActivity" />
```

### 移除的 Activity
- ❌ CameraConfigActivity
- ❌ CameraConfigDetailActivity
- ❌ GalleryActivity
- ❌ CameraActivity

---

## 四、代码变更

### MainActivity.kt
- 移除 GalleryActivity 引用
- 底部导航"画卷"按钮改为显示"功能开发中"提示

### SettingsActivity.kt
- 移除 GalleryActivity 引用
- 底部导航"画卷"按钮改为显示"功能开发中"提示

---

## 五、构建验证

✅ **构建成功**
- 编译无错误
- 资源链接正常
- APK 生成成功

---

## 六、后续建议

1. **画卷功能**: 当前显示"功能开发中"，后续可实现图片浏览功能
2. **引导线控制**: 已实现选择对话框（三分法、黄金分割、黄金螺旋、对角线、关闭）
3. **Logo 使用**: 统一使用 `logo_transparent.png`

---

## 七、文件统计

| 类别 | 使用中 | 已废弃 |
|------|--------|--------|
| Kotlin 文件 | 2 | 4 |
| Layout 文件 | 2 | 4 |
| Drawable 文件 | 25 | 26 |
| 动画文件 | 0 | 2 |
| 资源文件 | 0 | 1 |

**总计**: 29 个使用中，57 个已废弃

---

**整理完成** ✅
