/**
 * 应用常量配置
 */

// API相关常量
export const API_CONFIG = {
   BASE_URL: "http://localhost:8080/api",
  // remote_url
  //BASE_URL: "http://47.101.72.204:8080/api",
  TIMEOUT: 10000, // 10秒超时
  RETRY_ATTEMPTS: 3,
};

// 认证相关常量

export const AUTH_CONFIG = {
  TOKEN_KEY: "access_token",
  REFRESH_TOKEN_KEY: "refresh_token",
  USER_INFO_KEY: "mentara_user_info",
  LOGIN_STATE_KEY: "mentara_login_state",
  TOKEN_EXPIRE_HOURS: 24,
};

// 表单验证常量
export const VALIDATION_RULES = {
  STUDENT_ID: {
    MIN_LENGTH: 6,
    MAX_LENGTH: 12,
    PATTERN: /^[a-zA-Z0-9]+$/,
  },
  PASSWORD: {
    MIN_LENGTH: 6,
    MAX_LENGTH: 50,
  },
  EMAIL: {
    PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  },
};

// UI相关常量
export const UI_CONFIG = {
  NAVIGATION_DELAY: 1500, // 页面跳转延迟（毫秒）
  LOADING_DEBOUNCE: 300, // 加载状态防抖（毫秒）
  SUCCESS_MESSAGE_DURATION: 3000, // 成功消息显示时长（毫秒）
};

// 错误消息常量
export const ERROR_MESSAGES = {
  // 表单验证错误
  VALIDATION: {
    EMPTY_STUDENT_ID: "请输入学号",
    INVALID_STUDENT_ID: "学号格式不正确",
    STUDENT_ID_LENGTH: "学号长度应为6-12位",
    EMPTY_PASSWORD: "请输入密码",
    PASSWORD_TOO_SHORT: "密码长度至少6位",
    PASSWORD_TOO_LONG: "密码长度不能超过50位",
    EMPTY_EMAIL: "请输入邮箱",
    INVALID_EMAIL: "请输入有效的邮箱地址",
    EMPTY_CONFIRM_PASSWORD: "请确认密码",
    PASSWORD_MISMATCH: "两次输入的密码不一致",
  },

  // 网络和API错误
  NETWORK: {
    CONNECTION_FAILED: "无法连接到服务器，请检查网络连接",
    TIMEOUT: "请求超时，请稍后重试",
    SERVER_ERROR: "服务器错误，请稍后重试",
  },

  // 认证错误
  AUTH: {
    LOGIN_FAILED: "学号或密码错误，请重试",
    USER_NOT_FOUND: "学号不存在，请检查学号或前往注册",
    REGISTER_FAILED: "注册失败，请稍后重试",
    STUDENT_ID_EXISTS: "学号已被注册，请检查学号或前往登录",
    EMAIL_EXISTS: "邮箱已被注册，请使用其他邮箱或前往登录",
    TOKEN_EXPIRED: "登录已过期，请重新登录",
    UNAUTHORIZED: "未授权访问，请先登录",
  },

  // 通用错误
  COMMON: {
    UNKNOWN_ERROR: "发生未知错误，请稍后重试",
    OPERATION_FAILED: "操作失败，请稍后重试",
  },
};

// 成功消息常量
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: "登录成功！欢迎来到Mentara",
  REGISTER_SUCCESS: "注册成功！请登录您的账户",
  LOGOUT_SUCCESS: "已安全退出",
  PROFILE_UPDATED: "个人信息更新成功",
  POST_CREATED: "帖子发布成功",
  POST_DELETED: "帖子删除成功",
};

// 路由常量
export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  COMMUNITY: "/community",
  PROFILE: "/profile",
  AI_CHAT: "/ai-chat",
  POST: "/post",
  ADMIN: "/admin",
};

// 本地存储键名
export const STORAGE_KEYS = {
  ...AUTH_CONFIG,
  THEME: "mentara_theme",
  LANGUAGE: "mentara_language",
  USER_PREFERENCES: "mentara_user_preferences",
};

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 50,
  FIRST_PAGE: 0,
};

// 文件上传配置
export const UPLOAD_CONFIG = {
  MAX_FILE_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_IMAGE_TYPES: ["image/jpeg", "image/png", "image/gif", "image/webp"],
  ALLOWED_FILE_TYPES: ["application/pdf", "text/plain"],
};

// 全局统一标签列表
export const TAG_LIST = [
  "心理健康",
  "成长",
  "匿名",
  "正能量",
  "情感",
  "学习",
  "生活",
  "压力",
  "求助",
  "分享",
];

// 聊天室类型常量（与后端保持一致）
export const CHAT_ROOM_TYPE = {
  REALNAME: "REALNAME", // 实名聊天室
  ANONYMOUS: "ANONYMOUS", // 匿名聊天室
  PRIVATE: "PRIVATE", // 私聊（仅用于私聊功能，不在聊天室页面显示）
};
