import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import GlassCard from '../components/GlassCard';

export default function RegisterPage() {
  const [studentId, setStudentId] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { showSuccess } = useAuth();
  const navigate = useNavigate();

  // 生成随机昵称（保护隐私）
  function generateNickname() {
    const adjectives = [
      '快乐的', '温暖的', '勇敢的', '智慧的', '善良的', '活泼的', '沉静的', '乐观的',
      '友善的', '创意的', '坚强的', '温柔的', '幽默的', '认真的', '细心的', '开朗的'
    ];
    const nouns = [
      '小熊', '小猫', '小鸟', '小鹿', '小兔', '小狐', '小虎', '小狼',
      '星星', '月亮', '彩虹', '花朵', '蝴蝶', '海豚', '独角兽', '小天使'
    ];
    
    const adjective = adjectives[Math.floor(Math.random() * adjectives.length)];
    const noun = nouns[Math.floor(Math.random() * nouns.length)];
    const number = Math.floor(Math.random() * 1000);
    
    return `${adjective}${noun}${number}`;
  }

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
    
    if (!password.trim()) {
      setError('请输入密码');
      return;
    }
    
    if (!confirmPassword.trim()) {
      setError('请确认密码');
      return;
    }
    
    if (password !== confirmPassword) {
      setError('两次输入的密码不一致');
      return;
    }
    
    if (password.length < 6) {
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
      // 调用注册API - 将学号作为用户名发送给后端
      await authAPI.register({
        username: studentId.trim(), // 将学号映射为用户名
        email: email.trim(),
        password: password,
        nickname: generateNickname() // 生成随机昵称
      });

      // 注册成功后，显示成功消息
      showSuccess('注册成功！请登录您的账户');
      
      // 跳转到登录页面
    setTimeout(() => {
        navigate('/login');
      }, 1500);

    } catch (error) {
      console.error('注册失败:', error);
      
      // 根据错误类型设置不同的错误消息
      if (error.message.includes('Username is already taken')) {
        setError('学号已被注册，请检查学号或前往登录');
      } else if (error.message.includes('Email is already in use')) {
        setError('邮箱已被注册，请使用其他邮箱或前往登录');
      } else if (error.message.includes('Communications link failure')) {
        setError('无法连接到服务器，请检查网络连接');
      } else {
        setError(error.message || '注册失败，请稍后重试');
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
            分享快乐，发泄不满，我们在这里等你。
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
              placeholder="请输入邮箱"
              className="form-input"
              disabled={isLoading}
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
              disabled={isLoading}
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
                注册中
                <div className="loading-spinner"></div>
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
      </GlassCard>
    </div>
  );
} 