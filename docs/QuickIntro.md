# Mentara-校园心理健康互助社区

Mentara 心理互助平台是一个专为大学生设计的心理健康互助社区，提供心情记录、社区分享、AI心理咨询、专家预约等功能，现在支持移动网页端和PC网页与应用三端，帮助用户更好地管理心理健康，学校及管理方更好跟进学生心理状态。

## 主要技术栈
- 基于React + Springboot的网页开发框架
- 混合使用多种数据库：MySQL, MongoDB, Qdrant, OSS...
- 引入Redis缓存机制
- 本地运行大模型微服务支持embedding和文本分类功能

## 分层架构 + 微服务架构
- 服务器遵循标准的Spring Boot分层架构
- 微服务架构：集成了多个轻量级服务，可用Dockerfile一键部署并通过http通信：DeepSeek AI服务、文本分类本地大模型、llama.cpp本地向量大模型、Qdrant向量数据库、阿里云OSS存储等