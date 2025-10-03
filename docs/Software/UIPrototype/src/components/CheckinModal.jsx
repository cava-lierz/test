import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import GlassCard from './GlassCard';
import AnimatedButton from './AnimatedButton';

export default function CheckinModal({ isOpen, onClose, onCheckin }) {
  const { user } = useAuth();
  const [rating, setRating] = useState(0);
  const [moodText, setMoodText] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [moodHistory, setMoodHistory] = useState([]);

  // 模拟获取用户心情历史数据
  useEffect(() => {
    // 从localStorage获取历史数据，如果没有则生成模拟数据
    const savedHistory = localStorage.getItem(`moodHistory_${user.name}`);
    if (savedHistory) {
      setMoodHistory(JSON.parse(savedHistory));
    } else {
      // 生成过去7天的模拟数据
      const mockHistory = [];
      const today = new Date();
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        mockHistory.push({
          date: date.toISOString().split('T')[0],
          rating: Math.floor(Math.random() * 3) + 3, // 3-5星
          text: ['今天心情不错', '有点小烦恼', '很开心的日子', '平静的一天', '充满希望'][Math.floor(Math.random() * 5)]
        });
      }
      setMoodHistory(mockHistory);
    }
  }, [user.name]);

  const handleSubmit = async () => {
    if (rating === 0) {
      alert('请选择今天的心情评分');
      return;
    }

    setIsSubmitting(true);
    
    // 模拟提交请求
    setTimeout(() => {
      const today = new Date().toISOString().split('T')[0];
      const newMood = {
        date: today,
        rating: rating,
        text: moodText || '今天的心情'
      };
      
      const updatedHistory = [...moodHistory, newMood];
      setMoodHistory(updatedHistory);
      
      // 保存到localStorage
      localStorage.setItem(`moodHistory_${user.name}`, JSON.stringify(updatedHistory));
      
      setIsSubmitting(false);
      onCheckin && onCheckin(newMood);
      onClose();
    }, 1000);
  };

  const getRatingEmoji = (rating) => {
    const emojis = ['😢', '😔', '😐', '😊', '😄'];
    return emojis[rating - 1] || '😐';
  };

  const getRatingText = (rating) => {
    const texts = ['很差', '不好', '一般', '不错', '很好'];
    return texts[rating - 1] || '请选择';
  };

  const getRatingColor = (rating) => {
    const colors = ['#ff6b6b', '#ffa726', '#ffd54f', '#81c784', '#4caf50'];
    return colors[rating - 1] || '#e0e0e0';
  };

  if (!isOpen) return null;

  return (
    <div className="checkin-modal-overlay">
      <GlassCard className="checkin-modal-card">
        <div className="checkin-modal-content">
          {/* 标题 */}
          <div className="checkin-modal-header">
            <h2 className="checkin-modal-title">
              🌟 今日心情打卡
            </h2>
            <p className="checkin-modal-subtitle">
              记录一下今天的心情吧，{user.name}！
            </p>
          </div>

          {/* 心情评分 */}
          <div className="checkin-rating-section">
            <label className="checkin-rating-label">
              今天的心情如何？
            </label>
            <div className="checkin-rating-buttons">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setRating(star)}
                  className={`checkin-rating-button ${rating === star ? 'active' : ''}`}
                  style={{
                    borderColor: rating === star ? getRatingColor(star) : '#e0e0e0',
                    background: rating === star ? `${getRatingColor(star)}15` : 'transparent',
                  }}
                  onMouseOver={(e) => {
                    if (rating !== star) {
                      e.target.style.borderColor = getRatingColor(star);
                      e.target.style.background = `${getRatingColor(star)}10`;
                    }
                  }}
                  onMouseOut={(e) => {
                    if (rating !== star) {
                      e.target.style.borderColor = '#e0e0e0';
                      e.target.style.background = 'transparent';
                    }
                  }}
                >
                  <span className="checkin-rating-star">⭐</span>
                  <span 
                    className="checkin-rating-text"
                    style={{
                      color: rating === star ? getRatingColor(star) : '#666',
                      fontWeight: rating === star ? '600' : '400'
                    }}
                  >
                    {getRatingText(star)}
                  </span>
                </button>
              ))}
            </div>
            {rating > 0 && (
              <div 
                className="checkin-rating-preview"
                style={{
                  background: `${getRatingColor(rating)}15`,
                  border: `1px solid ${getRatingColor(rating)}30`
                }}
              >
                <span className="checkin-rating-preview-emoji">
                  {getRatingEmoji(rating)}
                </span>
                <span 
                  className="checkin-rating-preview-text"
                  style={{ color: getRatingColor(rating) }}
                >
                  {getRatingText(rating)}
                </span>
              </div>
            )}
          </div>

          {/* 心情文字 */}
          <div className="checkin-text-section">
            <label className="checkin-text-label">
              想说的话（可选）
            </label>
            <textarea
              value={moodText}
              onChange={(e) => setMoodText(e.target.value)}
              placeholder="分享今天的心情或想法..."
              className="checkin-text-input"
              onFocus={(e) => {
                e.target.style.borderColor = '#667eea';
                e.target.style.boxShadow = '0 0 0 3px rgba(102, 126, 234, 0.1)';
              }}
              onBlur={(e) => {
                e.target.style.borderColor = '#e2e8f0';
                e.target.style.boxShadow = 'none';
              }}
            />
          </div>

          {/* 心情趋势图 */}
          <div className="checkin-chart-section">
            <h3 className="checkin-chart-title">
              📈 最近心情趋势
            </h3>
            <div className="checkin-chart-container">
              {moodHistory.slice(-7).map((mood, index) => (
                <div key={index} className="checkin-chart-item">
                  <div 
                    className="checkin-chart-bar"
                    style={{
                      height: `${(mood.rating / 5) * 80}px`,
                      background: `linear-gradient(to top, ${getRatingColor(mood.rating)}, ${getRatingColor(mood.rating)}80)`
                    }}
                  />
                  <span className="checkin-chart-date">
                    {new Date(mood.date).getDate()}
                  </span>
                </div>
              ))}
            </div>
            <div className="checkin-chart-legend">
              <span>😢 1星</span>
              <span>😄 5星</span>
            </div>
          </div>

          {/* 按钮 */}
          <div className="checkin-modal-actions">
            <AnimatedButton
              variant="secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              稍后再说
            </AnimatedButton>
            <AnimatedButton
              onClick={handleSubmit}
              disabled={isSubmitting || rating === 0}
            >
              {isSubmitting ? (
                <>
                  <div className="loading-spinner loading-spinner-small"></div>
                  打卡中...
                </>
              ) : (
                '完成打卡'
              )}
            </AnimatedButton>
          </div>
        </div>
      </GlassCard>
    </div>
  );
} 