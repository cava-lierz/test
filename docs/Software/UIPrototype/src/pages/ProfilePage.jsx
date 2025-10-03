import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import GenericModal from '../components/GenericModal';
import { Link, useLocation } from 'react-router-dom';
import { notifications } from '../utils/notifications';

export default function ProfilePage() {
  const { user, handleLogout, showConfirm } = useAuth();
  const profileTabs = [
    { id: 'mood', label: '心情记录', icon: '📊' },
    { id: 'posts', label: '我的动态', icon: '📝' },
    { id: 'notifications', label: '通知', icon: '🔔' }
  ];
  const [activeTab, setActiveTab] = useState(profileTabs[0].id);
  const [moodHistory, setMoodHistory] = useState([]);
  const [selectedMood, setSelectedMood] = useState(null);
  const location = useLocation();

  // 获取用户心情历史数据
  useEffect(() => {
    const savedHistory = localStorage.getItem(`moodHistory_${user.name}`);
    if (savedHistory) {
      setMoodHistory(JSON.parse(savedHistory));
    }
  }, [user.name]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tab = params.get('tab');
    const nid = params.get('nid');
    if (tab === 'notifications') {
      setActiveTab('notifications');
      if (nid) {
        setTimeout(() => {
          const el = document.getElementById(`profile-notification-${nid}`);
          if (el) {
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            el.classList.add('highlight-notification');
            setTimeout(() => el.classList.remove('highlight-notification'), 2000);
          }
        }, 300);
      }
    }
  }, [location.search]);

  const handleLogoutClick = () => {
    showConfirm('确定要退出登录吗？', () => {
      handleLogout();
    });
  };

  const getRatingColor = (rating) => {
    const colors = ['#ff6b6b', '#ffa726', '#ffd54f', '#81c784', '#4caf50'];
    return colors[rating - 1] || '#e0e0e0';
  };

  const getRatingEmoji = (rating) => {
    const emojis = ['😢', '😔', '😐', '😊', '😄'];
    return emojis[rating - 1] || '😐';
  };

  const getRatingText = (rating) => {
    const texts = ['很差', '不好', '一般', '不错', '很好'];
    return texts[rating - 1] || '未知';
  };

  const stats = [
    { label: '发布动态', value: user.posts || 0, icon: '📝' },
    { label: '获得点赞', value: user.likes || 0, icon: '👍' },
    { label: '心情评分', value: user.moodAverage || '0.0', icon: '😊' },
    { label: '加入天数', value: '30', icon: '📅' }
  ];

  const recentPosts = [
    { id: 1, content: '今天心情不错，和朋友们一起去了公园！', time: '2小时前', likes: 15 },
    { id: 2, content: '学习压力有点大，但是我相信坚持下去会有好结果。', time: '1天前', likes: 23 },
    { id: 3, content: '感谢身边的朋友们，有你们真好！', time: '3天前', likes: 18 }
  ];

  return (
    <>
      <div className="profile-container">
        {/* 个人信息卡片 */}
        <div className="profile-header">
          {/* 背景装饰 */}
          <div className="profile-header-bg"></div>
          
          <div className="profile-header-content">
            <div className="profile-header-main">
              <div className="profile-avatar-section">
                <img
                  src={user.avatar}
                  alt={user.name}
                  className="profile-avatar"
                />
                <div className="profile-avatar-badge">
                  ✓
                </div>
              </div>
              
              <div className="profile-info">
                <h1 className="profile-name">
                  {user.name}
                </h1>
                <p className="profile-join-date">
                  加入时间：{user.joinDate}
                </p>
              </div>
              
              <button
                onClick={handleLogoutClick}
                className="logout-button"
              >
                退出登录
              </button>
            </div>
            
            {/* 统计数据 */}
            <div className="profile-stats">
              {stats.map((stat, index) => (
                <div key={index} className="stat-item">
                  <div className="stat-number">
                    {stat.icon} {stat.value}
                  </div>
                  <div className="stat-label">
                    {stat.label}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 标签页导航 */}
        <div className="profile-tabs">
          <div className="profile-tabs-nav">
            {profileTabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`profile-tab ${activeTab === tab.id ? 'active' : ''}`}
              >
                <span>{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </div>

          {/* 标签页内容 */}
          {activeTab === 'mood' && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">
                心情记录
              </h3>
              
              {moodHistory.length > 0 ? (
                <div className="profile-tab-content-inner">
                  {/* 心情趋势图 */}
                  <div className="mood-chart-section">
                    <h4 className="mood-chart-title">
                      📈 心情趋势图
                    </h4>
                    <div className="mood-chart-container">
                      {moodHistory.slice(-14).map((mood, index) => (
                        <div key={index} className="mood-chart-item">
                          <div
                            className="mood-chart-bar"
                            style={{
                              height: `${(mood.rating / 5) * 100}px`,
                              background: `linear-gradient(to top, ${getRatingColor(mood.rating)}, ${getRatingColor(mood.rating)}80)`
                            }}
                          ></div>
                          <span className="mood-chart-label">
                            {new Date(mood.date).getDate()}
                          </span>
                        </div>
                      ))}
                    </div>
                    <div className="mood-chart-legend">
                      <span>😢 1星</span>
                      <span>😄 5星</span>
                    </div>
                  </div>

                  {/* 心情记录列表 */}
                  <div className="mood-list-section">
                    <h4 className="mood-list-title">
                      📝 最近心情记录
                    </h4>
                    <div className="mood-list">
                      {moodHistory.slice(-10).reverse().map((mood, index) => (
                        <div key={index} className="mood-list-item" onClick={() => setSelectedMood(mood)}>
                          <div className="mood-list-emoji">
                            {getRatingEmoji(mood.rating)}
                          </div>
                          <div className="mood-list-content">
                            <div className="mood-list-header">
                              <span className="mood-list-title-text">
                                {getRatingText(mood.rating)}
                              </span>
                              <span className="mood-list-rating">
                                {mood.rating} 星
                              </span>
                            </div>
                            <p className="mood-list-description">
                              {mood.text}
                            </p>
                            <span className="mood-list-date">
                              {new Date(mood.date).toLocaleDateString()}
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="profile-tab-content-empty">
                  <div className="profile-tab-content-empty-icon">📊</div>
                  <p className="profile-tab-content-empty-title">还没有心情记录</p>
                  <p className="profile-tab-content-empty-description">去社区主页进行每日打卡吧！</p>
                </div>
              )}
            </div>
          )}

          {activeTab === 'posts' && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">
                我的动态
              </h3>
              <div className="profile-tab-content-inner">
                {recentPosts.map((post) => (
                  <Link to={`/post/${post.id}`} key={post.id} style={{ textDecoration: 'none', color: 'inherit' }}>
                    <div className="profile-tab-item profile-tab-item-link">
                      <p className="profile-tab-item-text">
                        {post.content}
                      </p>
                      <div className="profile-tab-item-footer">
                        <span>{post.time}</span>
                        <span>👍 {post.likes}</span>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">通知栏</h3>
              <div className="profile-tab-content-inner">
                {notifications.length > 0 ? (
                  notifications.map((notification) => (
                    <div
                      key={notification.id}
                      id={`profile-notification-${notification.id}`}
                      className={`profile-notification-item ${!notification.read ? "unread" : ""}`}
                    >
                      <div className="profile-notification-title">{notification.title}</div>
                      <div className="profile-notification-message">{notification.message}</div>
                      <div className="profile-notification-time">{notification.time}</div>
                    </div>
                  ))
                ) : (
                  <div className="profile-notification-empty">暂无通知</div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
      <GenericModal isOpen={!!selectedMood} onClose={() => setSelectedMood(null)}>
        {selectedMood && (
          <div className="mood-modal-content">
            <div className="mood-modal-header">
              <span className="mood-modal-emoji">{getRatingEmoji(selectedMood.rating)}</span>
              <h3 className="mood-modal-title">{getRatingText(selectedMood.rating)} ({selectedMood.rating} 星)</h3>
            </div>
            <p className="mood-modal-text">{selectedMood.text}</p>
            <span className="mood-modal-date">{new Date(selectedMood.date).toLocaleString()}</span>
          </div>
        )}
      </GenericModal>
    </>
  );
} 