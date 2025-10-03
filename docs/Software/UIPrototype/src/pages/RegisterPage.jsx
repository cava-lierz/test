import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import GlassCard from '../components/GlassCard';

export default function RegisterPage() {
  const [studentId, setStudentId] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { handleLogin, showSuccess } = useAuth();

  // 自动生成用户名
  function generateUsername() {
    return '用户' + Math.floor(100000 + Math.random() * 900000);
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    // 表单验证
    if (!studentId.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      alert('请填写所有字段');
      return;
    }
    if (password !== confirmPassword) {
      alert('两次输入的密码不一致');
      return;
    }
    if (password.length < 6) {
      alert('密码长度至少6位');
      return;
    }
    // 简单邮箱格式校验
    if (!/^\S+@\S+\.\S+$/.test(email)) {
      alert('请输入有效的邮箱地址');
      return;
    }
    setIsLoading(true);
    // 模拟注册请求
    setTimeout(() => {
      const username = generateUsername();
      const userData = {
        studentId: studentId,
        email: email,
        name: username, // 自动生成
        avatar: `https://i.pravatar.cc/150?u=${username}`,
        joinDate: '2024年1月',
        posts: 0,
        likes: 0,
        moodAverage: '0.0'
      };
      // 保存到localStorage
      localStorage.setItem('user_' + studentId, JSON.stringify(userData));
      handleLogin(userData);
      showSuccess('注册成功！欢迎来到Mentara');
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
            <label className="form-label">
              学号
            </label>
            <input
              type="text"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              placeholder="请输入学号"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              邮箱
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="请输入邮箱"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              密码
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码（至少6位）"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              确认密码
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="请再次输入密码"
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
                注册中...
              </>
            ) : (
              '注册'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <span>已有账号？</span>
          <Link to="/login" className="auth-link-inline">
            立即登录
          </Link>
        </div>

        {/* 可选：注册后展示自动生成的用户名 */}
        {username && (
          <div style={{marginTop: 16, color: '#666', fontSize: 14}}>
            您的昵称：{username}
          </div>
        )}
      </GlassCard>
    </div>
  );
} 