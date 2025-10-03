import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import GlassCard from '../components/GlassCard';

export default function ForgotPasswordPage() {
  const [studentId, setStudentId] = useState('');
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { showSuccess } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 清除之前的错误信息
    setError('');
    
    // 表单验证
    if (!studentId.trim()) {
      setError('请输入学号');
      return;
    }
    
    if (!email.trim()) {
      setError('请输入邮箱');
      return;
    }
    
    if (!newPassword.trim()) {
      setError('请输入新密码');
      return;
    }
    
    if (!confirmPassword.trim()) {
      setError('请确认新密码');
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError('两次输入的密码不一致');
      return;
    }
    
    if (newPassword.length < 6) {
      setError('密码长度至少6位');
      return;
    }
    
    // 简单邮箱格式校验
    if (!/^\S+@\S+\.\S+$/.test(email)) {
      setError('请输入有效的邮箱地址');
      return;
    }

    setIsLoading(true);

    try {
      // 调用重置密码API
      await authAPI.resetPassword({
        studentId: studentId.trim(),
        email: email.trim(),
        newPassword: newPassword,
        confirmPassword: confirmPassword
      });

      // 重置成功后，显示成功消息
      showSuccess('密码重置成功！请使用新密码登录');
      
      // 跳转到登录页面
      setTimeout(() => {
        navigate('/login');
      }, 1500);

    } catch (error) {
      console.error('密码重置失败:', error);
      
      // 根据错误类型设置不同的错误消息
      if (error.message.includes('学号不存在')) {
        setError('学号不存在，请检查学号或前往注册');
      } else if (error.message.includes('学号与邮箱不匹配')) {
        setError('学号与邮箱不匹配，请检查输入信息');
      } else if (error.message.includes('新密码和确认密码不一致')) {
        setError('新密码和确认密码不一致');
      } else if (error.message.includes('Communications link failure')) {
        setError('无法连接到服务器，请检查网络连接');
      } else {
        setError(error.message || '密码重置失败，请稍后重试');
      }
    } finally {
      setIsLoading(false);
    }
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
            重置您的密码，重新开始。
          </p>
          <p className="auth-tagline">
            MENTAL HEALTH COMMUNITY
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {/* 错误消息显示 */}
          {error && (
            <div className="error-message" style={{
              color: '#ff6b6b',
              backgroundColor: 'rgba(255, 107, 107, 0.1)',
              border: '1px solid rgba(255, 107, 107, 0.3)',
              borderRadius: '8px',
              padding: '12px',
              marginBottom: '20px',
              fontSize: '14px'
            }}>
              {error}
            </div>
          )}

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
              disabled={isLoading}
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
              placeholder="请输入注册时的邮箱"
              className="form-input"
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              新密码
            </label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="请输入新密码（至少6位）"
              className="form-input"
              disabled={isLoading}
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              确认新密码
            </label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="请再次输入新密码"
              className="form-input"
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="auth-button"
          >
            {isLoading ? (
              <>
                重置中
                <div className="loading-spinner"></div>
              </>
            ) : (
              '重置密码'
            )}
          </button>
        </form>

        <div className="auth-footer">
          <span>想起密码了？</span>
          <Link to="/login" className="auth-link-inline">
            返回登录
          </Link>
        </div>
      </GlassCard>
    </div>
  );
} 