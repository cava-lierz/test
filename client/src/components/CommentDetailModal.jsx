import React, { useEffect, useState, useRef, useCallback } from 'react';
import CommentCard from './CommentCard';
import { postService } from '../services/postService';
import { useBlock } from '../context/BlockContext';
import PagedScrollList from './PagedScrollList';

export default function CommentDetailModal({ open, onClose, mainComment, user, onReplySuccess, onCommentDeleted }) {
  const [replies, setReplies] = useState([]);
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyContent, setReplyContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [mainCommentState, setMainComment] = useState(mainComment);
  const [highlightedCommentId] = useState(null);
  const { blockedUserIds } = useBlock();
  const pagedListRef = useRef();

  // 父评论全局缓存
  const parentCommentCache = useRef({}); // id -> comment对象
  // 父评论所在页缓存
  const parentCommentPageCache = useRef({}); // id -> page

  // 提供给CommentCard的父评论获取方法
  const fetchParentComment = async (parentId) => {
    if (parentCommentCache.current[parentId]) {
      return parentCommentCache.current[parentId];
    }
    try {
      const parent = await postService.getCommentById(parentId);
      parentCommentCache.current[parentId] = parent;
      // page信息无法通过单条API获得，后续在renderItem中补充
      return parent;
    } catch {
      return { authorNickname: '未知用户' };
    }
  };

  // 拉取主评论最新数据
  const fetchMainComment = useCallback(async () => {
    if (!mainComment?.id) return;
    try {
      const latest = await postService.getCommentById(mainComment.id);
      setMainComment(prev => ({ ...latest, _page: prev?._page }));
    } catch (err) {
      // 可选：setError('获取主评论失败');
    }
  }, [mainComment?.id]);

  useEffect(() => {
    if (!open || !mainComment?.id) return;
    setMainComment({ ...mainComment, _page: mainComment._page });
    setReplyingTo(null);
    setReplyContent('');
    setReplies([]);
    fetchMainComment();
  }, [open, mainComment, fetchMainComment]);

  // 提交回复
  const handleSubmitReply = async (commentId) => {
    if (!replyContent.trim()) return;
    setIsSubmitting(true);
    try {
      // 查找最顶层直属于post的评论id
      let topCommentId = commentId;
      const findTopCommentId = (id) => {
        if (mainCommentState && mainCommentState.id === id) return mainCommentState.id;
        const findInReplies = (replies) => {
          for (const reply of replies) {
            if (reply.id === id) {
              if (!reply.parentId || reply.parentId === mainCommentState.id) return mainCommentState.id;
              return findTopCommentId(reply.parentId);
            }
          }
          return null;
        };
        return findInReplies(replies) || mainCommentState.id;
      };
      topCommentId = findTopCommentId(commentId);
      await postService.addReplyToComment(commentId, { content: replyContent }, topCommentId, mainCommentState.postId);
      setReplyContent('');
      setReplyingTo(null);
      if (pagedListRef.current) {
        await pagedListRef.current.scrollToBottomAndLoadLastPage();
      }
      await fetchMainComment();
      if (onReplySuccess) onReplySuccess();
    } catch (err) {
      console.error('回复失败:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // 点赞评论（仅更新单条）
  const handleLikeComment = async (commentId, replyObj) => {
    try {
      await postService.toggleLikeComment(commentId);
      if (replyObj && replyObj._page !== undefined && pagedListRef.current) {
        pagedListRef.current.reloadPage(replyObj._page);
      }
      await fetchMainComment();
    } catch (err) {
      console.error('点赞失败:', err);
    }
  };

  // 滚动并高亮，若父评论不在当前窗口则自动加载
  const handleGotoReply = async (replyId) => {
    if (!pagedListRef.current) return;
    // 优先查缓存
    const page = parentCommentPageCache.current[replyId];
    if (typeof page === 'number') {
      await pagedListRef.current.scrollToPageAndScrollToItem(page, replyId);
      return;
    }
    // 没有缓存时用原有遍历逻辑
    await pagedListRef.current.scrollToItemById(mainComment.id, replyId);
  };

  if (!open || !mainComment?.id) return null;

  return (
      <div className="comment-detail-modal-overlay">
        <div className="comment-detail-modal-card">
          {/* 关闭按钮 */}
          <button
              className="comment-detail-modal-close"
              onClick={() => onClose(mainCommentState)}
          >×</button>
          {/* 主评论 */}
          <div style={{ padding: '32px 32px 12px 32px', borderBottom: '1px solid #f0f0f0' }}>
            {mainCommentState && (
                <CommentCard
                    comment={mainCommentState}
                    user={user}
                    hideExpandReplies={true}
                    replyingTo={replyingTo}
                    setReplyingTo={setReplyingTo}
                    replyContent={replyContent}
                    setReplyContent={setReplyContent}
                    handleSubmitReply={handleSubmitReply}
                    isSubmitting={isSubmitting}
                    parentMap={{ [mainCommentState.id]: mainCommentState }}
                    handleLikeComment={(id) => handleLikeComment(id, null)}
                    onGotoReply={handleGotoReply}
                    highlighted={highlightedCommentId === mainCommentState.id}
                    fetchParentComment={fetchParentComment}
                    onCommentDeleted={onCommentDeleted}
                />
            )}
          </div>
          {/* replies */}
          <PagedScrollList
              ref={pagedListRef}
              fetchPage={page => postService.getRepliesByTopCommentPage(mainCommentState?.id || mainComment?.id, page, 10)}
              renderItem={(reply, allReplies) => {
                // 过滤被拉黑用户的回复
                if (blockedUserIds.includes(reply.authorId)) {
                  return null;
                }
                
                const parentMap = {};
                if (mainCommentState) parentMap[mainCommentState.id] = mainCommentState;
                allReplies.forEach(r => {
                  parentMap[r.id] = r;
                  // 记录每条评论的page
                  if (r.id) parentCommentPageCache.current[r.id] = r._page;
                });
                const parentAuthorNickname = parentMap[reply.parentId]?.authorNickname || parentMap[reply.parentId]?.authorName;
                return (
                    <CommentCard
                        key={reply.id}
                        comment={reply}
                        user={user}
                        replyingTo={replyingTo}
                        setReplyingTo={setReplyingTo}
                        replyContent={replyContent}
                        setReplyContent={setReplyContent}
                        handleSubmitReply={handleSubmitReply}
                        isSubmitting={isSubmitting}
                        parentMap={parentMap}
                        parentAuthorNickname={parentAuthorNickname}
                        handleLikeComment={(id) => handleLikeComment(id, reply)}
                        onGotoReply={handleGotoReply}
                        highlighted={highlightedCommentId === reply.id}
                        fetchParentComment={fetchParentComment}
                        onCommentDeleted={onCommentDeleted}
                        onAvatarClick={(userId) => {
                          // TODO: 在CommentDetailModal中显示用户信息弹窗
                          console.log('点击用户头像:', userId);
                        }}
                        onBlockSuccess={() => {
                          // 拉黑成功后，BlockContext会自动更新状态
                          console.log('拉黑成功，状态已自动更新');
                        }}
                    />
                );
              }}
              sortOrder="asc"
              style={{ maxHeight: 400, overflowY: 'auto', padding: '18px 24px 24px 32px' }}
          />
        </div>
      </div>
  );
} 