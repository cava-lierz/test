import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import { userAPI, blockAPI } from "../services/api";
import UserCard from "../components/UserCard";
import UserProfileModal from "../components/UserProfileModal";

export default function FollowBlockPage() {
  const { user, isLoading } = useAuth();
  const { blockedUserIds, unblockUser } = useBlock();
  const [activeTab, setActiveTab] = useState("follows");
  const [followedUsers, setFollowedUsers] = useState([]);
  const [blockedUsers, setBlockedUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [followStats, setFollowStats] = useState(null);
  const [blockStats, setBlockStats] = useState(null);

  // 搜索和筛选状态
  const [searchQuery, setSearchQuery] = useState("");
  const [sortBy, setSortBy] = useState("name"); // name, date
  const [sortOrder, setSortOrder] = useState("asc"); // asc, desc

  // 用户信息弹窗状态
  const [showUserProfile, setShowUserProfile] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);

  // 判断是否为管理员
  const isAdmin = user?.role === "ADMIN";

  const fetchFollowedUsers = useCallback(async () => {
    setLoading(true);
    try {
      const [followedResponse, statsResponse] = await Promise.all([
        userAPI.getFollowedUsers(),
        userAPI.getFollowStats(),
      ]);
      setFollowedUsers(followedResponse.followedUsers || []);
      setFollowStats(statsResponse);
    } catch (error) {
      setFollowedUsers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchBlockedUsers = useCallback(async () => {
    // 管理员不调用拉黑相关API
    if (isAdmin) return;

    try {
      const [blockedResponse, statsResponse] = await Promise.all([
        blockAPI.getBlockedUsers(),
        blockAPI.getBlockStats(),
      ]);
      setBlockedUsers(blockedResponse.blockedUsers || []);
      setBlockStats(statsResponse);
    } catch (error) {
      setBlockedUsers([]);
    }
  }, [isAdmin]);

  // 获取关注用户列表
  useEffect(() => {
    if (!user || isLoading) return;
    fetchFollowedUsers();
  }, [user, isLoading, fetchFollowedUsers]);

  // 获取拉黑用户列表 (管理员不调用)
  useEffect(() => {
    if (!user || isLoading || isAdmin) return;
    fetchBlockedUsers();
  }, [user, isLoading, blockedUserIds, isAdmin, fetchBlockedUsers]);

  const handleUnfollow = async (userId) => {
    window.showConfirm &&
      window.showConfirm("确定要取消关注这个用户吗？", async () => {
        try {
          await userAPI.unfollowUser(userId);
          fetchFollowedUsers(); // 刷新列表
        } catch (error) {
          window.showToast &&
            window.showToast("取消关注失败：" + error.message, "error");
        }
      });
  };

  const handleUnblock = async (userId) => {
    window.showConfirm &&
      window.showConfirm("确定要取消拉黑这个用户吗？", async () => {
        try {
          await unblockUser(userId);
          fetchBlockedUsers(); // 刷新列表
        } catch (error) {
          window.showToast &&
            window.showToast("取消拉黑失败：" + error.message, "error");
        }
      });
  };

  // 搜索和筛选函数
  const filterAndSortUsers = (users) => {
    let filtered = users;

    // 搜索过滤
    if (searchQuery.trim()) {
      filtered = users.filter(
        (user) =>
          user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (user.bio &&
            user.bio.toLowerCase().includes(searchQuery.toLowerCase()))
      );
    }

    // 排序
    filtered.sort((a, b) => {
      let aValue, bValue;

      if (sortBy === "name") {
        aValue = a.name.toLowerCase();
        bValue = b.name.toLowerCase();
      } else if (sortBy === "email") {
        aValue = a.email.toLowerCase();
        bValue = b.email.toLowerCase();
      } else {
        aValue = a.id;
        bValue = b.id;
      }

      if (sortOrder === "asc") {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    return filtered;
  };

  const getFilteredUsers = () => {
    if (activeTab === "follows") {
      return filterAndSortUsers(followedUsers);
    } else {
      return filterAndSortUsers(blockedUsers);
    }
  };

  const handleSort = (newSortBy) => {
    if (sortBy === newSortBy) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    } else {
      setSortBy(newSortBy);
      setSortOrder("asc");
    }
  };

  // 处理头像点击
  const handleAvatarClick = (userId) => {
    setSelectedUserId(userId);
    setShowUserProfile(true);
  };

  if (loading && followedUsers.length === 0 && blockedUsers.length === 0) {
    return <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>;
  }

  const filteredUsers = getFilteredUsers();

  return (
    <div className="follow-block-page">
      <div className="profile-header">
        <div className="profile-header-bg"></div>
        <div className="profile-header-content">
          <div className="profile-header-main">
            <div className="profile-info">
              <h1 className="profile-name">
                {isAdmin ? "关注管理" : "关注与拉黑管理"}
              </h1>
              <p className="profile-email">
                {isAdmin ? "管理您关注的用户" : "管理您关注的用户和拉黑列表"}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 标签页 */}
      <div className="profile-tabs">
        <div className="profile-tabs-nav">
          <button
            className={`profile-tab ${activeTab === "follows" ? "active" : ""}`}
            onClick={() => setActiveTab("follows")}
          >
            <span className="profile-tab-title">关注列表</span>
            {followStats && (
              <span className="profile-tab-value">
                ({followStats.followingCount || 0})
              </span>
            )}
          </button>
          {/* 管理员不显示拉黑标签页 */}
          {!isAdmin && (
            <button
              className={`profile-tab ${
                activeTab === "blocks" ? "active" : ""
              }`}
              onClick={() => setActiveTab("blocks")}
            >
              <span className="profile-tab-title">拉黑列表</span>
              {blockStats && (
                <span className="profile-tab-value">
                  ({blockStats.blockedCount || 0})
                </span>
              )}
            </button>
          )}
        </div>
      </div>

      {/* 搜索和筛选工具栏 */}
      <div className="follow-block-toolbar">
        <div className="search-box">
          <input
            type="text"
            placeholder={`搜索${
              activeTab === "follows" ? "关注" : "拉黑"
            }用户...`}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
          <span className="search-icon">🔍</span>
        </div>

        <div className="sort-controls">
          <span className="sort-label">排序：</span>
          <button
            className={`sort-btn ${sortBy === "name" ? "active" : ""}`}
            onClick={() => handleSort("name")}
          >
            姓名 {sortBy === "name" && (sortOrder === "asc" ? "↑" : "↓")}
          </button>
          <button
            className={`sort-btn ${sortBy === "email" ? "active" : ""}`}
            onClick={() => handleSort("email")}
          >
            邮箱 {sortBy === "email" && (sortOrder === "asc" ? "↑" : "↓")}
          </button>
        </div>
      </div>

      {/* 关注列表标签页 */}
      {activeTab === "follows" && (
        <div className="profile-tab-content">
          <div className="profile-tab-content-inner">
            {filteredUsers.length === 0 ? (
              <div className="profile-tab-content-empty">
                <div className="profile-tab-content-empty-icon">👥</div>
                <div className="profile-tab-content-empty-title">
                  {searchQuery ? "没有找到匹配的关注用户" : "暂无关注用户"}
                </div>
                <div className="profile-tab-content-empty-description">
                  {searchQuery
                    ? "尝试使用不同的搜索关键词"
                    : "您还没有关注任何用户，去社区发现有趣的朋友吧！"}
                </div>
              </div>
            ) : (
              <div className="followed-users-list">
                {filteredUsers.map((followedUser) => (
                  <UserCard
                    key={followedUser.id}
                    user={followedUser}
                    onAction={handleUnfollow}
                    actionText="取消关注"
                    actionType="secondary"
                    onAvatarClick={handleAvatarClick}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* 拉黑列表标签页 (管理员不显示) */}
      {!isAdmin && activeTab === "blocks" && (
        <div className="profile-tab-content">
          <div className="profile-tab-content-inner">
            {filteredUsers.length === 0 ? (
              <div className="profile-tab-content-empty">
                <div className="profile-tab-content-empty-icon">🚫</div>
                <div className="profile-tab-content-empty-title">
                  {searchQuery ? "没有找到匹配的拉黑用户" : "暂无拉黑用户"}
                </div>
                <div className="profile-tab-content-empty-description">
                  {searchQuery
                    ? "尝试使用不同的搜索关键词"
                    : "您还没有拉黑任何用户"}
                </div>
              </div>
            ) : (
              <div className="blocked-users-list">
                {filteredUsers.map((blockedUser) => (
                  <UserCard
                    key={blockedUser.id}
                    user={blockedUser}
                    onAction={handleUnblock}
                    actionText="取消拉黑"
                    actionType="primary"
                    onAvatarClick={handleAvatarClick}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* 统计信息 */}
      <div className="follow-block-stats">
        <div className="stats-card">
          <div className="stats-item">
            <div className="stats-value">
              {followStats?.followingCount || 0}
            </div>
            <div className="stats-label">关注中</div>
          </div>
          <div className="stats-item">
            <div className="stats-value">
              {followStats?.followersCount || 0}
            </div>
            <div className="stats-label">粉丝数</div>
          </div>
          {/* 管理员不显示拉黑统计 */}
          {!isAdmin && (
            <div className="stats-item">
              <div className="stats-value">{blockStats?.blockedCount || 0}</div>
              <div className="stats-label">已拉黑</div>
            </div>
          )}
        </div>
      </div>

      {/* 用户信息弹窗 */}
      <UserProfileModal
        isOpen={showUserProfile}
        onClose={() => setShowUserProfile(false)}
        userId={selectedUserId}
      />
    </div>
  );
}
