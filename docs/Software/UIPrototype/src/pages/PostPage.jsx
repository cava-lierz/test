import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function PostPage() {
  const navigate = useNavigate();
  const { user, showConfirm, showSuccess } = useAuth();
  const [content, setContent] = useState('');
  const [mood, setMood] = useState('😊');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const moods = [
    { emoji: '😊', label: '开心' },
    { emoji: '😢', label: '难过' },
    { emoji: '😡', label: '愤怒' },
    { emoji: '😰', label: '焦虑' },
    { emoji: '😴', label: '疲惫' },
    { emoji: '🤗', label: '温暖' },
    { emoji: '💪', label: '充满力量' },
    { emoji: '🎉', label: '兴奋' }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!content.trim()) {
      alert('请输入内容');
      return;
    }

    setIsSubmitting(true);

    // 模拟发布请求
    setTimeout(() => {
      showSuccess('发布成功！');
      setIsSubmitting(false);
      navigate('/community');
    }, 1500);
  };

  const handleCancel = () => {
    if (content.trim()) {
      showConfirm('确定要取消发布吗？未保存的内容将会丢失。', () => {
        navigate('/community');
      });
    } else {
      navigate('/community');
    }
  };

  return (
    <>
      <div className="post-form-container">
        <div className="post-form-card">
          {/* 头部 */}
          <div className="post-form-header">
            <img
              src={user.avatar}
              alt={user.name}
              className="post-form-avatar"
            />
            <div>
              <h1 className="post-form-title">
                分享你的想法
              </h1>
              <p className="post-form-subtitle">
                今天感觉怎么样？和大家分享一下吧
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="post-form">
            {/* 心情选择 */}
            <div className="mood-selector-section">
              <label className="form-label">
                选择心情
              </label>
              <div className="mood-selector">
                {moods.map((moodOption) => (
                  <button
                    key={moodOption.emoji}
                    type="button"
                    onClick={() => setMood(moodOption.emoji)}
                    className={`mood-option ${mood === moodOption.emoji ? 'selected' : ''}`}
                  >
                    <span className="mood-option-emoji">{moodOption.emoji}</span>
                    <span className="mood-option-label">{moodOption.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* 内容输入 */}
            <div className="content-input-section">
              <label className="form-label">
                分享内容
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="写下你的想法、感受或者想要分享的事情..."
                className="post-textarea"
                rows="8"
              />
            </div>

            {/* 操作按钮 */}
            <div className="post-form-actions">
              <button
                type="button"
                onClick={handleCancel}
                className="post-cancel-button"
              >
                取消
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="post-submit-button"
              >
                {isSubmitting ? (
                  <>
                    <div className="loading-spinner loading-spinner-small"></div>
                    发布中...
                  </>
                ) : (
                  '发布动态'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
} 