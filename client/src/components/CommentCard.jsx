import React, { useEffect, useState } from "react";
import PostMenu from "./PostMenu";
import { postService } from "../services/postService";
import { commentAPI, adminAPI } from "../services/api";
import { getCommentAvatarUrl, getUserAvatarUrl } from "../utils/avatarUtils";
import CommentReportModal from "./CommentReportModal";
import { useAuth } from "../context/AuthContext";
import BlockUserModal from "./BlockUserModal";

export default function CommentCard({
  comment,
  user,
  replyingTo,
  setReplyingTo,
  replyContent,
  setReplyContent,
  handleSubmitReply,
  isSubmitting,
  handleLikeComment,
  level = 1,
  parentAuthorNickname,
  onReport,
  onBlock,
  onBlockSuccess,
  onAvatarClick,
  hideExpandReplies = false,
  onExpandReplies,
  parentMap,
  onGotoReply,
  highlighted,
  fetchParentComment,
  onCommentDeleted,
}) {
  // reply对comment缩进
  const marginLeft = 30;

  // 新增：本地state存储临时加载的父评论作者昵称
  const [tempParentAuthor, setTempParentAuthor] = useState(null);
  const [showReportModal, setShowReportModal] = useState(false);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [blockUserState, setBlockUserState] = useState(null);
  const { isAdmin } = useAuth();

  useEffect(() => {
    let ignore = false;
    async function fetchParent() {
      if (
        comment.parentId &&
        (!parentMap || !parentMap[comment.parentId]) &&
        comment.parentId !== comment.topCommentId
      ) {
        try {
          // 优先用全局缓存
          if (fetchParentComment) {
            const parent = await fetchParentComment(comment.parentId);
            if (!ignore)
              setTempParentAuthor(
                parent.authorNickname || parent.authorName || "未知用户"
              );
          } else {
            // 兼容老用法
            const parent = await postService.getCommentById(comment.parentId);
            if (!ignore)
              setTempParentAuthor(
                parent.authorNickname || parent.authorName || "未知用户"
              );
          }
        } catch {
          if (!ignore) setTempParentAuthor("未知用户");
        }
      }
    }
    fetchParent();
    return () => {
      ignore = true;
    };
  }, [comment.parentId, comment.topCommentId, parentMap, fetchParentComment]);

  // 优先使用props传入的parentAuthorNickname，其次parentMap，再次临时加载
  const displayParentAuthor =
    parentAuthorNickname ||
    (parentMap && parentMap[comment.parentId]?.authorNickname) ||
    (parentMap && parentMap[comment.parentId]?.authorName) ||
    tempParentAuthor;

  // 举报评论
  const handleReportComment = () => {
    setShowReportModal(true);
  };

  // 删除评论（用户删除自己的评论）
  const handleDeleteComment = async () => {
    window.showConfirm &&
      window.showConfirm("确定要删除这条评论吗？", async () => {
        try {
          await commentAPI.deleteComment(comment.id);
          window.showToast && window.showToast("评论删除成功", "success");
          onCommentDeleted && onCommentDeleted(comment.id);
        } catch (error) {
          window.showToast &&
            window.showToast("删除评论失败，请稍后重试", "error");
        }
      });
  };

  // 管理员强制删除评论
  const handleAdminDeleteComment = async () => {
    window.showConfirm &&
      window.showConfirm("确定要删除这条评论吗？此操作不可撤销。", async () => {
        try {
          await adminAPI.forceDeleteComment(comment.id);
          window.showToast && window.showToast("评论删除成功", "success");
          onCommentDeleted && onCommentDeleted(comment.id);
        } catch (error) {
          window.showToast &&
            window.showToast("删除评论失败，请稍后重试", "error");
        }
      });
  };

  // 默认举报/拉黑行为
  const handleReport = onReport || handleReportComment;
  const handleBlock =
    onBlock ||
    (() => {
      setBlockUserState({
        id: comment.authorId,
        avatar: comment.authorAvatar,
        nickname: comment.authorNickname || comment.authorName,
        username: comment.authorName,
        role: comment.authorRole,
      });
      setShowBlockModal(true);
    });

  // 判断是否显示删除按钮：用户删除自己的评论 或 管理员删除任意评论
  const canDelete = comment.canDelete || isAdmin();
  const deleteHandler = isAdmin()
    ? handleAdminDeleteComment
    : handleDeleteComment;

  return (
    <div
      className={`comment-item${highlighted ? " highlighted" : ""}`}
      style={{ marginLeft }}
    >
      {/* 右上角三点菜单 */}
      <PostMenu
        onReport={handleReport}
        onBlock={handleBlock}
        onDelete={canDelete ? deleteHandler : undefined}
        showDelete={canDelete}
        showReport={
          user.role !== "ADMIN" &&
          comment.authorRole !== "ADMIN" &&
          comment.authorRole !== "EXPERT" &&
          comment.authorId !== user.id
        }
        showBlock={
          user.role !== "ADMIN" &&
          comment.authorRole !== "ADMIN" &&
          comment.authorRole !== "EXPERT" &&
          comment.authorId !== user.id
        }
      />
      <div className="comment-main">
        <img
          src={getCommentAvatarUrl(
            { avatar: comment.authorAvatar },
            "https://i.pravatar.cc/150?u=anon"
          )}
          alt={comment.author}
          className="comment-avatar"
          onClick={() => onAvatarClick && onAvatarClick(comment.authorId)}
          style={{ cursor: onAvatarClick ? "pointer" : "default" }}
        />
        <div className="comment-content">
          <div className="comment-header">
            <span className="comment-author">
              {comment.authorNickname || comment.authorName}
            </span>
            {/* parentId不为null且parentId不等于topCommentId时显示"回复 XXX"和转到按钮 */}
            {comment.parentId != null &&
              comment.parentId !== comment.topCommentId && (
                <>
                  <span className="reply-to">
                    {" "}
                    回复{" "}
                    <span className="reply-to-name">{displayParentAuthor}</span>
                  </span>
                  {onGotoReply && (
                    <button
                      className="goto-reply-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        onGotoReply(comment.parentId);
                      }}
                    >
                      <span
                        style={{
                          fontSize: 13,
                          marginRight: 2,
                          verticalAlign: "middle",
                        }}
                      >
                        ↖
                      </span>
                      转到
                    </button>
                  )}
                </>
              )}
            {/* 评论时间 */}
            {comment.createdAt && (
              <span
                className="comment-created-at"
                style={{ marginLeft: 8, color: "#888", fontSize: 12 }}
              >
                {new Date(comment.createdAt).toLocaleString()}
              </span>
            )}
            <span className="comment-time">{comment.time}</span>
          </div>
          <p className="comment-text">{comment.content}</p>
          <div className="comment-actions">
            <div className="comment-actions-left">
              <button
                onClick={() =>
                  handleLikeComment(comment.id, level > 1, comment.parentId)
                }
                className={`comment-action-button ${
                  comment.liked ? "liked" : ""
                }`}
              >
                <span className="comment-action-icon">
                  {comment.liked ? "❤️" : "🤍"}
                </span>
                <span className="comment-action-text">
                  {comment.likesCount}
                </span>
              </button>
              <button
                onClick={() =>
                  setReplyingTo(replyingTo === comment.id ? null : comment.id)
                }
                className="comment-action-button"
              >
                <span className="comment-action-icon">💬</span>
                <span className="comment-action-text">回复</span>
              </button>
            </div>
            {/* 展开回复按钮 */}
            {!hideExpandReplies &&
              comment.parentId == null &&
              comment.repliesCount > 0 && (
                <button
                  className="expand-replies-btn"
                  onClick={() => {
                    if (onExpandReplies) {
                      onExpandReplies(comment);
                    } else {
                      window.showToast &&
                        window.showToast("展开全部回复功能待实现");
                    }
                  }}
                  onMouseOver={(e) => {
                    e.currentTarget.classList.add("hover");
                  }}
                  onMouseOut={(e) => {
                    e.currentTarget.classList.remove("hover");
                  }}
                >
                  <span
                    style={{
                      marginRight: 6,
                      fontSize: 11,
                      verticalAlign: "middle",
                    }}
                  >
                    ↓
                  </span>
                  展开{comment.repliesCount}条回复
                </button>
              )}
          </div>
        </div>
      </div>
      {/* 回复输入框 */}
      {replyingTo === comment.id && (
        <div className="reply-input-section">
          <img
            src={getUserAvatarUrl(user)}
            alt={user.name}
            className="reply-input-avatar"
          />
          <div className="reply-input-content">
            <textarea
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder={`回复 ${
                comment.authorNickname || comment.authorName
              }...`}
              className="reply-input-textarea"
              rows="2"
            />
            <div className="reply-input-actions">
              <button
                onClick={() => {
                  setReplyingTo(null);
                  setReplyContent("");
                }}
                className="reply-cancel-button"
              >
                取消
              </button>
              <button
                onClick={() => handleSubmitReply(comment.id)}
                disabled={!replyContent.trim() || isSubmitting}
                className="reply-submit-button"
              >
                {isSubmitting ? "发布中..." : "回复"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 举报评论模态框 */}
      <CommentReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        commentId={comment.id}
        onReportSuccess={() => {
          setShowReportModal(false);
        }}
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
    </div>
  );
}
