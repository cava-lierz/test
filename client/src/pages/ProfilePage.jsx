import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import GenericModal from '../components/GenericModal';
import WeekControls from '../components/WeekControls';
import MoodChart from '../components/MoodChart';
import MoodList from '../components/MoodList';
import WeeklyMoodStat from '../components/WeeklyMoodStat';
import AvatarUpload from '../components/AvatarUpload';
import AvatarPreviewModal from '../components/AvatarPreviewModal';
import { Link } from 'react-router-dom';
import { postService } from '../services/postService';
import { userAPI, moodAPI } from '../services/api';
import { getUserAvatarUrl } from '../utils/avatarUtils';

export default function ProfilePage() {
  const { user, handleLogout, showConfirm, updateUser } = useAuth();
  const profileTabs = [
    { id: 'mood', label: '心情记录', icon: '📊' },
    { id: 'posts', label: '我的动态', icon: '📝' },
    { id: 'info', label: '个人信息设置', icon: '👤' },
  ];
  const [activeTab, setActiveTab] = useState(profileTabs[0].id);
  const [weekData, setWeekData] = useState([]);
  const [currentWeekData, setCurrentWeekData] = useState([]); // 固定的当前周数据，用于统计
  const [selectedMood, setSelectedMood] = useState(null);
  const [recentPosts, setRecentPosts] = useState([]);
  const [userStats, setUserStats] = useState(null);
  const [loadingStats, setLoadingStats] = useState(true);
  const [moodLoading, setMoodLoading] = useState(false);
  const [weekLoading, setWeekLoading] = useState(false);
  const [currentWeek, setCurrentWeek] = useState(0);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [isProfilePublic, setIsProfilePublic] = useState(true);
  const [privacyLoading, setPrivacyLoading] = useState(false);
  const [showAvatarPreview, setShowAvatarPreview] = useState(false);
  const [currentPreviewUrl, setCurrentPreviewUrl] = useState(null);
  const [nicknameInput, setNicknameInput] = useState(user?.nickname || '');
  const [nicknameSaving, setNicknameSaving] = useState(false);
  const [nicknameSaveMsg, setNicknameSaveMsg] = useState('');

  // 获取当前周数据用于统计（只在初次进入心情标签时获取）
  useEffect(() => {
    const fetchCurrentWeekForStats = async () => {
      if (activeTab === 'mood' && user?.id && currentWeekData.length === 0) {
        try {
          const response = await moodAPI.getCurrentWeek();
          setCurrentWeekData(response || []);
        } catch (error) {
          console.error('获取当前周心情记录失败:', error);
          setCurrentWeekData([]);
        }
      }
    };

    fetchCurrentWeekForStats();
  }, [activeTab, user?.id, currentWeekData.length]);

  // 获取心情记录数据用于显示
  useEffect(() => {
    const fetchMoodData = async () => {
      if (activeTab === 'mood' && user?.id) {
        // 区分初始加载和切换周的加载
        if (isInitialLoad) {
          setMoodLoading(true);
        } else {
          setWeekLoading(true);
        }
        
        try {
          let response;
          if (currentWeek === 0) {
            response = await moodAPI.getCurrentWeek();
          } else {
            response = await moodAPI.getWeek(currentWeek);
          }
          setWeekData(response || []);
        } catch (error) {
          console.error('获取心情记录失败:', error);
          setWeekData([]);
        } finally {
          setMoodLoading(false);
          setWeekLoading(false);
          setIsInitialLoad(false);
        }
      }
    };

    fetchMoodData();
  }, [activeTab, user?.id, currentWeek, isInitialLoad]);

  // 获取用户统计信息和隐私设置
  useEffect(() => {
    const fetchUserData = async () => {
      if (user?.id) {
        try {
          setLoadingStats(true);
          console.log('正在获取用户统计信息...');
          const stats = await userAPI.getCurrentUserStats();
          console.log('获取到的用户统计信息:', stats);
          setUserStats(stats);
          
          // 获取用户隐私设置
          const userData = await userAPI.getUserById(user.id);
          if (userData && userData.isProfilePublic !== undefined) {
            setIsProfilePublic(userData.isProfilePublic);
          }
        } catch (error) {
          console.error('获取用户信息失败:', error);
        } finally {
          setLoadingStats(false);
        }
      }
    };
    
    fetchUserData();
  }, [user?.id]);

  useEffect(() => {
    if (activeTab === 'posts' && user?.id) {
      postService.getPostsByUserId(user.id, 0, 10).then(res => {
        // 兼容分页结构
        const posts = Array.isArray(res.content) ? res.content : res;
        setRecentPosts(posts);
      });
    }
  }, [activeTab, user?.id]);

  useEffect(() => {
    setNicknameInput(user?.nickname || '');
  }, [user?.nickname]);

  const handleLogoutClick = () => {
    showConfirm('确定要退出登录吗？', () => {
      handleLogout();
    });
  };

  const getRatingEmoji = (rating) => {
    const emojis = ['😢', '😔', '😐', '😊', '😄'];
    return emojis[rating - 1] || '😐';
  };

  const getRatingText = (rating) => {
    const texts = ['很差', '不好', '一般', '不错', '很好'];
    return texts[rating - 1] || '未知';
  };

  // 切换到前一周
  const handlePreviousWeek = () => {
    setCurrentWeek(currentWeek + 1);
  };

  // 切换到后一周
  const handleNextWeek = () => {
    if (currentWeek > 0) {
      setCurrentWeek(currentWeek - 1);
    }
  };

  // 处理头像更新
  const handleAvatarChange = (newAvatarUrl) => {
    // 更新认证上下文中的用户信息
    if (user) {
      const updatedUser = { ...user, avatar: newAvatarUrl };
      updateUser(updatedUser);
      
      // 触发全局头像更新事件，通知其他组件
      window.dispatchEvent(new CustomEvent('avatar-updated', {
        detail: { avatarUrl: newAvatarUrl, user: updatedUser }
      }));
    }
    // 清除预览URL
    setCurrentPreviewUrl(null);
  };

  // 处理头像预览更新
  const handleAvatarPreviewChange = (previewUrl) => {
    setCurrentPreviewUrl(previewUrl);
  };

  // 处理隐私设置更新
  const handlePrivacyChange = async (newValue) => {
    try {
      setPrivacyLoading(true);
      await userAPI.updatePrivacySettings(newValue);
      setIsProfilePublic(newValue);
      showConfirm('隐私设置已更新', () => {});
    } catch (error) {
      console.error('更新隐私设置失败:', error);
      showConfirm('更新隐私设置失败，请重试', () => {});
    } finally {
      setPrivacyLoading(false);
    }
  };

  const stats = [
    { label: '发布动态', value: loadingStats ? '--' : (userStats?.postsCount || 0), icon: '📝' },
    { label: '发布评论', value: loadingStats ? '--' : (userStats?.commentsCount || 0), icon: '💬' },
    { label: '获得点赞', value: loadingStats ? '--' : (userStats?.totalLikes || 0), icon: '👍' }
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
              <div className="profile-avatar-section" title="点击查看头像">
                <img
                  src={getUserAvatarUrl(user)}
                  alt={user.nickname || user.username}
                  className="profile-avatar avatar-clickable"
                  onClick={() => setShowAvatarPreview(true)}
                  onError={(e) => {
                    console.error('头像加载失败:', e.target.src);
                    console.log('用户对象:', user);
                  }}
                />
                <div className="profile-avatar-badge">
                  ✓
                </div>
              </div>
              
              <div className="profile-info">
                <h1 className="profile-name">
                  {user.nickname || user.username}
                </h1>
                <p className="profile-join-date">
                  加入时间：{userStats?.createdAt ? new Date(userStats.createdAt).toLocaleDateString() : '加载中...'}
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
              <WeeklyMoodStat currentWeekData={currentWeekData} />
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
              {/* 本周心情组件 */}

            </div>
          </div>
        </div>

        {/* 标签页导航 */}
        <div className="profile-tabs">
          <div className="profile-tabs-nav">
            {profileTabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id);
                  // 切换到心情记录标签时重置初始加载状态
                  if (tab.id === 'mood') {
                    setIsInitialLoad(true);
                  }
                }}
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
              
              {moodLoading ? (
                <div className="profile-tab-content-loading">
                  <div className="loading-spinner"></div>
                  <p>加载心情记录中...</p>
                </div>
              ) : (
                <div className="profile-tab-content-inner">
                  {/* 周切换控制 */}
                  <WeekControls
                    currentWeek={currentWeek}
                    onPreviousWeek={handlePreviousWeek}
                    onNextWeek={handleNextWeek}
                    isLoading={weekLoading}
                  />

                  {/* 心情趋势图 */}
                  <div className={`mood-content-section ${weekLoading ? 'loading' : ''}`}>
                    <MoodChart weekData={weekData} />

                    {/* 心情记录列表 */}
                    <MoodList 
                      weekData={weekData} 
                      onMoodClick={setSelectedMood}
                    />
                    
                    {/* 切换周时的加载遮罩 */}
                    {weekLoading && (
                      <div className="week-loading-overlay">
                        <div className="week-loading-spinner"></div>
                      </div>
                    )}
                  </div>
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
                {recentPosts.length === 0 ? (
                  <div style={{ color: '#aaa', textAlign: 'center', marginTop: 24 }}>暂无动态</div>
                ) : (
                  recentPosts.map((post) => (
                    <Link to={`/post/${post.id}`} key={post.id} style={{ textDecoration: 'none', color: 'inherit' }}>
                      <div className="profile-tab-item profile-tab-item-link" style={{ display: 'flex', flexDirection: 'column', alignItems: 'stretch' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          {/* 心情emoji */}
                          <span style={{ fontSize: 20 }}>{getRatingEmoji(post.mood === 'HAPPY' ? 5 : post.mood === 'SAD' ? 1 : 3)}</span>
                          {/* 内容 */}
                          <p className="profile-tab-item-text" style={{ margin: 0, flex: 1 }}>
                            {post.content}
                          </p>
                        </div>
                        {/* 标签单独一行，放在footer上方 */}
                        {Array.isArray(post.tags) && post.tags.length > 0 && (
                          <div style={{ margin: '8px 0 0 0', display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                            {post.tags.map(tag => (
                              <span
                                key={tag.id}
                                className={`post-tag-btn tag-color-${tag.tag}`}
                                style={{ borderColor: tag.color }}
                              >
                                #{tag.tag}
                              </span>
                            ))}
                          </div>
                        )}
                        <div className="profile-tab-item-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <span>{new Date(post.createdAt).toLocaleString()}</span>
                          <span style={{ display: 'flex', gap: 12 }}>
                            <span>👍 {post.likesCount}</span>
                            <span>💬 {post.commentsCount}</span>
                          </span>
                        </div>
                      </div>
                    </Link>
                  ))
                )}
              </div>
            </div>
          )}

          {activeTab === 'info' && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">
                个人信息设置
              </h3>
              <div className="profile-tab-content-inner" style={{ background: '#fff', borderRadius: 16, boxShadow: '0 2px 8px rgba(0,0,0,0.04)', padding: 32, minWidth: 260, display: 'flex', flexDirection: 'column', gap: 0 }}>
                {/* 头像上传 */}
                <div style={{ marginBottom: 0 }}>
                  <AvatarUpload 
                    onAvatarChange={handleAvatarChange}
                    currentAvatar={user?.avatar}
                    onPreviewChange={handleAvatarPreviewChange}
                  />
                </div>
                {/* 昵称设置 */}
                <div className="profile-info-setting-item" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap', margin: '8px 0 0 0' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 180 }}>
                    <h4 htmlFor="nickname-input" style={{ fontWeight: 600, fontSize: 18, color: '#222', margin: 0, marginBottom: 4 }}>昵称</h4>
                    <input
                      id="nickname-input"
                      type="text"
                      value={nicknameInput}
                      maxLength={20}
                      onChange={e => setNicknameInput(e.target.value)}
                      placeholder="请输入昵称"
                      style={{
                        padding: '8px 14px',
                        borderRadius: 8,
                        border: '1.5px solid #d0d7de',
                        fontSize: 15,
                        outline: 'none',
                        transition: 'border 0.2s',
                        boxShadow: nicknameSaving ? '0 0 0 2px #b3d4fc' : 'none',
                        background: nicknameSaving ? '#f5faff' : '#fff',
                        width: '100%',
                        minWidth: 120,
                        maxWidth: 260,
                      }}
                      disabled={nicknameSaving}
                    />
                    {nicknameSaveMsg && (
                      <span
                        style={{
                          marginTop: 8,
                          fontSize: 15,
                          fontWeight: 500,
                          color: nicknameSaveMsg === '保存成功' ? '#27ae60' : '#e74c3c',
                          opacity: nicknameSaveMsg ? 1 : 0,
                          transition: 'opacity 0.3s',
                          animation: 'fadeInScale 0.5s',
                        }}
                      >
                        {nicknameSaveMsg}
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'flex-end', minWidth: 100, height: '100%' }}>
                    <button
                      className="btn-select"
                      style={{ width: 100, alignSelf: 'flex-end', marginLeft: 24 }}
                      disabled={nicknameSaving || nicknameInput.trim() === '' || nicknameInput === user?.nickname}
                      onClick={async () => {
                        setNicknameSaving(true);
                        setNicknameSaveMsg('');
                        try {
                          await userAPI.updateProfile({ nickname: nicknameInput.trim() });
                          updateUser({ ...user, nickname: nicknameInput.trim() });
                          setNicknameSaveMsg('保存成功');
                        } catch (e) {
                          setNicknameSaveMsg('保存失败，请重试');
                        } finally {
                          setNicknameSaving(false);
                          setTimeout(() => setNicknameSaveMsg(''), 2000);
                        }
                      }}
                    >{nicknameSaving ? '保存中...' : '保存'}</button>
                  </div>
                </div>
                {/* 个人信息公开设置（原隐私设置） */}
                <div className="privacy-setting-item" style={{ margin: '32px 0 0 0', background: 'none', border: 'none', borderRadius: 0, boxShadow: 'none', padding: 0 }}>
                  <div className="privacy-setting-info">
                    <h4 className="privacy-setting-title">个人信息公开</h4>
                    <p className="privacy-setting-description">
                      {isProfilePublic 
                        ? '其他用户可以看到您的个人信息、动态和心情记录' 
                        : '其他用户无法查看您的个人信息、动态和心情记录'
                      }
                    </p>
                  </div>
                  <div className="privacy-setting-control">
                    <label className="privacy-toggle">
                      <input
                        type="checkbox"
                        checked={isProfilePublic}
                        onChange={(e) => handlePrivacyChange(e.target.checked)}
                        disabled={privacyLoading}
                      />
                      <span className="privacy-toggle-slider"></span>
                    </label>
                    {privacyLoading && (
                      <div className="privacy-loading">
                        <div className="loading-spinner"></div>
                      </div>
                    )}
                  </div>
                </div>
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
            <p className="mood-modal-text">{selectedMood.note || '今天的心情'}</p>
            <span className="mood-modal-date">{new Date(selectedMood.checkinDate).toLocaleString()}</span>
          </div>
        )}
      </GenericModal>

      {/* 头像预览模态框 */}
      <AvatarPreviewModal
        isOpen={showAvatarPreview}
        onClose={() => setShowAvatarPreview(false)}
        avatarUrl={currentPreviewUrl || getUserAvatarUrl(user)}
        userName={`${user?.nickname || user?.username}的头像`}
      />
    </>
  );
} 