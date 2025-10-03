import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import { userAPI, postAPI, moodAPI } from "../services/api";
import privateChatService from "../services/privateChatService";
import { getUserAvatarUrl } from "../utils/avatarUtils";
import BlockUserModal from "./BlockUserModal";
import AvatarPreviewModal from "./AvatarPreviewModal";
import FollowButton from "./FollowButton";
import "../styles/components.css";

const UserProfileModal = ({ isOpen, onClose, userId }) => {
  const { user } = useAuth();
  const { isUserBlocked } = useBlock();
  const [userProfile, setUserProfile] = useState(null);
  const [userPosts, setUserPosts] = useState([]);
  const [userMoods, setUserMoods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [blockUserState, setBlockUserState] = useState(null);
  const [showAvatarPreview, setShowAvatarPreview] = useState(false);

  // 获取用户信息
  useEffect(() => {
    if (!isOpen || !userId) return;

    const fetchUserData = async () => {
      setLoading(true);
      try {
        // 获取用户基本信息
        const profileData = await userAPI.getUserById(userId);

        // 检查是否返回了隐私保护信息
        if (
          profileData &&
          profileData.message &&
          profileData.message.includes("不公开个人信息")
        ) {
          setUserProfile({ isPrivate: true, message: profileData.message });
          setUserPosts([]);
          setUserMoods([]);
          return;
        }

        setUserProfile(profileData);

        // 获取用户帖子
        const postsData = await postAPI.getPostsByUserId(userId, 0, 5);
        setUserPosts(postsData.content || []);

        // 获取用户心情记录
        try {
          const moodsData = await moodAPI.getUserMoods(userId, 0, 5);
          console.log("心情记录数据:", moodsData);
          // 处理分页数据结构
          const moods = moodsData.content || moodsData || [];
          setUserMoods(Array.isArray(moods) ? moods : []);
        } catch (error) {
          console.error("获取心情记录失败:", error);
          setUserMoods([]);
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [isOpen, userId]);

  // 处理私聊
  const handleChat = async () => {
    try {
      setLoading(true);
      // 创建或获取私聊聊天室
      const privateRoom = await privateChatService.createOrGetPrivateRoom(
        userId
      );

      // 跳转到私聊页面
      window.location.href = `/private-chat?room=${privateRoom.id}`;
    } catch (error) {
      console.error("创建私聊失败:", error);
      window.showToast && window.showToast("创建私聊失败，请稍后重试");
    } finally {
      setLoading(false);
    }
  };

  // 处理拉黑
  const handleBlock = () => {
    if (!userProfile) return;

    setBlockUserState({
      id: userProfile.id,
      avatar: userProfile.avatar,
      nickname: userProfile.nickname || userProfile.username,
      username: userProfile.username,
      role: userProfile.role,
    });
    setShowBlockModal(true);
  };

  // 根据评分获取心情emoji
  const getMoodEmojiByRating = (rating) => {
    if (!rating) return "😐";
    if (rating >= 4) return "😄";
    if (rating >= 3) return "😊";
    if (rating >= 2) return "😐";
    return "😢";
  };

  // 根据评分获取心情文本
  const getMoodTextByRating = (rating) => {
    if (!rating) return "一般";
    if (rating >= 4) return "很好";
    if (rating >= 3) return "不错";
    if (rating >= 2) return "一般";
    return "较差";
  };

  if (!isOpen || !userId) return null;

  return (
    <>
      <div className="user-profile-modal-overlay" onClick={onClose}>
        <div
          className="user-profile-modal"
          onClick={(e) => e.stopPropagation()}
        >
          {loading ? (
            <div className="user-profile-loading">
              <div className="loading-spinner"></div>
              <p>加载中...</p>
            </div>
          ) : userProfile ? (
            <>
              {/* 检查是否为隐私保护用户 */}
              {userProfile.isPrivate ? (
                <div className="user-profile-private">
                  <div className="user-profile-private-icon">🔒</div>
                  <h2 className="user-profile-private-title">隐私保护</h2>
                  <p className="user-profile-private-message">
                    {userProfile.message}
                  </p>
                  <button className="user-profile-close-btn" onClick={onClose}>
                    ✕
                  </button>
                </div>
              ) : (
                <>
                  {/* 头部信息 */}
                  <div className="user-profile-header">
                    <div className="user-profile-avatar-section">
                      <img
                        src={getUserAvatarUrl(
                          userProfile,
                          "https://i.pravatar.cc/150?u=anon"
                        )}
                        alt={userProfile.nickname || userProfile.username}
                        className="user-profile-avatar avatar-clickable"
                        onClick={() => setShowAvatarPreview(true)}
                        title="点击查看头像"
                      />
                      {userProfile.role === "ADMIN" && (
                        <div className="user-profile-admin-badge">👑</div>
                      )}
                      {userProfile.role === "EXPERT" && (
                        <div className="user-profile-expert-badge">🧑‍⚕️</div>
                      )}
                    </div>
                    <div className="user-profile-info">
                      <h2 className="user-profile-name">
                        {userProfile.nickname || userProfile.username}
                        {userProfile.role === "ADMIN" && (
                          <span className="admin-crown">👑</span>
                        )}
                        {userProfile.role === "EXPERT" && (
                          <span className="expert-badge">🧑‍⚕️</span>
                        )}
                      </h2>
                      <p className="user-profile-join-date">
                        加入时间：
                        {userProfile.createdAt
                          ? new Date(userProfile.createdAt).toLocaleDateString()
                          : "未知"}
                      </p>
                    </div>
                    <button
                      className="user-profile-close-btn"
                      onClick={onClose}
                    >
                      ✕
                    </button>
                  </div>

                  {/* 操作按钮 */}
                  <div className="user-profile-actions">
                    {user.id !== userId && (
                      <>
                        {/* 关注按钮 - 对所有非自己的用户都显示 */}
                        <FollowButton
                          targetUserId={userId}
                          currentUser={user}
                          size="medium"
                          variant="outline"
                          className="user-profile-follow-btn"
                        />

                        {/* 私聊按钮 - 对所有非自己的用户都显示 */}
                        <button
                          className="user-profile-action-btn chat-btn"
                          onClick={handleChat}
                        >
                          <span className="action-icon">💬</span>
                          私聊
                        </button>

                        {/* 拉黑按钮 - 只对非管理员用户显示，且被查看的用户不是管理员 */}
                        {user.role !== "ADMIN" &&
                          userProfile.role !== "ADMIN" &&
                          (isUserBlocked(userId) ? (
                            <button
                              className="user-profile-action-btn unblock-btn"
                              onClick={handleBlock}
                            >
                              <span className="action-icon">✅</span>
                              已拉黑
                            </button>
                          ) : (
                            <button
                              className="user-profile-action-btn block-btn"
                              onClick={handleBlock}
                            >
                              <span className="action-icon">⛔</span>
                              拉黑
                            </button>
                          ))}
                      </>
                    )}
                    {user.id === userId && (
                      <button
                        className="user-profile-action-btn edit-btn"
                        onClick={() => (window.location.href = "/profile")}
                      >
                        <span className="action-icon">✏️</span>
                        编辑资料
                      </button>
                    )}
                  </div>

                  {/* 统计信息 */}
                  <div className="user-profile-stats">
                    <div className="user-profile-stat-item">
                      <div className="stat-number">{userPosts.length}</div>
                      <div className="stat-label">动态</div>
                    </div>
                    <div className="user-profile-stat-item">
                      <div className="stat-number">{userMoods.length}</div>
                      <div className="stat-label">心情记录</div>
                    </div>
                    <div className="user-profile-stat-item">
                      <div className="stat-number">
                        {userProfile.postsCount || 0}
                      </div>
                      <div className="stat-label">总发帖</div>
                    </div>
                  </div>

                  {/* 最近动态 */}
                  <div className="user-profile-section">
                    <h3 className="user-profile-section-title">📝 最近动态</h3>
                    <div className="user-profile-posts">
                      {userPosts.length === 0 ? (
                        <p className="user-profile-empty">暂无动态</p>
                      ) : (
                        userPosts.map((post) => (
                          <div key={post.id} className="user-profile-post-item">
                            <div className="post-item-header">
                              <span className="post-mood">
                                {getMoodEmojiByRating(post.rating)}
                              </span>
                              <span className="post-time">
                                {new Date(post.createdAt).toLocaleDateString()}
                              </span>
                            </div>
                            <p className="post-content">{post.content}</p>
                            {post.tags && post.tags.length > 0 && (
                              <div className="post-tags">
                                {post.tags.map((tag, index) => (
                                  <span
                                    key={index}
                                    className="post-tag"
                                    style={{
                                      borderColor: tag.color,
                                      color: tag.color,
                                    }}
                                  >
                                    #{tag.tag}
                                  </span>
                                ))}
                              </div>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  {/* 最近心情 */}
                  <div className="user-profile-section">
                    <h3 className="user-profile-section-title">📊 最近心情</h3>
                    <div className="user-profile-moods">
                      {userMoods.length === 0 ? (
                        <p className="user-profile-empty">暂无心情记录</p>
                      ) : (
                        userMoods.map((mood) => (
                          <div
                            key={mood.id || mood.checkinId}
                            className="user-profile-mood-item"
                          >
                            <div className="mood-item-header">
                              <span className="mood-emoji">
                                {getMoodEmojiByRating(mood.rating)}
                              </span>
                              <span className="mood-text">
                                {getMoodTextByRating(mood.rating)}
                              </span>
                              <span className="mood-rating">
                                ⭐ {mood.rating || "N/A"}
                              </span>
                            </div>
                            {mood.note && (
                              <p className="mood-note">{mood.note}</p>
                            )}
                            <span className="mood-date">
                              {new Date(
                                mood.checkinDate || mood.createdAt
                              ).toLocaleDateString()}
                            </span>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </>
              )}
            </>
          ) : (
            <div className="user-profile-error">
              <p>用户信息加载失败</p>
            </div>
          )}
        </div>
      </div>

      {/* 拉黑用户模态框 */}
      <BlockUserModal
        isOpen={showBlockModal}
        onClose={() => setShowBlockModal(false)}
        userToBlock={blockUserState}
        onBlockSuccess={() => {
          setShowBlockModal(false);
          // 拉黑成功后，BlockContext会自动更新状态
          console.log("拉黑成功，状态已自动更新");
        }}
      />

      {/* 头像预览模态框 */}
      <AvatarPreviewModal
        isOpen={showAvatarPreview}
        onClose={() => setShowAvatarPreview(false)}
        avatarUrl={getUserAvatarUrl(
          userProfile,
          "https://i.pravatar.cc/150?u=anon"
        )}
        userName={`${userProfile?.nickname || userProfile?.username}的头像`}
      />
    </>
  );
};

export default UserProfileModal;
