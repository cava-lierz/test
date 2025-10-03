import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [confirmConfig, setConfirmConfig] = useState({});

  // 检查本地存储中的登录状态
  useEffect(() => {
    const savedLoginState = localStorage.getItem('mentara_login_state');
    const savedUserInfo = localStorage.getItem('mentara_user_info');
    
    if (savedLoginState === 'true' && savedUserInfo) {
      setIsLoggedIn(true);
      setUser(JSON.parse(savedUserInfo));
    }
  }, []);

  // 登录处理函数
  const handleLogin = (userData) => {
    setIsLoggedIn(true);
    setUser(userData);
    localStorage.setItem('mentara_login_state', 'true');
    localStorage.setItem('mentara_user_info', JSON.stringify(userData));
  };

  // 退出登录处理函数
  const handleLogout = () => {
    setIsLoggedIn(false);
    setUser(null);
    localStorage.removeItem('mentara_login_state');
    localStorage.removeItem('mentara_user_info');
  };

  // 显示成功提示
  const showSuccess = (message) => {
    setSuccessMessage(message);
    setShowSuccessModal(true);
  };

  // 显示确认对话框
  const showConfirm = (message, onConfirm) => {
    setConfirmConfig({ message, onConfirm });
    setShowConfirmModal(true);
  };

  const value = {
    isLoggedIn,
    user,
    handleLogin,
    handleLogout,
    showSuccess,
    showConfirm,
    showSuccessModal,
    showConfirmModal,
    successMessage,
    confirmConfig,
    setShowSuccessModal,
    setShowConfirmModal
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 