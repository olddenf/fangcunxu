# GitHub Desktop 使用指南

## 步骤 1：下载并安装 GitHub Desktop

1. 访问 [GitHub Desktop 官网](https://desktop.github.com/)
2. 下载适合你操作系统的版本
3. 安装 GitHub Desktop

## 步骤 2：登录 GitHub 账户

1. 打开 GitHub Desktop
2. 点击 "Sign in to GitHub.com"
3. 输入你的 GitHub 用户名和密码
4. 完成登录

## 步骤 3：克隆仓库

1. 点击 "File" > "Clone repository"
2. 在 "URL" 标签页中输入：`https://github.com/olddenf/fangcunxu.git`
3. 选择本地保存位置（建议选择一个空文件夹）
4. 点击 "Clone"

## 步骤 4：添加文件

1. 打开克隆的仓库文件夹
2. 将 `d:\Programs\tool\fangcunxu` 目录下的所有文件复制到克隆的仓库文件夹中
3. 特别注意复制 `android` 目录及其所有内容

## 步骤 5：提交更改

1. 回到 GitHub Desktop
2. 你会看到所有添加的文件都显示在 "Changes" 标签页中
3. 在 "Summary" 输入框中输入提交信息：
   ```
   Add complete Android project with AI models and core functionality
   
   - Add NIMA MobileNet model (10.5 MB) for composition scoring
   - Add MobileNet V3 Small model (5.2 MB) for scene recognition
   - Add YOLOv8n INT8 model (5.2 MB) for object detection
   - Add complete AI engine implementation
   - Add camera functionality and UI components
   - Add all project files and resources
   ```
4. 点击 "Commit to master"

## 步骤 6：推送到 GitHub

1. 点击 "Push origin" 按钮（位于窗口右上角）
2. 等待推送完成

## 验证

1. 打开浏览器访问：https://github.com/olddenf/fangcunxu
2. 确认所有文件都已上传成功
3. 查看提交历史，确认版本 v0.5.0.1 已推送

## 注意事项

- 确保网络连接稳定
- 推送可能需要一些时间，特别是包含大文件时
- 如果遇到问题，可以尝试重启 GitHub Desktop 或重新克隆仓库
