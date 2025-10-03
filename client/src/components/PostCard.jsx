import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { postService } from "../services/postService";
import { postAPI } from "../services/api";
import ReportModal from "./ReportModal";
import PostImageGrid from "./PostImageGrid";
import { useAuth } from "../context/AuthContext";
import { getUserAvatarUrl } from "../utils/avatarUtils";

const PostCard = ({ post }) => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [isLiked, setIsLiked] = useState(post.liked || false);
  const [likeCount, setLikeCount] = useState(post.likes);
  const [showReportModal, setShowReportModal] = useState(false);
  const tags = post.tags || [];

  const handleLike = async (e) => {
    e.stopPropagation();
    try {
      await postService.toggleLike(post.id);
      setIsLiked((prev) => !prev);
      setLikeCount((prev) => (isLiked ? prev - 1 : prev + 1));
    } catch (error) {
      window.showToast && window.showToast("操作失败，请稍后重试");
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
      throw error; // 重新抛出错误，让ReportModal处理
    }
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
      <div className="post-card" onClick={() => navigate(`/post/${post.id}`)}>
        {/* 卡片头部（匿名+时间+心情） */}
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
              className="post-avatar"
              style={{
                width: 40,
                height: 40,
                borderRadius: "50%",
                objectFit: "cover",
              }}
            />
            <div className="post-anonymous-info">
              <span className="post-anonymous-label">{post.username}</span>
              <span className="post-time">{post.time}</span>
            </div>
          </div>
          <div className="post-mood-indicator">
            <span className="post-mood-emoji">{post.mood}</span>
          </div>
        </div>

        {/* 卡片内容 */}
        <div className="post-card-content">
          {post.title && <div className="post-title">{post.title}</div>}
          <p className="post-text">{post.content}</p>
          {/* 图片展示 */}
          <PostImageGrid imageUrls={post.imageUrls} />
        </div>

        {/* 卡片底部：左侧tag，右侧操作 */}
        <div className="post-card-footer">
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
          <div className="post-footer-actions">
            <button
              className={`post-action-btn like-btn ${isLiked ? "liked" : ""}`}
              onClick={handleLike}
            >
              <span className="action-icon">{isLiked ? "❤️" : "🤍"}</span>
              <span className="action-text">{likeCount}</span>
            </button>
            <button
              className="post-action-btn report-btn"
              onClick={handleReport}
            >
              <span className="action-icon">🚩</span>
              <span className="action-text">举报</span>
            </button>
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
    </>
  );
};

export default PostCard;
