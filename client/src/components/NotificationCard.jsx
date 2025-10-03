import React from 'react';

function renderContent(notification) {
  switch (notification.type) {
    case 'post_reply':
      return (
        <>
          <div>你的帖子《{notification.postTitle}》有新回复：</div>
          <div style={{ color: '#555' }}>{notification.replyContent}</div>
          <div>来自：{notification.replyUserNickname}</div>
        </>
      );
    case 'comment_reply':
      return (
        <>
          <div>你的评论有新回复：</div>
          <div style={{ color: '#555' }}>{notification.replyContent}</div>
          <div>原评论：{notification.commentContent}</div>
          <div>来自：{notification.replyUserNickname}</div>
        </>
      );
    case 'post_like':
      return (
        <>
          <div>你的帖子《{notification.postTitle}》收到{notification.likerCount}个赞</div>
          <div>最近点赞：{notification.lastLikerNickname}</div>
        </>
      );
    case 'comment_like':
      return (
        <>
          <div>你的评论"{notification.commentContent}"收到{notification.likerCount}个赞</div>
          <div>最近点赞：{notification.lastLikerNickname}</div>
        </>
      );
    case 'system':
      return (
        <>
          <div style={{ fontWeight: 'bold', color: '#3b82f6', fontSize: 16 }}>{notification.title || '系统通知'}</div>
          <div style={{ color: '#555', margin: '8px 0' }}>{notification.content}</div>
          {notification.actionUrl && (
            <a
              href={notification.actionUrl}
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: '#2563eb', textDecoration: 'underline', fontSize: 14 }}
              onClick={e => e.stopPropagation()}
            >
              查看详情
            </a>
          )}
        </>
      );
    default:
      return (
        <>
          <div>{notification.title || '新通知'}</div>
          <div>{notification.message || ''}</div>
        </>
      );
  }
}

export default function NotificationCard({ notification, showDot, onMarkAsRead, onClick, onDelete }) {
  function getTimeDisplay(createdAt) {
    if (!createdAt) return '';
    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now - created;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) {
      if (diffHour >= 1) {
        return `${diffHour}小时前`;
      } else if (diffMin >= 1) {
        return `${diffMin}分钟前`;
      } else {
        return '刚刚';
      }
    } else {
      const y = created.getFullYear();
      const m = String(created.getMonth() + 1).padStart(2, '0');
      const d = String(created.getDate()).padStart(2, '0');
      const hh = String(created.getHours()).padStart(2, '0');
      const mm = String(created.getMinutes()).padStart(2, '0');
      return `${y}-${m}-${d} ${hh}:${mm}`;
    }
  }

  const handleClick = () => {
    if (!notification.read && onMarkAsRead) {
      onMarkAsRead(notification.id);
    }
    if (onClick) {
      onClick(notification);
    }
  };
  
  const handleDelete = (e) => {
    e.stopPropagation(); // 阻止事件冒泡
    if (onDelete) {
      onDelete(notification.id);
      } 
  };

  return (
    <div
      id={`profile-notification-${notification.id}`}
      className={`profile-notification-item ${!notification.read ? "unread" : ""}`}
      style={{ position: 'relative', background: 'transparent' }}
      onClick={handleClick}
    >
      <div className="profile-notification-title">{renderContent(notification)}</div>
      <div className="profile-notification-time">{getTimeDisplay(notification.createdAt)}</div>
      {showDot && (
        <div className="header-notification-dot" style={{ position: 'absolute', top: 8, right: 8, zIndex: 3 }}></div>
      )}
      {onDelete && (
        <button
          className="notification-delete-btn"
          onClick={handleDelete}
          style={{
            position: 'absolute',
            top: 8,
            right: 28,
            background: 'none',
            border: 'none',
            color: '#999',
            cursor: 'pointer',
            fontSize: '16px',
            padding: '2px 6px',
            borderRadius: '3px',
            zIndex: 2
          }}
          title="删除通知"
        >
          ×
        </button>
      )}
    </div>
  );
} 