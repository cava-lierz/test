#!/bin/bash

# 构建脚本
set -e

echo "开始构建embedding service..."

# 创建构建目录
mkdir -p build
cd build

# 配置CMake
echo "配置CMake..."
cmake .. -DCMAKE_BUILD_TYPE=Release -DGGML_CUDA=ON

# 编译
echo "编译项目..."
make -j$(nproc)

echo "构建完成！"
echo "可执行文件位置: build/bin/embedding_server"
echo ""
echo "运行服务器:"
echo "  ./build/bin/embedding_server"
echo ""
echo "查看帮助:"
echo "  ./build/bin/embedding_server --help"