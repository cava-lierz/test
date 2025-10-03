import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import GlassCard from '../components/GlassCard';

export default function LoginPage() {
  const [studentId, setStudentId] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { handleLogin, showSuccess } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!studentId.trim() || !password.trim()) {
      alert('请填写学号和密码');
      return;
    }
    setIsLoading(true);
    // 从localStorage查找用户
    setTimeout(() => {
      const userDataStr = localStorage.getItem('user_' + studentId);
      if (!userDataStr) {
        alert('用户不存在，请先注册');
        setIsLoading(false);
        return;
      }
      const userData = JSON.parse(userDataStr);
      handleLogin(userData);
      showSuccess('登录成功！欢迎来到Mentara');
      setIsLoading(false);
    }, 1000);
  };

  return (
    <div className="page-container">
      {/* 动态渐变背景效果 */}
      <div className="page-background"></div>
      
      <GlassCard className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <div className="auth-logo-text">
              M
            </div>
          </div>
          <h1 className="auth-title gradient-text">
            Mentara
          </h1>
          <p className="auth-subtitle">
            分享快乐，发泄不满，我们在这里等你。
          </p>
          <p className="auth-tagline">
            MENTAL HEALTH COMMUNITY
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label">学号</label>
            <input
              type="text"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              placeholder="请输入学号"
              className="form-input"
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
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="auth-button"
          >
            {isLoading ? (
              <>
                <div className="loading-spinner loading-spinner-small"></div>
                登录中...
              </>
            ) : (
              '登录'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <span>还没有账号？</span>
          <Link to="/register" className="auth-link-inline">
            立即注册
          </Link>
        </div>
      </GlassCard>
    </div>
  );
} 