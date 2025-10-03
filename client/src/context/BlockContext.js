import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import { blockAPI } from '../services/api';

const BlockContext = createContext();

export const useBlock = () => {
  const context = useContext(BlockContext);
  if (!context) {
    throw new Error("useBlock must be used within a BlockProvider");
  }
  return context;
};

export const BlockProvider = ({ children }) => {
  const { user } = useAuth();
  const [blockedUserIds, setBlockedUserIds] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(null);

  // 获取拉黑用户列表
  const fetchBlockedUsers = useCallback(async () => {
    // 检查用户是否已登录
    if (!user || !user.id) {
      setBlockedUserIds([]);
      return;
    }
    
    // 管理员不需要拉黑功能
    if (user.role === 'ADMIN') {
      setBlockedUserIds([]);
      return;
    }
    
    try {
      setIsLoading(true);
      const response = await blockAPI.getBlockedUsers();
      const blockedIds = response.blockedUserIds || [];
      setBlockedUserIds(blockedIds);
      setLastUpdated(new Date());
      console.log('拉黑列表已更新:', blockedIds);
    } catch (error) {
      console.error('获取拉黑用户列表失败:', error);
      setBlockedUserIds([]);
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  // 拉黑用户
  const blockUser = useCallback(async (userId, reason = '') => {
    if (!user || !user.id) {
      throw new Error('用户未登录');
    }

    try {
      await blockAPI.blockUser(userId, reason);
      // 拉黑成功后立即更新本地状态
      setBlockedUserIds(prev => {
        const newList = [...prev, userId];
        console.log('用户已拉黑，更新列表:', newList);
        return newList;
      });
      setLastUpdated(new Date());
      return true;
    } catch (error) {
      console.error('拉黑用户失败:', error);
      throw error;
    }
  }, [user]);

  // 取消拉黑用户
  const unblockUser = useCallback(async (userId) => {
    if (!user || !user.id) {
      throw new Error('用户未登录');
    }

    try {
      await blockAPI.unblockUser(userId);
      // 取消拉黑成功后立即更新本地状态
      setBlockedUserIds(prev => {
        const newList = prev.filter(id => id !== userId);
        console.log('用户已取消拉黑，更新列表:', newList);
        return newList;
      });
      setLastUpdated(new Date());
      return true;
    } catch (error) {
      console.error('取消拉黑用户失败:', error);
      throw error;
    }
  }, [user]);

  // 检查用户是否被拉黑
  const isUserBlocked = useCallback((userId) => {
    return blockedUserIds.includes(userId);
  }, [blockedUserIds]);

  // 强制刷新拉黑列表
  const refreshBlockedUsers = useCallback(() => {
    fetchBlockedUsers();
  }, [fetchBlockedUsers]);

  // 用户登录状态变化时重新获取拉黑列表
  useEffect(() => {
    fetchBlockedUsers();
  }, [fetchBlockedUsers]);

  const value = {
    blockedUserIds,
    isLoading,
    lastUpdated,
    blockUser,
    unblockUser,
    isUserBlocked,
    refreshBlockedUsers,
    fetchBlockedUsers
  };

  return (
    <BlockContext.Provider value={value}>
      {children}
    </BlockContext.Provider>
  );
}; 