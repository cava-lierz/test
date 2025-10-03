/**
 * 认证工具函数
 */

import { AUTH_CONFIG } from './constants';

/**
 * 清除所有认证信息
 */
export const clearAllAuthData = () => {
  // 清除所有相关的认证信息
  localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
  localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
  localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
  
  // 清除所有以特定前缀开头的键
  const keysToRemove = [];
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && (
      key.startsWith(`${AUTH_CONFIG.TOKEN_KEY}_`) ||
      key.startsWith(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_`) ||
      key.startsWith(`${AUTH_CONFIG.USER_INFO_KEY}_`) ||
      key.startsWith(`${AUTH_CONFIG.LOGIN_STATE_KEY}_`)
    )) {
      keysToRemove.push(key);
    }
  }
  keysToRemove.forEach(key => localStorage.removeItem(key));
  
  console.log('已清除所有认证信息');
};

/**
 * 强制重新登录
 */
export const forceReLogin = () => {
  clearAllAuthData();
  // 重定向到登录页面
  window.location.href = '/login';
};

/**
 * 检查是否有有效的认证信息
 */
export const hasValidAuth = () => {
  const userInfo = localStorage.getItem(AUTH_CONFIG.USER_INFO_KEY);
  if (!userInfo) return false;
  
  try {
    const parsed = JSON.parse(userInfo);
    const userId = parsed.id;
    if (!userId) return false;
    
    const token = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    return !!token;
  } catch (e) {
    return false;
  }
};

/**
 * 获取当前用户ID
 */
export const getCurrentUserId = () => {
  const userInfo = localStorage.getItem(AUTH_CONFIG.USER_INFO_KEY);
  if (userInfo) {
    try {
      const parsed = JSON.parse(userInfo);
      return parsed.id;
    } catch (e) {
      console.error("解析用户信息失败:", e);
    }
  }
  return null;
}; 