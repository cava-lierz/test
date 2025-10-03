import React, { useState, useEffect, useCallback } from "react";
import { useParams, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import { userAPI } from "../services/api";
import { getUserAvatarUrl } from "../utils/avatarUtils";
import FollowButton from "../components/FollowButton";

export default function UserDetailPage() {
  const { userId } = useParams();
  const { user: currentUser } = useAuth();
  const { blockUser, isUserBlocked } = useBlock();
  const [user, setUser] = useState(null);
  const [userStats, setUserStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [blockLoading, setBlockLoading] = useState(false);
  const [recentPosts] = useState([]);

  const fetchUserInfo = useCallback(async () => {
    setLoading(true);
    try {
      const [userData, statsData] = await Promise.all([
        userAPI.getUserById(userId),
        userAPI.getUserStatsById(userId),
      ]);

      setUser(userData);
      setUserStats(statsData);
    } catch (error) {
      console.error("获取用户信息失败:", error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  // 获取用户信息
  useEffect(() => {
    if (!userId) return;
    fetchUserInfo();
  }, [userId, fetchUserInfo]);

  const handleFollowChange = (isNowFollowing) => {
    // 可以在这里添加关注状态变化的回调逻辑
    console.log("关注状态变化:", isNowFollowing);
  };

  const handleBlock = async () => {
    if (!currentUser) return;

    const reason = prompt("请输入拉黑原因（可选）：");
    setBlockLoading(true);
    try {
      await blockUser(userId, reason || "");
      window.showToast && window.showToast("用户已拉黑", "success");
    } catch (error) {
      window.showToast &&
        window.showToast("拉黑失败：" + error.message, "error");
    } finally {
      setBlockLoading(false);
    }
  };

  if (loading) {
    return <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>;
  }

  if (!user) {
    return <div style={{ textAlign: "center", padding: 32 }}>用户不存在</div>;
  }

  // 如果当前用户被拉黑，显示提示
  if (isUserBlocked(userId)) {
    return (
      <div style={{ textAlign: "center", padding: 32 }}>
        <p>您已拉黑此用户，无法查看其信息</p>
        <Link to="/follow-block" className="btn btn-primary">
          管理拉黑列表
        </Link>
      </div>
    );
  }

  return (
    <div className="user-detail-page">
      <div className="profile-header">
        <div className="profile-header-bg"></div>
        <div className="profile-header-content">
          <div className="profile-header-main">
            <div className="profile-avatar-section">
              <img
                src={getUserAvatarUrl(user.avatar)}
                alt="用户头像"
                className="profile-avatar"
              />
            </div>
            <div className="profile-info">
              <h1 className="profile-name">{user.name}</h1>
              <p className="profile-email">{user.email}</p>
              {user.bio && <p className="profile-bio">{user.bio}</p>}
              <p className="profile-join-date">
                加入时间：{new Date(user.createdAt).toLocaleDateString("zh-CN")}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 用户统计信息 */}
      <div className="profile-section">
        <div className="profile-section-title">
          <span className="profile-section-title-icon">📊</span>
          用户统计
        </div>
        <div className="profile-stats">
          <div className="stat-item">
            <div className="stat-number">{userStats?.postsCount || 0}</div>
            <div className="stat-label">发布动态</div>
          </div>
          <div className="stat-item">
            <div className="stat-number">{userStats?.commentsCount || 0}</div>
            <div className="stat-label">发布评论</div>
          </div>
          <div className="stat-item">
            <div className="stat-number">{userStats?.totalLikes || 0}</div>
            <div className="stat-label">获得点赞</div>
          </div>
        </div>
      </div>

      {/* 用户操作 */}
      {currentUser && currentUser.id !== parseInt(userId) && (
        <div className="profile-section">
          <div className="profile-section-title">
            <span className="profile-section-title-icon">⚙️</span>
            用户操作
          </div>
          <div className="user-actions">
            <FollowButton
              targetUserId={parseInt(userId)}
              currentUser={currentUser}
              size="large"
              variant="default"
              onFollowChange={handleFollowChange}
              className="user-action-follow-btn"
            />
            <button
              className="btn btn-danger"
              onClick={handleBlock}
              disabled={blockLoading}
            >
              {blockLoading ? "处理中..." : "拉黑用户"}
            </button>
          </div>
        </div>
      )}

      {/* 最近动态 */}
      <div className="profile-section">
        <div className="profile-section-title">
          <span className="profile-section-title-icon">📝</span>
          最近动态
        </div>
        {recentPosts.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📝</div>
            <div className="empty-state-title">暂无动态</div>
            <div className="empty-state-text">该用户还没有发布过动态</div>
          </div>
        ) : (
          <div className="posts-grid-small">
            {recentPosts.map((post) => (
              <div key={post.id} className="post-item-small">
                <div className="post-header-small">
                  <div className="post-avatar-small">
                    <img src={getUserAvatarUrl(user.avatar)} alt="头像" />
                  </div>
                  <div className="post-author-small">{user.name}</div>
                  <div className="post-time-small">
                    {new Date(post.createdAt).toLocaleDateString("zh-CN")}
                  </div>
                </div>
                <div className="post-content-small">{post.content}</div>
                <div className="post-meta-small">
                  <span>👍 {post.likesCount || 0}</span>
                  <span>💬 {post.commentsCount || 0}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 返回按钮 */}
      <div className="profile-section">
        <Link to="/community" className="btn btn-secondary">
          ← 返回社区
        </Link>
      </div>
    </div>
  );
}
