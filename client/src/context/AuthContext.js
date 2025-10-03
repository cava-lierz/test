import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import { authAPI } from "../services/api";
import { AUTH_CONFIG } from "../utils/constants";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [confirmConfig, setConfirmConfig] = useState({});

  // 初始化认证状态
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        console.log("AuthContext - 开始初始化认证状态");
        // 先尝试读取全局user_info，获取userId
        let userInfo = localStorage.getItem(AUTH_CONFIG.USER_INFO_KEY);
        let parsedUserInfo = null;
        let userId = null;
        if (userInfo) {
          try {
            parsedUserInfo = JSON.parse(userInfo);
            userId = parsedUserInfo.id;
            console.log("AuthContext - 解析全局user_info成功，userId:", userId);
          } catch (e) {
            console.error("AuthContext - 解析全局user_info失败:", e);
          }
        }
        // 用userId拼接key再读取真正的user数据
        if (userId) {
          const token = localStorage.getItem(
            `${AUTH_CONFIG.TOKEN_KEY}_${userId}`
          );
          userInfo = localStorage.getItem(
            `${AUTH_CONFIG.USER_INFO_KEY}_${userId}`
          );
          console.log(
            "AuthContext - 检查用户数据，token存在:",
            !!token,
            "userInfo存在:",
            !!userInfo
          );
          if (token && userInfo) {
            try {
              parsedUserInfo = JSON.parse(userInfo);
              console.log("AuthContext - 设置用户登录状态为true");
              setIsLoggedIn(true);
              setUser(parsedUserInfo);
              localStorage.setItem(
                `${AUTH_CONFIG.LOGIN_STATE_KEY}_${userId}`,
                "true"
              );
              console.log("AuthContext - 设置isLoading为false");
              setIsLoading(false);
              authAPI
                .validateToken(userId)
                .then((isValid) => {
                  if (!isValid) {
                    console.warn(
                      "Token validation failed, will handle on API calls"
                    );
                  }
                })
                .catch((validationError) => {
                  console.warn(
                    "Token validation error:",
                    validationError.message
                  );
                  // 如果token验证失败，清除认证状态
                  if (validationError.message.includes("登录已过期")) {
                    setIsLoggedIn(false);
                    setUser(null);
                    authAPI.logout(userId);
                  }
                });
            } catch (parseError) {
              console.error("AuthContext - 解析用户信息失败:", parseError);
              setIsLoggedIn(false);
              setUser(null);
              authAPI.logout(userId);
              setIsLoading(false);
            }
            return;
          }
        }
        console.log("AuthContext - 未找到有效用户数据，设置登录状态为false");
        setIsLoggedIn(false);
        setUser(null);
        setIsLoading(false);
      } catch (error) {
        console.error("AuthContext - 初始化认证状态失败:", error);
        setIsLoggedIn(false);
        setUser(null);
        setIsLoading(false);
      }
    };
    initializeAuth();
  }, []);

  // 登录处理函数
  const handleLogin = useCallback((userData) => {
    setIsLoggedIn(true);
    setUser(userData);
    const userId = userData.id;
    localStorage.setItem(`${AUTH_CONFIG.LOGIN_STATE_KEY}_${userId}`, "true");
    localStorage.setItem(
      `${AUTH_CONFIG.USER_INFO_KEY}_${userId}`,
      JSON.stringify(userData)
    );
    // 额外存一份user_info用于下次初始化时获取userId
    localStorage.setItem(AUTH_CONFIG.USER_INFO_KEY, JSON.stringify(userData));
    if (userData.token) {
      localStorage.setItem(
        `${AUTH_CONFIG.TOKEN_KEY}_${userId}`,
        userData.token
      );
    }
  }, []);

  // 退出登录处理函数
  const handleLogout = useCallback(() => {
    if (user && user.id) {
      const userId = user.id;
      authAPI.logout(userId); // 统一清理所有本地存储
    }
    setIsLoggedIn(false);
    setUser(null);
  }, [user]);

  // 更新用户信息
  const updateUser = useCallback(
    (newUserData) => {
      const updatedUser = { ...user, ...newUserData };
      setUser(updatedUser);
      if (updatedUser && updatedUser.id) {
        localStorage.setItem(
          `${AUTH_CONFIG.USER_INFO_KEY}_${updatedUser.id}`,
          JSON.stringify(updatedUser)
        );
        localStorage.setItem(
          AUTH_CONFIG.USER_INFO_KEY,
          JSON.stringify(updatedUser)
        );
      }
    },
    [user]
  );

  // 检查是否已登录
  const checkAuthStatus = () => {
    return authAPI.isLoggedIn() && user;
  };

  // 显示成功提示
  const showSuccess = useCallback((message) => {
    setSuccessMessage(message);
    setShowSuccessModal(true);
  }, []);

  // 显示确认对话框
  const showConfirm = useCallback((message, onConfirm) => {
    setConfirmConfig({ message, onConfirm });
    setShowConfirmModal(true);
  }, []);

  // 全局确认对话框函数
  const showConfirmDialog = useCallback((message, onConfirm, onCancel) => {
    setConfirmConfig({
      message,
      onConfirm: () => {
        if (onConfirm) onConfirm();
        setShowConfirmModal(false);
      },
      onCancel: () => {
        if (onCancel) onCancel();
        setShowConfirmModal(false);
      },
    });
    setShowConfirmModal(true);
  }, []);

  // 获取当前用户token
  const getToken = () => {
    if (user && user.id) {
      return localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${user.id}`);
    }
    return null;
  };

  // 获取用户信息
  const getCurrentUser = () => {
    if (user && user.id) {
      const userInfo = localStorage.getItem(
        `${AUTH_CONFIG.USER_INFO_KEY}_${user.id}`
      );
      return userInfo ? JSON.parse(userInfo) : null;
    }
    return null;
  };

  // 检查是否为管理员
  const isAdmin = () => {
    return user && user.role === "ADMIN";
  };

  // 检查是否为专家
  const isExpert = () => {
    return user && user.role === "EXPERT";
  };

  const value = {
    isLoggedIn,
    user,
    isLoading,
    handleLogin,
    handleLogout,
    updateUser,
    checkAuthStatus,
    getToken,
    getCurrentUser,
    isAdmin,
    isExpert,
    showSuccess,
    showConfirm,
    showConfirmDialog,
    showSuccessModal,
    showConfirmModal,
    successMessage,
    confirmConfig,
    setShowSuccessModal,
    setShowConfirmModal,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
