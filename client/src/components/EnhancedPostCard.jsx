import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { postService } from "../services/postService";
import { postAPI, adminAPI } from "../services/api";
import ReportModal from "./ReportModal";
import BlockUserModal from "./BlockUserModal";
import PostImageGrid from "./PostImageGrid";
import { getUserAvatarUrl } from "../utils/avatarUtils";

const EnhancedPostCard = ({
  post,
  onPostDeleted,
  onBlockSuccess,
  onAvatarClick,
}) => {
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();
  const [isLiked, setIsLiked] = useState(post.liked || false);
  const [likeCount, setLikeCount] = useState(post.likes);
  const [showReportModal, setShowReportModal] = useState(false);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [blockUserState, setBlockUserState] = useState(null);
  const tags = post.tags || [];

  // 判断是否为当前用户的帖子
  const isOwnPost = user && post.authorId === user.id;

  // 判断是否为管理员帖子
  const isAdminPost = post.authorRole === "ADMIN";

  // 判断是否为专家帖子
  const isExpertPost = post.authorRole === "EXPERT";

  // 判断是否为公告帖子
  const isAnnouncement = post.isAnnouncement;

  const handleLike = async (e) => {
    e.stopPropagation();
    try {
      await postService.toggleLike(post.id);
      setIsLiked((prev) => !prev);
      setLikeCount((prev) => (isLiked ? prev - 1 : prev + 1));
    } catch (error) {
      window.showToast && window.showToast("操作失败，请稍后重试", "error");
    }
  };

  const handleComment = (e) => {
    e.stopPropagation();
    navigate(`/post/${post.id}`);
  };

  const handleReport = (e) => {
    e.stopPropagation();
    // 检查用户是否登录
    const userInfo =
      user && user.id
        ? localStorage.getItem(`mentara_user_info_${user.id}`)
        : null;
    if (!userInfo) {
      window.showToast && window.showToast("请先登录后再举报", "warning");
      return;
    }
    setShowReportModal(true);
  };

  const handleBlock = (e) => {
    e.stopPropagation();
    setBlockUserState({
      id: post.authorId,
      avatar: post.avatar,
      nickname: post.username,
      username: post.username,
      role: post.authorRole,
    });
    setShowBlockModal(true);
  };

  const handleReportSubmit = async (postId, reason) => {
    try {
      console.log("举报帖子:", postId, "原因:", reason);
      await postAPI.reportPost(postId, reason);
      window.showToast && window.showToast("举报成功！", "success");
    } catch (error) {
      console.error("举报失败:", error);
      if (error.message.includes("401")) {
        window.showToast && window.showToast("请先登录后再举报", "warning");
      } else if (error.message.includes("403")) {
        window.showToast && window.showToast("权限不足，无法举报", "error");
      } else if (error.message.includes("不能举报自己的帖子")) {
        window.showToast && window.showToast("不能举报自己的帖子", "warning");
      } else if (error.message.includes("已经举报过")) {
        window.showToast && window.showToast("您已经举报过这个帖子", "warning");
      } else {
        window.showToast &&
          window.showToast(`举报失败: ${error.message}`, "error");
      }
      throw error;
    }
  };

  // 用户删除自己的帖子
  const handleDeleteOwnPost = async (e) => {
    e.stopPropagation();

    window.showConfirm &&
      window.showConfirm("确定要删除这个帖子吗？", async () => {
        try {
          await postAPI.deletePost(post.id);
          window.showToast && window.showToast("帖子删除成功！", "success");
          if (onPostDeleted) {
            onPostDeleted(post.id);
          }
        } catch (error) {
          console.error("删除帖子失败:", error);
          window.showToast && window.showToast("删除失败，请稍后重试", "error");
        }
      });
  };

  // 管理员直接删除帖子
  const handleAdminDeletePost = async (e) => {
    e.stopPropagation();

    window.showConfirm &&
      window.showConfirm("确定要删除这个帖子吗？此操作不可撤销。", async () => {
        try {
          await adminAPI.deleteReportedPost(post.id);
          window.showToast && window.showToast("帖子删除成功！", "success");
          if (onPostDeleted) {
            onPostDeleted(post.id);
          }
        } catch (error) {
          console.error("删除帖子失败:", error);
          window.showToast && window.showToast("删除失败，请稍后重试", "error");
        }
      });
  };

  const handleTagClick = (tagObj, e) => {
    e.stopPropagation();
    const urlParams = new URLSearchParams(window.location.search);
    let tagsArr = [];
    if (urlParams.has("tags")) {
      tagsArr = urlParams.get("tags").split(",");
    }
    if (tagsArr.includes(String(tagObj.id))) {
      tagsArr = tagsArr.filter((t) => t !== String(tagObj.id));
    } else {
      tagsArr.push(String(tagObj.id));
    }
    const param = tagsArr.length > 0 ? `?tags=${tagsArr.join(",")}` : "";
    navigate(`/community${param}`);
  };

  return (
    <>
      <div
        className={`post-card ${isAnnouncement ? "announcement-post" : ""} ${
          isAdminPost ? "admin-post" : ""
        } ${isExpertPost ? "expert-post" : ""}`}
        onClick={() => navigate(`/post/${post.id}`)}
      >
        {/* 公告标识 */}
        {isAnnouncement && (
          <div className="announcement-banner">
            <span className="announcement-icon">📢</span>
            <span className="announcement-text">官方公告</span>
          </div>
        )}

        {/* 卡片头部 */}
        <div
          className="post-card-header"
          style={{ justifyContent: "space-between", alignItems: "center" }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
            <img
              src={getUserAvatarUrl(
                { avatar: post.avatar },
                "https://i.pravatar.cc/150?u=anon"
              )}
              alt="avatar"
              className={`post-avatar ${isAdminPost ? "admin-avatar" : ""} ${
                isExpertPost ? "expert-avatar" : ""
              }`}
              style={{
                width: 40,
                height: 40,
                borderRadius: "50%",
                objectFit: "cover",
                cursor: onAvatarClick ? "pointer" : "default",
              }}
              onClick={(e) => {
                e.stopPropagation();
                onAvatarClick && onAvatarClick(post.authorId);
              }}
            />
            <div className="post-anonymous-info">
              <span
                className={`post-anonymous-label ${
                  isAdminPost ? "admin-username" : ""
                } ${isExpertPost ? "expert-username" : ""}`}
              >
                {post.username}
                {isAdminPost && <span className="admin-crown">👑</span>}
                {isExpertPost && <span className="expert-badge">🧑‍⚕️</span>}
              </span>
              <span className="post-time">{post.time}</span>
            </div>
          </div>
          {/* 心情指示器 - 仅非公告帖子显示 */}
          {!isAnnouncement && (
            <div className="post-mood-indicator">
              <span className="post-mood-emoji">{post.mood}</span>
            </div>
          )}
        </div>

        {/* 卡片内容 */}
        <div className="post-card-content">
          {post.title && (
            <div
              className={`post-title ${
                isAnnouncement ? "announcement-title" : ""
              }`}
            >
              {post.title}
            </div>
          )}
          <p
            className={`post-text ${isAnnouncement ? "announcement-text" : ""}`}
          >
            {post.content}
          </p>
          {/* 图片展示 */}
          <PostImageGrid imageUrls={post.imageUrls} />
        </div>

        {/* 卡片底部 */}
        <div className="post-card-footer">
          {/* 标签 - 仅非公告帖子显示 */}
          {!isAnnouncement && (
            <div className="post-tags">
              {tags.map((tag, idx) => (
                <button
                  key={tag.id || idx}
                  className="post-tag-btn"
                  style={{ borderColor: tag.color, color: tag.color }}
                  onClick={(e) => handleTagClick(tag, e)}
                  type="button"
                >
                  #{tag.tag}
                </button>
              ))}
            </div>
          )}

          {/* 操作按钮 */}
          <div className="post-footer-actions">
            <button
              className={`post-action-btn like-btn ${isLiked ? "liked" : ""}`}
              onClick={handleLike}
            >
              <span className="action-icon">{isLiked ? "❤️" : "🤍"}</span>
              <span className="action-text">{likeCount}</span>
            </button>

            {/* 条件渲染：举报 vs 删除按钮 */}
            {isAdmin()
              ? // 管理员：显示删除按钮（除了自己的帖子）
                !isOwnPost && (
                  <button
                    className="post-action-btn delete-btn admin-delete"
                    onClick={handleAdminDeletePost}
                  >
                    <span className="action-icon">🗑️</span>
                    <span className="action-text">删除</span>
                  </button>
                )
              : // 普通用户：显示举报和拉黑按钮（除了自己的帖子和管理员帖子）
                !isOwnPost &&
                !isAdminPost && (
                  <>
                    <button
                      className="post-action-btn report-btn"
                      onClick={handleReport}
                    >
                      <span className="action-icon">🚩</span>
                      <span className="action-text">举报</span>
                    </button>
                    <button
                      className="post-action-btn block-btn"
                      onClick={handleBlock}
                    >
                      <span className="action-icon">⛔</span>
                      <span className="action-text">拉黑</span>
                    </button>
                  </>
                )}

            {/* 自己的帖子：显示删除按钮 */}
            {isOwnPost && (
              <button
                className="post-action-btn delete-btn own-delete"
                onClick={handleDeleteOwnPost}
              >
                <span className="action-icon">🗑️</span>
                <span className="action-text">删除</span>
              </button>
            )}

            <button
              className="post-action-btn comment-btn"
              onClick={handleComment}
            >
              <span className="action-icon">💬</span>
              <span className="action-text">{post.comments}</span>
            </button>
          </div>
        </div>
      </div>

      {/* 举报模态框 */}
      <ReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        onSubmit={handleReportSubmit}
        postId={post.id}
      />

      {/* 拉黑用户模态框 */}
      <BlockUserModal
        isOpen={showBlockModal}
        onClose={() => setShowBlockModal(false)}
        userToBlock={blockUserState}
        onBlockSuccess={() => {
          setShowBlockModal(false);
          // 通知父组件刷新拉黑列表
          onBlockSuccess && onBlockSuccess();
        }}
      />
    </>
  );
};

export default EnhancedPostCard;
