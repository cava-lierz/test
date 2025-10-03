# Mentara Electron 桌面应用

这个项目现在支持打包为桌面应用，使用 Electron 框架。

## 安装依赖

### 方法 1：使用 npm（推荐）

```bash
npm install
```

### 方法 2：如果 npm 安装失败（网络问题）

```bash
# 安装cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com

# 使用cnpm安装依赖
cnpm install
```

## 开发模式

### 启动开发服务器

```bash
npm run electron-dev
```

这个命令会：

1. 启动 React 开发服务器 (http://localhost:3000)
2. 等待服务器启动完成
3. 启动 Electron 应用

### 仅启动 Electron（需要先启动 React 服务器）

```bash
npm run electron
```

## 打包应用

### 打包所有平台

```bash
npm run electron-pack
```

### 打包特定平台

```bash
# Windows
npm run electron-pack-win

# macOS
npm run electron-pack-mac

# Linux
npm run electron-pack-linux
```

打包后的文件会生成在 `dist` 目录中。

## 应用特性

- **跨平台支持**: Windows, macOS, Linux
- **原生菜单**: 包含文件、编辑、视图、窗口、帮助菜单
- **安全设置**: 禁用 nodeIntegration，启用 contextIsolation
- **外部链接处理**: 自动在默认浏览器中打开外部链接
- **开发者工具**: 开发模式下自动打开 DevTools
- **窗口管理**: 支持最小化、最大化、全屏等操作

## 文件结构

```
public/
├── electron.js          # Electron主进程文件
├── preload.js          # 预加载脚本（安全通信）
├── icon.png            # 应用图标
└── favicon.ico         # 网站图标

package.json            # 包含Electron配置
```

## 配置说明

### package.json 中的关键配置

```json
{
  "main": "public/electron.js",        # 主进程入口
  "homepage": "./",                     # 静态资源路径
  "build": {                           # 打包配置
    "appId": "com.mentara.client",     # 应用ID
    "productName": "Mentara",          # 应用名称
    "directories": {
      "output": "dist"                 # 输出目录
    }
  }
}
```

### 支持的打包格式

- **Windows**: NSIS 安装包 (.exe)
- **macOS**: DMG 镜像文件 (.dmg)
- **Linux**: AppImage (.AppImage)

## 开发注意事项

1. **安全**: 应用启用了安全设置，禁用了一些潜在危险的 API
2. **通信**: 使用 preload 脚本进行安全的进程间通信
3. **调试**: 开发模式下会自动打开开发者工具
4. **图标**: 确保 public 目录中有适当的图标文件

## 故障排除

### 常见问题

1. **端口冲突**: 确保 3000 端口没有被其他应用占用
2. **依赖问题**: 如果遇到依赖问题，删除 node_modules 并重新安装
3. **打包失败**: 检查是否有足够的磁盘空间和权限

### 调试技巧

- 使用 `console.log` 在渲染进程中调试
- 使用开发者工具查看网络请求和控制台输出
- 检查主进程日志了解应用启动情况

## 更新应用

要更新应用，需要：

1. 修改 `package.json` 中的版本号
2. 重新打包应用
3. 分发新的安装包

## 许可证

本项目遵循原项目的许可证条款。
