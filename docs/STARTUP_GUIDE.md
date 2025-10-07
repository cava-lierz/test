# Mentara 项目一键启动指南

本指南提供了 Mentara 项目的快速启动、停止和管理方法。

## 📋 目录

- [前置要求](#前置要求)
- [快速开始](#快速开始)
- [脚本说明](#脚本说明)
- [服务架构](#服务架构)
- [常见问题](#常见问题)
- [手动启动](#手动启动)

## 🔧 前置要求

在启动项目之前，请确保已安装以下软件：

### 必需软件

| 软件 | 版本要求 | 用途 | 安装命令 |
|------|---------|------|----------|
| Docker | >= 20.10 | 数据库容器 | [官方安装指南](https://docs.docker.com/get-docker/) |
| Docker Compose | >= 2.0 | 容器编排 | 通常随Docker一起安装 |
| Node.js | 22 | 前端运行环境 | [官方安装指南](https://nodejs.org/zh-cn/download) |
| Java JDK | >= 17 | 后端运行环境 | `sudo apt install java-17-openjdk` |
| Maven | >= 3.8 | Java构建工具 | `sudo apt install maven` |
| Python | 3.10 | BERT微服务 | `sudo apt install python3 python3-pip` |
| GCC/G++ | >= 9.0 | Embedding编译 | `sudo apt install build-essentials` |
| CMake | >= 3.15 | 构建工具 | `sudo apt install cmake` |

### 可选工具

- `lsof` - 端口检查（通常已预装）
- `curl` - 健康检查（通常已预装）

## 🚀 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

```bash
cd microservices/quick-embedding
mkdir -p build
cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DGGML_CUDA=OFF # only CPU
make -j
```

### 2. 启动项目

#### BERT

```bash
cd microservices/bert
python bert_service.py
```

#### Embedding

```bash
cd microservices/quick-embedding
./build/bin/embedding_server
```
#### DB

```bash
cd database
docker compose up -d
docker-compose up -d # or
```

#### Springboot

```bash
cd server
maven spring-boot:run
```
#### React

```bash
cd client
npm start
```

## 🏗️ 服务架构

### 服务依赖关系

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (React)                          │
│                   localhost:3000                         │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────┐
│               后端 (Spring Boot)                         │
│                 localhost:8080/api                       │
└──┬──────────────────┬───────────────┬──────────────────┘
   │                  │               │
   ↓                  ↓               ↓
┌────────┐    ┌──────────────┐  ┌────────────────┐
│  BERT  │    │  Embedding   │  │   数据库层     │
│  :8000 │    │    :8081     │  │  MySQL:3306    │
└────────┘    └──────────────┘  │  MongoDB:27017 │
                                 │  Qdrant:6333   │
                                 │  Redis:6379    │
                                 └────────────────┘
```

### 服务端口映射

| 服务 | 端口 | 协议 | 用途 |
|------|------|------|------|
| 前端 | 3000 | HTTP | React 开发服务器 |
| 后端 | 8080 | HTTP | Spring Boot REST API |
| BERT | 8000 | HTTP | 情感分析服务 |
| Embedding | 8081 | HTTP | 文本向量化服务 |
| MySQL | 3306 | TCP | 关系型数据库 |
| MongoDB | 27017 | TCP | 文档数据库 |
| Qdrant | 6333 | HTTP | 向量数据库 API |
| Qdrant | 6334 | gRPC | 向量数据库 gRPC |
| Redis | 6379 | TCP | 缓存数据库 |
---

**版本**: 1.0.0  
**最后更新**: 2025-10-06  
**维护者**: Mentara 团队



