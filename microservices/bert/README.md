# BERT 推理服务

基于 FastAPI 的 BERT 模型推理服务，使用 ONNX Runtime 进行 CPU 加速优化。

## 功能特性

- 🚀 **ONNX Runtime 加速**：使用 Optimum 库自动转换模型为 ONNX 格式，CPU 推理速度提升 2-4 倍
- 💻 **纯 CPU 运行**：适用于无 GPU 的服务器环境
- 🔄 **双模型支持**：同时提供情绪评分和抑郁检查两个服务
- 📊 **自动优化**：首次运行时自动将 PyTorch 模型转换为优化的 ONNX 格式

## 快速开始

### Deploy

### API

### Troubleshot

