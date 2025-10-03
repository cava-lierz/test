import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import GlassCard from '../components/GlassCard';

// 常量定义
const NAVIGATION_DELAY = 1500; // 跳转延迟时间（毫秒）

// 错误消息映射
const ERROR_MESSAGES = {
  EMPTY_STUDENT_ID: "请输入学号",
  EMPTY_PASSWORD: "请输入密码",
  NETWORK_ERROR: "无法连接到服务器，请检查网络连接",
  AUTH_FAILED: "学号或密码错误，请重试",
  USER_NOT_FOUND: "学号不存在，请检查学号或前往注册",
  DEFAULT_ERROR: "登录失败，请稍后重试",
};

export default function LoginPage() {
  const [studentId, setStudentId] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [loginSuccess, setLoginSuccess] = useState(false);
  
  const { handleLogin, showSuccess, isLoggedIn } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (loginSuccess && isLoggedIn) {
      setTimeout(() => {
        navigate('/community');
      }, NAVIGATION_DELAY);
    }
  }, [loginSuccess, isLoggedIn, navigate]);

  /**
   * 处理表单提交
   * @param {Event} e - 表单事件
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // 清除之前的错误信息
    setError("");

    // 验证输入
    if (!studentId.trim()) {
      setError(ERROR_MESSAGES.EMPTY_STUDENT_ID);
      return;
    }

    if (!password.trim()) {
      setError(ERROR_MESSAGES.EMPTY_PASSWORD);
      return;
    }

    setIsLoading(true);

    try {
      // 调用登录API - 将学号作为用户名发送给后端
      const response = await authAPI.login({
        username: studentId.trim(), // 将学号映射为用户名
        password: password,
      });

      // 登录成功，处理用户信息
      const userData = {
        id: response.id,
        username: response.username,
        nickname: response.nickname,
        email: response.email,
        role: response.role, // 添加role字段
        token: response.accessToken, // 修正字段名
        studentId: studentId.trim(), // 保存学号信息
        // 添加必要的显示字段
        avatar: response.avatar || `https://i.pravatar.cc/150?u=${response.id}`,
        joinDate: new Date().toLocaleDateString(),
        posts: 0,
        likes: 0,
        moodAverage: "0.0",
      };

      // 调用认证上下文的登录处理
      handleLogin(userData);
      
      // 显示成功消息
      showSuccess('登录成功！欢迎来到Mentara');
      
      // 触发成功状态，useEffect将处理跳转
      setLoginSuccess(true);

      // 显示成功消息
      showSuccess("登录成功！欢迎来到Mentara");

      // 触发成功状态，useEffect将处理跳转
      setLoginSuccess(true);
    } catch (error) {
      console.error("登录失败:", error);

      // 根据错误类型设置不同的错误消息
      const errorMessage = getErrorMessage(error);
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * 根据错误类型获取错误消息
   * @param {Error} error - 错误对象
   * @returns {string} 错误消息
   */
  const getErrorMessage = (error) => {
    const message = error.message || "";

    if (message.includes("Communications link failure")) {
      return ERROR_MESSAGES.NETWORK_ERROR;
    } else if (message.includes("401") || message.includes("认证失败")) {
      return ERROR_MESSAGES.AUTH_FAILED;
    } else if (message.includes("User not found")) {
      return ERROR_MESSAGES.USER_NOT_FOUND;
    } else {
      return message || ERROR_MESSAGES.DEFAULT_ERROR;
    }
  };

  return (
    <div className="page-container">
      {/* 动态渐变背景效果 */}
      <div className="page-background"></div>

      <GlassCard className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <div className="auth-logo-text">M</div>
          </div>
          <h1 className="auth-title gradient-text">Mentara</h1>
          <p className="auth-subtitle">分享快乐，发泄不满，我们在这里等你。</p>
          <p className="auth-tagline">MENTAL HEALTH COMMUNITY</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {/* 错误消息显示 */}
          {error && (
            <div
              className="error-message"
              style={{
                color: "#ff6b6b",
                backgroundColor: "rgba(255, 107, 107, 0.1)",
                border: "1px solid rgba(255, 107, 107, 0.3)",
                borderRadius: "8px",
                padding: "12px",
                marginBottom: "20px",
                fontSize: "14px",
              }}
            >
              {error}
            </div>
          )}

          <div className="form-group">
            <label className="form-label">学号</label>
            <input
              type="text"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              placeholder="请输入学号"
              className="form-input"
              disabled={isLoading}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label className="form-label">密码</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码"
              className="form-input"
              disabled={isLoading}
              autoComplete="current-password"
            />
          </div>

          <button type="submit" disabled={isLoading} className="auth-button">
            {isLoading ? (
              <>
                登录中
                <div className="loading-spinner"></div>
              </>
            ) : (
              "登录"
            )}
          </button>
        </form>

        <div className="auth-footer">
          <div>
            <Link to="/forgot-password" className="auth-link-inline">
              忘记密码？
            </Link>
          </div>
          <div>
            <span>还没有账号？</span>
            <Link to="/register" className="auth-link-inline">
              立即注册
            </Link>
          </div>
        </div>
      </GlassCard>
    </div>
  );
}
