import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const { user, showSuccess } = useAuth();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyContent, setReplyContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 多个预设暖心评论
  const warmComments = [
    '加油！你的分享很温暖，希望你每天都开心！',
    '你很棒，继续保持积极的心态！',
    '感谢你的分享，愿你每天都有好心情！',
    '无论遇到什么，都要相信自己会越来越好！',
    '你的故事很打动我，祝你幸福快乐！'
  ];
  const [showWarmList, setShowWarmList] = useState(false);

  // 鼠标悬停控制暖心评论选项栏
  let warmListTimeout = null;
  const handleWarmMouseEnter = () => {
    if (warmListTimeout) clearTimeout(warmListTimeout);
    setShowWarmList(true);
  };
  const handleWarmMouseLeave = () => {
    warmListTimeout = setTimeout(() => setShowWarmList(false), 120);
  };

  // 模拟帖子数据
  useEffect(() => {
    // 模拟从API获取帖子详情
    const mockPost = {
      id: parseInt(postId),
      author: '小明',
      avatar: 'https://i.pravatar.cc/150?u=1',
      content: '今天心情特别好，和朋友们一起去了公园，感觉整个人都轻松了！希望每个人都能找到属于自己的快乐时光。想和大家分享一些我最近的感悟：生活中的小确幸往往比我们想象的更珍贵。比如一杯热茶的温暖、朋友的一个微笑、或者是看到花儿绽放的那一瞬间。这些看似平凡的时刻，却能给我们带来真正的快乐和满足感。',
      likes: 15,
      isLiked: false,
      comments: 12,
      time: '2小时前',
      mood: '😊',
      tags: ['心情分享', '正能量']
    };

    const mockComments = [
      {
        id: 1,
        author: '小红',
        avatar: 'https://i.pravatar.cc/150?u=2',
        content: '说得太好了！我也觉得生活中的小确幸很重要，感谢分享～',
        likes: 8,
        isLiked: false,
        time: '1小时前',
        replies: [
          {
            id: 11,
            author: '小明',
            avatar: 'https://i.pravatar.cc/150?u=1',
            content: '谢谢你的认同！我们都要保持积极的心态呢',
            likes: 3,
            isLiked: false,
            time: '50分钟前',
            replyTo: '小红'
          }
        ]
      },
      {
        id: 2,
        author: '小李',
        avatar: 'https://i.pravatar.cc/150?u=3',
        content: '最近也在学着关注身边的美好，你的分享让我很有共鸣！',
        likes: 5,
        isLiked: true,
        time: '45分钟前',
        replies: []
      },
      {
        id: 3,
        author: '小张',
        avatar: 'https://i.pravatar.cc/150?u=4',
        content: '这个分享真的很治愈，谢谢你带来的正能量！有时候我们确实需要停下来感受生活的美好。',
        likes: 6,
        isLiked: false,
        time: '30分钟前',
        replies: []
      }
    ];

    setPost(mockPost);
    setComments(mockComments);
  }, [postId]);

  // 点赞帖子
  const handleLikePost = () => {
    setPost(prev => ({
      ...prev,
      isLiked: !prev.isLiked,
      likes: prev.isLiked ? prev.likes - 1 : prev.likes + 1
    }));
    showSuccess(post.isLiked ? '取消点赞' : '点赞成功！');
  };

  // 点赞评论
  const handleLikeComment = (commentId, isReply = false, parentId = null) => {
    setComments(prev => {
      return prev.map(comment => {
        if (!isReply && comment.id === commentId) {
          return {
            ...comment,
            isLiked: !comment.isLiked,
            likes: comment.isLiked ? comment.likes - 1 : comment.likes + 1
          };
        }
        
        if (isReply && comment.id === parentId) {
          return {
            ...comment,
            replies: comment.replies.map(reply => {
              if (reply.id === commentId) {
                return {
                  ...reply,
                  isLiked: !reply.isLiked,
                  likes: reply.isLiked ? reply.likes - 1 : reply.likes + 1
                };
              }
              return reply;
            })
          };
        }
        
        return comment;
      });
    });
  };

  // 提交评论
  const handleSubmitComment = async () => {
    if (!newComment.trim()) return;

    setIsSubmitting(true);

    // 模拟API调用
    setTimeout(() => {
      const comment = {
        id: Date.now(),
        author: user.name,
        avatar: user.avatar,
        content: newComment,
        likes: 0,
        isLiked: false,
        time: '刚刚',
        replies: []
      };

      setComments(prev => [...prev, comment]);
      setNewComment('');
      setIsSubmitting(false);
      showSuccess('评论发布成功！');
    }, 1000);
  };

  // 提交回复
  const handleSubmitReply = async (commentId) => {
    if (!replyContent.trim()) return;

    setIsSubmitting(true);

    // 模拟API调用
    setTimeout(() => {
      const reply = {
        id: Date.now(),
        author: user.name,
        avatar: user.avatar,
        content: replyContent,
        likes: 0,
        isLiked: false,
        time: '刚刚',
        replyTo: comments.find(c => c.id === commentId)?.author
      };

      setComments(prev => {
        return prev.map(comment => {
          if (comment.id === commentId) {
            return {
              ...comment,
              replies: [...comment.replies, reply]
            };
          }
          return comment;
        });
      });

      setReplyContent('');
      setReplyingTo(null);
      setIsSubmitting(false);
      showSuccess('回复发布成功！');
    }, 1000);
  };

  // 发送选中的暖心评论
  const handleSendWarmComment = (text) => {
    if (isSubmitting) return;
    setIsSubmitting(true);
    setShowWarmList(false);
    setTimeout(() => {
      const comment = {
        id: Date.now(),
        author: user.name,
        avatar: user.avatar,
        content: text,
        likes: 0,
        isLiked: false,
        time: '刚刚',
        replies: []
      };
      setComments(prev => [...prev, comment]);
      setIsSubmitting(false);
      showSuccess('暖心评论已发送！');
    }, 1000);
  };

  if (!post) {
    return (
      <>
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </>
    );
  }

  return (
    <>
      <div className="post-detail-container">
        {/* 返回按钮 */}
        <div className="post-detail-header">
          <button 
            onClick={() => navigate('/community')}
            className="back-button"
          >
            ← 返回社区
          </button>
        </div>

        {/* 帖子详情 */}
        <div className="post-detail-card" style={{position: 'relative'}}>
          <div className="post-detail-main">
            <div className="post-detail-author">
              <img
                src={post.avatar}
                alt={post.author}
                className="post-detail-avatar"
              />
              <div className="post-detail-author-info">
                <div className="post-detail-author-header">
                  <span className="post-detail-author-name">{post.author}</span>
                  <span className="post-detail-mood">{post.mood}</span>
                  <span className="post-detail-time">{post.time}</span>
                </div>
                {post.tags && (
                  <div className="post-detail-tags">
                    {post.tags.map((tag, index) => (
                      <span key={index} className="post-detail-tag">
                        #{tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
            
            <div className="post-detail-content">
              {post.content}
            </div>
            
            <div className="post-detail-actions">
              <button
                onClick={handleLikePost}
                className={`post-action-button ${post.isLiked ? 'liked' : ''}`}
              >
                <span className="post-action-icon">
                  {post.isLiked ? '❤️' : '🤍'}
                </span>
                <span className="post-action-text">{post.likes}</span>
              </button>
              <div className="post-action-button">
                <span className="post-action-icon">💬</span>
                <span className="post-action-text">{comments.length}</span>
              </div>
            </div>
          </div>
          {/* 卡片右下角暖心评论按钮 */}
          <div
            style={{position: 'absolute', right: 24, bottom: 24, zIndex: 10}}
            onMouseEnter={handleWarmMouseEnter}
            onMouseLeave={handleWarmMouseLeave}
          >
            <button
              className="warm-comment-button comment-submit-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? '发送中...' : '暖心评论'}
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
        </div>

        {/* 评论输入区 */}
        <div className="comment-input-section">
          <div className="comment-input-header">
            <img
              src={user.avatar}
              alt={user.name}
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
                '发布评论'
              )}
            </button>
          </div>
        </div>

        {/* 评论列表 */}
        <div className="comments-section">
          <h3 className="comments-title">
            全部评论 ({comments.length})
          </h3>
          
          {comments.length === 0 ? (
            <div className="comments-empty">
              <div className="comments-empty-icon">💭</div>
              <p className="comments-empty-text">还没有评论，来说点什么吧～</p>
            </div>
          ) : (
            <div className="comments-list">
              {comments.map((comment) => (
                <div key={comment.id} className="comment-item">
                  <div className="comment-main">
                    <img
                      src={comment.avatar}
                      alt={comment.author}
                      className="comment-avatar"
                    />
                    <div className="comment-content">
                      <div className="comment-header">
                        <span className="comment-author">{comment.author}</span>
                        <span className="comment-time">{comment.time}</span>
                      </div>
                      <p className="comment-text">{comment.content}</p>
                      <div className="comment-actions">
                        <button
                          onClick={() => handleLikeComment(comment.id)}
                          className={`comment-action-button ${comment.isLiked ? 'liked' : ''}`}
                        >
                          <span className="comment-action-icon">
                            {comment.isLiked ? '❤️' : '🤍'}
                          </span>
                          <span className="comment-action-text">{comment.likes}</span>
                        </button>
                        <button
                          onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}
                          className="comment-action-button"
                        >
                          <span className="comment-action-icon">💬</span>
                          <span className="comment-action-text">回复</span>
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* 回复输入框 */}
                  {replyingTo === comment.id && (
                    <div className="reply-input-section">
                      <img
                        src={user.avatar}
                        alt={user.name}
                        className="reply-input-avatar"
                      />
                      <div className="reply-input-content">
                        <textarea
                          value={replyContent}
                          onChange={(e) => setReplyContent(e.target.value)}
                          placeholder={`回复 ${comment.author}...`}
                          className="reply-input-textarea"
                          rows="2"
                        />
                        <div className="reply-input-actions">
                          <button
                            onClick={() => {
                              setReplyingTo(null);
                              setReplyContent('');
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
                            {isSubmitting ? '发布中...' : '回复'}
                          </button>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* 回复列表 */}
                  {comment.replies.length > 0 && (
                    <div className="replies-list">
                      {comment.replies.map((reply) => (
                        <div key={reply.id} className="reply-item">
                          <img
                            src={reply.avatar}
                            alt={reply.author}
                            className="reply-avatar"
                          />
                          <div className="reply-content">
                            <div className="reply-header">
                              <span className="reply-author">{reply.author}</span>
                              {reply.replyTo && (
                                <span className="reply-to">
                                  回复 <span className="reply-to-name">{reply.replyTo}</span>
                                </span>
                              )}
                              <span className="reply-time">{reply.time}</span>
                            </div>
                            <p className="reply-text">{reply.content}</p>
                            <div className="reply-actions">
                              <button
                                onClick={() => handleLikeComment(reply.id, true, comment.id)}
                                className={`reply-action-button ${reply.isLiked ? 'liked' : ''}`}
                              >
                                <span className="reply-action-icon">
                                  {reply.isLiked ? '❤️' : '🤍'}
                                </span>
                                <span className="reply-action-text">{reply.likes}</span>
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
} 