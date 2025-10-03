import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import { blockAPI } from "../services/api";

const BlockManagementPage = () => {
  const { user } = useAuth();
  const { blockedUserIds, unblockUser, refreshBlockedUsers } = useBlock();
  const [blockStats, setBlockStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadBlockData();
  }, []);

  const loadBlockData = async () => {
    try {
      setLoading(true);
      const statsResponse = await blockAPI.getBlockStats();
      setBlockStats(statsResponse);
    } catch (error) {
      console.error("加载拉黑数据失败:", error);
      setError("加载数据失败，请稍后重试");
    } finally {
      setLoading(false);
    }
  };

  const handleUnblockUser = async (blockedUserId) => {
    window.showConfirm &&
      window.showConfirm("确定要取消拉黑该用户吗？", async () => {
        try {
          await unblockUser(blockedUserId);
          window.showToast && window.showToast("取消拉黑成功", "success");
          // 重新加载统计数据
          loadBlockData();
        } catch (error) {
          console.error("取消拉黑失败:", error);
          window.showToast &&
            window.showToast("取消拉黑失败，请稍后重试", "error");
        }
      });
  };

  if (loading) {
    return (
      <div className="block-management-page">
        <div className="loading">加载中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="block-management-page">
        <div className="error">{error}</div>
      </div>
    );
  }

  return (
    <div className="block-management-page">
      <div className="block-management-header">
        <h1>拉黑管理</h1>
        <div className="block-stats">
          <div className="stat-item">
            <span className="stat-label">已拉黑用户</span>
            <span className="stat-value">{blockStats.blockedCount || 0}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">被拉黑次数</span>
            <span className="stat-value">{blockStats.blockerCount || 0}</span>
          </div>
        </div>
      </div>

      <div className="blocked-users-section">
        <h2>已拉黑用户列表</h2>
        {blockedUserIds.length === 0 ? (
          <div className="empty-state">
            <p>您还没有拉黑任何用户</p>
          </div>
        ) : (
          <div className="blocked-users-list">
            {blockedUserIds.map((userId) => (
              <div key={userId} className="blocked-user-item">
                <div className="blocked-user-info">
                  <span className="blocked-user-id">用户ID: {userId}</span>
                  <span className="blocked-user-note">
                    该用户的帖子和评论将不会显示给您
                  </span>
                </div>
                <button
                  className="unblock-btn"
                  onClick={() => handleUnblockUser(userId)}
                >
                  取消拉黑
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="block-management-tips">
        <h3>拉黑功能说明</h3>
        <ul>
          <li>拉黑用户后，您将不会看到该用户发布的帖子和评论</li>
          <li>管理员不能被拉黑</li>
          <li>您不能拉黑自己</li>
          <li>取消拉黑后，您可以重新看到该用户的内容</li>
          <li>拉黑是单向的，被拉黑用户不会收到通知</li>
        </ul>
      </div>
    </div>
  );
};

export default BlockManagementPage;
