import React, { useState, useEffect, useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import { postService, warmComments, moods } from "../services/postService";
import CommentCard from "../components/CommentCard";
import PostMenu from "../components/PostMenu";
import CommentDetailModal from "../components/CommentDetailModal";
import UserProfileModal from "../components/UserProfileModal";
import PagedScrollList from "../components/PagedScrollList";
import PostImageGrid from "../components/PostImageGrid";
import { getUserAvatarUrl } from "../utils/avatarUtils";

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { user, showSuccess, showConfirm, setShowConfirmModal } = useAuth();
  const { blockedUserIds } = useBlock();
  const [post, setPost] = useState(null);
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyContent, setReplyContent] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showWarmList, setShowWarmList] = useState(false);
  const [showCommentDetail, setShowCommentDetail] = useState(false);
  const [detailComment, setDetailComment] = useState(null);
  const [showUserProfile, setShowUserProfile] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const pagedListRef = useRef();
  const [commentsKey, setCommentsKey] = useState(0); // 用于强制刷新评论列表

  // 鼠标悬停控制暖心评论选项栏
  let warmListTimeout = null;
  const handleWarmMouseEnter = () => {
    if (warmListTimeout) clearTimeout(warmListTimeout);
    setShowWarmList(true);
  };
  const handleWarmMouseLeave = () => {
    warmListTimeout = setTimeout(() => setShowWarmList(false), 120);
  };

  const fetchPage = useCallback(
    (page) => postService.getCommentsOfPostByPage(postId, page, 10),
    [postId]
  );

  const fetchData = useCallback(async () => {
    try {
      const postData = await postService.getPostById(postId);
      setPost(postData);
    } catch (error) {
      showConfirm("加载帖子失败，请稍后重试。", () => navigate(-1));
      console.error(error);
    }
  }, [postId, showConfirm, navigate]);

  // 监听全局评论删除事件（来自管理员后台删除）
  useEffect(() => {
    const handleGlobalCommentDeleted = (event) => {
      const { commentId, source } = event.detail;
      if (source === "admin") {
        // 管理员在后台删除了评论，强制刷新整个评论列表
        setCommentsKey((prev) => prev + 1);

        // 同时刷新帖子数据以更新评论计数
        postService
          .getPostById(postId)
          .then((updatedPost) => {
            setPost(updatedPost);
          })
          .catch((error) => {
            // 忽略刷新错误
          });
      }
    };

    window.addEventListener("commentDeleted", handleGlobalCommentDeleted);

    return () => {
      window.removeEventListener("commentDeleted", handleGlobalCommentDeleted);
    };
  }, [postId]);

  useEffect(() => {
    fetchData();

    return () => {
      setShowConfirmModal(false);
    };
  }, [fetchData, setShowConfirmModal]);

  // 点赞帖子
  const handleLikePost = async () => {
    try {
      await postService.toggleLike(post.id);
      // 只刷新post数据
      const updatedPost = await postService.getPostById(postId);
      setPost(updatedPost);
    } catch (error) {
      if (error.message && error.message.includes("用户或帖子不存在")) {
        showConfirm("用户或帖子不存在", () => {});
      } else {
        showConfirm("操作失败", () => {});
      }
    }
  };

  // 提交评论
  const handleSubmitComment = async () => {
    if (!newComment.trim()) return;
    setIsSubmitting(true);
    try {
      const commentData = { content: newComment };
      await postService.addCommentToPost(postId, commentData);
      setNewComment("");
      // 发布后拉取最后一页并滚动到底部
      await pagedListRef.current?.scrollToBottomAndLoadLastPage();
      // 刷新post数据
      const updatedPost = await postService.getPostById(postId);
      setPost(updatedPost);
    } catch (error) {
      showConfirm("评论失败", () => {});
    } finally {
      setIsSubmitting(false);
    }
  };

  // 发送选中的暖心评论
  const handleSendWarmComment = async (text) => {
    if (isSubmitting) return;
    setIsSubmitting(true);
    setShowWarmList(false);
    try {
      const commentData = { content: text };
      await postService.addCommentToPost(postId, commentData);
      await pagedListRef.current?.scrollToBottomAndLoadLastPage();
      // 新增：刷新post数据
      const updatedPost = await postService.getPostById(postId);
      setPost(updatedPost);
    } catch (error) {
      showConfirm("发送失败", () => {});
    } finally {
      setIsSubmitting(false);
    }
  };

  // 删除帖子
  const handleDeletePost = async () => {
    window.showConfirm &&
      window.showConfirm("确定要删除这个帖子吗？此操作不可撤销。", async () => {
        try {
          await postService.deletePost(post.id);
          showSuccess("帖子删除成功！");
          navigate("/community");
        } catch (error) {
          console.error("删除帖子失败:", error);
          showConfirm("删除失败，请稍后重试", () => {});
        }
      });
  };

  // mood英文转emoji
  const getMoodEmoji = (moodValue) => {
    const moodObj = moods.find((m) => m.value === moodValue);
    return moodObj ? moodObj.emoji : moodValue;
  };

  if (!post) {
    return (
      <div className="loading-container">
        <p>帖子不存在或加载失败。</p>
      </div>
    );
  }

  return (
    <>
      <div className="post-detail-container">
        {/* 返回按钮 */}
        <div className="post-detail-header">
          <button onClick={() => navigate(-1)} className="back-button">
            ← 返回
          </button>
        </div>

        {/* 帖子详情 */}
        <div
          className={`post-detail-card ${
            post.isAnnouncement ? "announcement-post" : ""
          }`}
          style={{ position: "relative" }}
        >
          {/* 公告标识 */}
          {post.isAnnouncement && (
            <div className="announcement-banner">
              <span className="announcement-icon">📢</span>
              <span className="announcement-text">官方公告</span>
            </div>
          )}

          {/* 右上角三点菜单 */}
          <PostMenu
            onReport={() => showSuccess("已举报")}
            onBlock={() => showSuccess("已拉黑")}
            showReport={
              user.role !== "ADMIN" &&
              post.authorRole !== "ADMIN" &&
              post.authorRole !== "EXPERT" &&
              post.authorId !== user.id
            }
            showBlock={user.role !== "ADMIN" && post.authorId !== user.id}
          />
          <div className="post-detail-main">
            {post.title && (
              <div
                className={`post-title ${
                  post.isAnnouncement ? "announcement-title" : ""
                }`}
              >
                {post.title}
              </div>
            )}
            <div className="post-detail-author">
              <img
                src={getUserAvatarUrl(
                  { avatar: post.authorAvatar },
                  "https://i.pravatar.cc/150?u=anon"
                )}
                alt={post.authorNickname}
                className={`post-detail-avatar ${
                  post.authorRole === "ADMIN" ? "admin-avatar" : ""
                } ${post.authorRole === "EXPERT" ? "expert-avatar" : ""}`}
                onClick={() => {
                  setSelectedUserId(post.authorId);
                  setShowUserProfile(true);
                }}
                style={{ cursor: "pointer" }}
              />
              <div className="post-detail-author-info">
                <div className="post-detail-author-header">
                  <span
                    className={`post-detail-author-name ${
                      post.authorRole === "ADMIN" ? "admin-username" : ""
                    } ${post.authorRole === "EXPERT" ? "expert-username" : ""}`}
                  >
                    {post.authorNickname || post.authorName}
                    {post.authorRole === "ADMIN" && (
                      <span className="admin-crown">👑</span>
                    )}
                    {post.authorRole === "EXPERT" && (
                      <span className="expert-badge">🧑‍⚕️</span>
                    )}
                  </span>
                  {!post.isAnnouncement && (
                    <span className="post-detail-mood">
                      {getMoodEmoji(post.mood)}
                    </span>
                  )}
                  {/* 发帖时间 */}
                  {post.createdAt && (
                    <span
                      className="post-detail-created-at"
                      style={{ marginLeft: 8, color: "#888", fontSize: 13 }}
                    >
                      {new Date(post.createdAt).toLocaleString()}
                    </span>
                  )}
                </div>
                {!post.isAnnouncement && post.tags && (
                  <div className="post-detail-tags">
                    {post.tags.map((tag, index) => (
                      <span
                        key={tag.id || index}
                        className="post-tag-btn"
                        style={{
                          borderColor: tag.color,
                          color: tag.color,
                          cursor: "pointer",
                        }}
                        onClick={() => {
                          const urlParams = new URLSearchParams(
                            window.location.search
                          );
                          let tagsArr = [];
                          if (urlParams.has("tags")) {
                            tagsArr = urlParams.get("tags").split(",");
                          }
                          if (tagsArr.includes(String(tag.id))) {
                            tagsArr = tagsArr.filter(
                              (t) => t !== String(tag.id)
                            );
                          } else {
                            tagsArr.push(String(tag.id));
                          }
                          const param =
                            tagsArr.length > 0
                              ? `?tags=${tagsArr.join(",")}`
                              : "";
                          navigate(`/community${param}`);
                        }}
                      >
                        #{tag.tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div
              className={`post-detail-content ${
                post.isAnnouncement ? "announcement-text" : ""
              }`}
            >
              {post.content}
              {/* 图片展示 */}
              <PostImageGrid imageUrls={post.imageUrls} />
            </div>

            <div className="post-detail-actions">
              <button
                onClick={handleLikePost}
                className={`post-action-button ${post.liked ? "liked" : ""}`}
              >
                <span className="post-action-icon">
                  {post.liked ? "❤️" : "🤍"}
                </span>
                <span className="post-action-text">{post.likesCount}</span>
              </button>
              <div className="post-action-button">
                <span className="post-action-icon">💬</span>
                <span className="post-action-text">{post.commentsCount}</span>
              </div>

              {/* 暖心评论按钮 */}
              <div
                style={{
                  marginLeft: "auto",
                  marginRight:
                    post.authorId === user.id || post.authorId === user.userId
                      ? "12px"
                      : "0",
                  position: "relative",
                }}
                onMouseEnter={handleWarmMouseEnter}
                onMouseLeave={handleWarmMouseLeave}
              >
                <button
                  className="warm-comment-button comment-submit-button"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? "发送中..." : "暖心评论"}
                </button>
                {showWarmList && (
                  <div className="warm-comment-list">
                    {warmComments.map((text, idx) => (
                      <button
                        key={idx}
                        className="warm-comment-option"
                        onClick={() => handleSendWarmComment(text)}
                        disabled={isSubmitting}
                      >
                        {text}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* 删除按钮 - 仅自己的帖子显示 */}
              {(post.authorId === user.id || post.authorId === user.userId) && (
                <button
                  onClick={handleDeletePost}
                  className="post-action-button delete-btn own-delete"
                  style={{
                    color: "#ff4757",
                    border: "1px solid #ff4757",
                    borderRadius: "6px",
                    padding: "8px 12px",
                    backgroundColor: "transparent",
                    cursor: "pointer",
                    transition: "all 0.2s ease",
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.backgroundColor = "#ff4757";
                    e.target.style.color = "white";
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.backgroundColor = "transparent";
                    e.target.style.color = "#ff4757";
                  }}
                >
                  <span className="post-action-icon">🗑️</span>
                  <span className="post-action-text">删除</span>
                </button>
              )}
            </div>
          </div>
        </div>

        {/* 评论输入区 */}
        <div className="comment-input-section">
          <div className="comment-input-header">
            <img
              src={getUserAvatarUrl(user, "https://i.pravatar.cc/150?u=anon")}
              alt={user.nickname || user.username}
              className="comment-input-avatar"
            />
            <span className="comment-input-title">写评论</span>
          </div>
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="分享你的想法..."
            className="comment-input-textarea"
            rows="3"
          />
          <div className="comment-input-actions">
            <button
              onClick={handleSubmitComment}
              disabled={!newComment.trim() || isSubmitting}
              className="comment-submit-button"
            >
              {isSubmitting ? (
                <>
                  <div className="loading-spinner loading-spinner-small"></div>
                  发布中...
                </>
              ) : (
                "发布评论"
              )}
            </button>
          </div>
        </div>

        {/* 评论列表 */}
        <div className="comments-section">
          <h3 className="comments-title">全部评论 ({post.commentsCount})</h3>
          <PagedScrollList
            key={commentsKey} // 添加key强制刷新
            ref={pagedListRef}
            fetchPage={fetchPage}
            renderItem={(comment) => {
              // 过滤被拉黑用户的评论
              if (blockedUserIds.includes(comment.authorId)) {
                return null;
              }

              // 点赞评论
              const handleLikeComment = async (
                commentId,
                isReply = false,
                parentId = null
              ) => {
                try {
                  await postService.toggleLikeComment(commentId);
                } catch (error) {
                  showConfirm("点赞失败", () => {});
                  console.error("点赞接口失败", error);
                  return;
                }
                try {
                  const updatedPost = await postService.getPostById(postId);
                  setPost(updatedPost);
                  if (pagedListRef.current && comment._page !== undefined) {
                    pagedListRef.current.reloadPage(comment._page);
                  }
                } catch (error) {
                  showConfirm("刷新失败", () => {});
                  console.error("刷新接口失败", error);
                }
              };
              // 回复评论
              const handleSubmitReply = async (commentId) => {
                if (!replyContent.trim()) return;
                setIsSubmitting(true);
                try {
                  await postService.addReplyToComment(
                    commentId,
                    { content: replyContent },
                    commentId,
                    postId
                  );
                } catch (error) {
                  showConfirm("回复失败", () => {});
                  console.error("回复接口失败", error);
                  setIsSubmitting(false);
                  return;
                }
                try {
                  setReplyContent("");
                  setReplyingTo(null);
                  const updatedPost = await postService.getPostById(postId);
                  setPost(updatedPost);
                  if (pagedListRef.current && comment._page !== undefined) {
                    pagedListRef.current.reloadPage(comment._page);
                  }
                } catch (error) {
                  showConfirm("刷新失败", () => {});
                  console.error("刷新接口失败", error);
                } finally {
                  setIsSubmitting(false);
                }
              };
              return (
                <CommentCard
                  key={comment.id}
                  comment={comment}
                  user={user}
                  replyingTo={replyingTo}
                  setReplyingTo={setReplyingTo}
                  replyContent={replyContent}
                  setReplyContent={setReplyContent}
                  handleSubmitReply={handleSubmitReply}
                  isSubmitting={isSubmitting}
                  handleLikeComment={handleLikeComment}
                  onAvatarClick={(userId) => {
                    setSelectedUserId(userId);
                    setShowUserProfile(true);
                  }}
                  onBlockSuccess={() => {
                    // 拉黑成功后，BlockContext会自动更新状态
                    console.log("拉黑成功，状态已自动更新");
                  }}
                  onExpandReplies={(c) => {
                    if (!showCommentDetail) {
                      setDetailComment(c);
                      setShowCommentDetail(true);
                    }
                  }}
                  onCommentDeleted={async (deletedComment) => {
                    // 刷新帖子数据以更新评论数
                    const updatedPost = await postService.getPostById(postId);
                    setPost(updatedPost);

                    // 刷新删除评论所在的页面，立即隐藏被删除的评论
                    if (
                      pagedListRef.current &&
                      deletedComment._page !== undefined
                    ) {
                      pagedListRef.current.reloadPage(deletedComment._page);
                    }

                    // 如果删除的是当前查看的评论，关闭详情模态框
                    if (
                      detailComment &&
                      detailComment.id === deletedComment.id
                    ) {
                      setShowCommentDetail(false);
                      setDetailComment(null);
                    }
                  }}
                />
              );
            }}
            sortOrder="asc"
          />
        </div>
      </div>

      {/* 评论详情浮窗 */}
      <CommentDetailModal
        open={showCommentDetail}
        onClose={(updatedComment) => {
          setShowCommentDetail(false);
          if (updatedComment) {
            setDetailComment(updatedComment);
            if (updatedComment._page !== undefined && pagedListRef.current) {
              pagedListRef.current.reloadPage(updatedComment._page);
            }
          }
        }}
        mainComment={detailComment}
        user={user}
        onReplySuccess={async () => {
          const updatedPost = await postService.getPostById(postId);
          setPost(updatedPost);
        }}
        onCommentDeleted={async (deletedComment) => {
          // 刷新帖子数据以更新评论数
          const updatedPost = await postService.getPostById(postId);
          setPost(updatedPost);

          // 刷新删除评论所在的页面，立即隐藏被删除的评论
          if (pagedListRef.current && deletedComment._page !== undefined) {
            pagedListRef.current.reloadPage(deletedComment._page);
          }

          // 如果删除的是当前查看的评论，关闭详情模态框
          if (detailComment && detailComment.id === deletedComment.id) {
            setShowCommentDetail(false);
            setDetailComment(null);
          }
        }}
      />

      {/* 用户信息弹窗 */}
      <UserProfileModal
        isOpen={showUserProfile}
        onClose={() => setShowUserProfile(false)}
        userId={selectedUserId}
      />
    </>
  );
}
