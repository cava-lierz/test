import React, { useState, useEffect } from "react";
import { userAPI } from "../services/api";

export default function FollowButton({
  targetUserId,
  currentUser,
  size = "medium",
  variant = "default",
  onFollowChange,
  className = "",
}) {
  const [isFollowing, setIsFollowing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [initialized, setInitialized] = useState(false);

  // 检查关注状态
  useEffect(() => {
    if (!targetUserId || !currentUser || currentUser.id === targetUserId) {
      setInitialized(true);
      return;
    }

    const checkFollowStatus = async () => {
      try {
        const response = await userAPI.checkFollowStatus(targetUserId);
        setIsFollowing(response.isFollowing || false);
      } catch (error) {
        console.error("检查关注状态失败:", error);
        setIsFollowing(false);
      } finally {
        setInitialized(true);
      }
    };

    checkFollowStatus();
  }, [targetUserId, currentUser]);

  const handleFollow = async () => {
    if (!currentUser || currentUser.id === targetUserId) return;

    setLoading(true);
    try {
      if (isFollowing) {
        await userAPI.unfollowUser(targetUserId);
        setIsFollowing(false);
      } else {
        await userAPI.followUser(targetUserId);
        setIsFollowing(true);
      }

      // 回调通知父组件关注状态变化
      if (onFollowChange) {
        onFollowChange(!isFollowing);
      }
    } catch (error) {
      window.showToast && window.showToast("操作失败：" + error.message);
    } finally {
      setLoading(false);
    }
  };

  // 如果当前用户查看自己的页面，不显示关注按钮
  if (!currentUser || currentUser.id === targetUserId) {
    return null;
  }

  // 如果还在初始化，显示加载状态
  if (!initialized) {
    return (
      <button
        className={`follow-button follow-button-${size} follow-button-${variant} ${className}`}
        disabled
      >
        <span className="follow-button-loading">加载中...</span>
      </button>
    );
  }

  const getButtonText = () => {
    if (loading) {
      return isFollowing ? "取消关注中..." : "关注中...";
    }
    return isFollowing ? "取消关注" : "关注";
  };

  const getButtonIcon = () => {
    if (loading) {
      return "⏳";
    }
    return isFollowing ? "✓" : "+";
  };

  return (
    <button
      className={`follow-button follow-button-${size} follow-button-${variant} ${
        isFollowing ? "following" : ""
      } ${className}`}
      onClick={handleFollow}
      disabled={loading}
      title={isFollowing ? "点击取消关注" : "点击关注用户"}
    >
      <span className="follow-button-icon">{getButtonIcon()}</span>
      <span className="follow-button-text">{getButtonText()}</span>
    </button>
  );
}
