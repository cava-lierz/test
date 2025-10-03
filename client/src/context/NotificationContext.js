import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import WebSocketService from '../services/webSocketService';
import { useAuth } from './AuthContext';
import { AUTH_CONFIG } from "../utils/constants";
import notificationService from "../services/notificationService";

const WS_URL = 'http://localhost:8080/api/ws';

const NotificationContext = createContext();

export function useNotification() {
  return useContext(NotificationContext);
}

// 工具函数：按id去重，保留createdAt较新的
function dedupeByIdKeepLatest(arr) {
  const map = new Map();
  arr.forEach(n => {
    if (!map.has(n.id) || new Date(n.createdAt) > new Date(map.get(n.id).createdAt)) {
      map.set(n.id, n);
    }
  });
  return Array.from(map.values()).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
}

// 默认的后端拉取通知方法
async function fetchNotifications(userId) {
  if (!userId) return [];
  const token = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
  if (!token) return [];
  return await notificationService.getUnreadNotifications();
}

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const userId = user?.id;
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [ws, setWs] = useState(null);

  // 拉取后端通知并合并
  useEffect(() => {
    if (!userId) {
      setNotifications([]);
      return;
    }
    const token = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    if (!token) {
      setNotifications([]);
      return;
    }
    let cancelled = false;
    async function load() {
      try {
        const data = await fetchNotifications(userId);
        if (!cancelled && Array.isArray(data)) {
          setNotifications(prev => {
            const merged = dedupeByIdKeepLatest([...data, ...prev]);
            localStorage.setItem(`notifications_${userId}`, JSON.stringify(merged));
            return merged;
          });
        }
      } catch (e) {}
    }
    load();
    return () => { cancelled = true; };
  }, [userId]);

  // 处理新通知
  const handleNewNotification = useCallback((notif) => {
    if (!userId) return;
    setNotifications(prev => {
      const merged = dedupeByIdKeepLatest([notif, ...prev]);
      localStorage.setItem(`notifications_${userId}`, JSON.stringify(merged));
      return merged;
    });
  }, [userId]);

  // 标记为已读
  const markAsRead = useCallback(async (id) => {
    if (!userId) return;
    try {
      await notificationService.markAsRead(id);
      setNotifications(prev => {
        const updated = prev.map(n => n.id === id ? { ...n, read: true } : n);
        localStorage.setItem(`notifications_${userId}`, JSON.stringify(updated));
        return updated;
      });
    } catch (e) {
      // 可选：错误处理
    }
  }, [userId]);

  // 全部标记为已读
  const markAllAsRead = useCallback(async () => {
    if (!userId) return;
    try {
      await notificationService.markAllAsRead();
      setNotifications(prev => {
        const updated = prev.map(n => ({ ...n, read: true }));
        localStorage.setItem(`notifications_${userId}`, JSON.stringify(updated));
        return updated;
      });
    } catch (e) {
      // 可选：错误处理
    }
  }, [userId]);
  
  // 删除单条通知
  const deleteNotification = useCallback(async (id) => {
    if (!userId) return;
    try {
      await notificationService.deleteNotification(id);
      setNotifications(prev => {
        const updated = prev.filter(n => n.id !== id);
        localStorage.setItem(`notifications_${userId}`, JSON.stringify(updated));
        return updated;
      });
    } catch (e) {
      console.error('删除通知失败:', id, e);
      // 可选：错误处理
    }
  }, [userId]);
  
  // 删除所有通知
  const deleteAllNotifications = useCallback(async () => {
    if (!userId) return;
    try {
      await notificationService.deleteAllNotifications();
      setNotifications([]);
      localStorage.removeItem(`notifications_${userId}`);
    } catch (e) {
      // 可选：错误处理
    }
  }, [userId]);

  // WebSocket连接
  useEffect(() => {
    if (!userId) return;
    const jwtToken = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    if (!jwtToken) return;
    const wsInstance = new WebSocketService({
      serverUrl: WS_URL,
      debug: process.env.NODE_ENV === 'development'
    });
    wsInstance.connect(jwtToken);
    wsInstance.on('connect', () => {
      console.log('WebSocket connected for notifications');
      wsInstance.subscribe('/user/queue/notifications', handleNewNotification);
      wsInstance.subscribe('/topic/notifications', handleNewNotification); // 订阅系统广播通知
    });
    wsInstance.on('error', (error) => {
      console.error('WebSocket error:', error);
    });
    wsInstance.on('disconnect', () => {
      console.log('WebSocket disconnected for notifications');
    });
    setWs(wsInstance);
    return () => {
      if (wsInstance && typeof wsInstance.disconnect === 'function') {
        wsInstance.disconnect();
      }
    };
  }, [userId, handleNewNotification]);

  // 计算未读数
  useEffect(() => {
    setUnreadCount(notifications.filter(n => !n.read).length);
  }, [notifications]);

  // 当userId变化时，切换localStorage数据
  useEffect(() => {
    if (userId) {
      const local = localStorage.getItem(`notifications_${userId}`);
      setNotifications(local ? JSON.parse(local) : []);
    } else {
      setNotifications([]);
    }
  }, [userId]);

  return (
    <NotificationContext.Provider value={{
      notifications,
      unreadCount,
      markAsRead,
      markAllAsRead,
      deleteNotification,
      deleteAllNotifications,
      setNotifications,
      ws
    }}>
      {children}
    </NotificationContext.Provider>
  );
} 