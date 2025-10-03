const { contextBridge, ipcRenderer } = require("electron");

// 暴露安全的API给渲染进程
contextBridge.exposeInMainWorld("electronAPI", {
  // 获取应用版本
  getVersion: () => process.versions.electron,

  // 获取平台信息
  getPlatform: () => process.platform,

  // 最小化窗口
  minimize: () => ipcRenderer.send("minimize-window"),

  // 最大化窗口
  maximize: () => ipcRenderer.send("maximize-window"),

  // 关闭窗口
  close: () => ipcRenderer.send("close-window"),

  // 获取窗口状态
  getWindowState: () => ipcRenderer.invoke("get-window-state"),

  // 设置窗口大小
  setWindowSize: (width, height) =>
    ipcRenderer.send("set-window-size", width, height),

  // 监听窗口状态变化
  onWindowStateChange: (callback) => {
    ipcRenderer.on("window-state-changed", callback);
  },

  // 移除监听器
  removeAllListeners: (channel) => {
    ipcRenderer.removeAllListeners(channel);
  },
});

// 处理未捕获的异常
window.addEventListener("error", (event) => {
  console.error("渲染进程错误:", event.error);
});

window.addEventListener("unhandledrejection", (event) => {
  console.error("未处理的Promise拒绝:", event.reason);
});
