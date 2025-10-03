import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const PostCard = ({ post }) => {
  const navigate = useNavigate();
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(post.likes);
  // 假设post.tags为标签数组
  const tags = post.tags || ["心理健康", "成长", "匿名"];

  const handleLike = (e) => {
    e.stopPropagation();
    setIsLiked(!isLiked);
    setLikeCount(isLiked ? likeCount - 1 : likeCount + 1);
  };

  const handleComment = (e) => {
    e.stopPropagation();
    navigate(`/post/${post.id}`);
  };

  const handleReport = (e) => {
    e.stopPropagation();
    // 举报功能可在此实现
    alert("已举报该动态");
  };

  const handleTagClick = (tag, e) => {
    e.stopPropagation();
    // 可实现tag筛选或跳转
    alert(`点击了标签：${tag}`);
  };

  return (
    <div className="post-card" onClick={() => navigate(`/post/${post.id}`)}>
      {/* 卡片头部（匿名+时间+心情） */}
      <div
        className="post-card-header"
        style={{ justifyContent: "space-between", alignItems: "center" }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <img
            src={post.avatar || "https://i.pravatar.cc/150?u=anon"}
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
        <p className="post-text">{post.content}</p>
      </div>

      {/* 卡片底部：左侧tag，右侧操作 */}
      <div className="post-card-footer">
        <div className="post-tags">
          {tags.map((tag, idx) => (
            <button
              key={idx}
              className="post-tag-btn"
              onClick={(e) => handleTagClick(tag, e)}
              type="button"
            >
              #{tag}
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
          <button className="post-action-btn report-btn" onClick={handleReport}>
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
  );
};

export default PostCard;
