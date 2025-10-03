import React, { useRef, useEffect } from 'react';
import NotificationCard from '../components/NotificationCard';
import PagedScrollList from '../components/PagedScrollList';
import { useNotification } from '../context/NotificationContext';

const PAGE_SIZE = 10;

export default function NotificationsPage() {
  const { notifications, markAsRead, markAllAsRead, unreadCount, deleteNotification, deleteAllNotifications } = useNotification();
  const pagedListRef = useRef();

  const params = new URLSearchParams(window.location.search);
  const nid = params.get('nid');
  let initialPage = 0;
  if (nid) {
    const index = notifications.findIndex(n => String(n.id) === String(nid));
    if (index !== -1) {
      initialPage = Math.floor(index / PAGE_SIZE);
    }
  }

  useEffect(() => {
    if (nid && pagedListRef.current) {
      pagedListRef.current.scrollToPageAndScrollToItem(initialPage, nid);
    }
  }, [nid, initialPage]);

  // 分页函数基于context数据
  function fetchNotificationPage(page) {
    const start = page * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    const content = notifications.slice(start, end);
    return Promise.resolve({
      content,
      totalPages: Math.ceil(notifications.length / PAGE_SIZE)
    });
  }

  return (
    <div className="profile-tab-content" style={{ maxWidth: 800, margin: '12px auto', background: '#fff', borderRadius: 16, boxShadow: '0 2px 12px rgba(0,0,0,0.08)' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '0 32px 0 32px', paddingTop: 32 }}>
        <h3 className="profile-tab-title" style={{ margin: 0 }}>通知栏</h3>
        {notifications.length > 0 && (
          <div style={{ display: 'flex', gap: '12px' }}>
            {unreadCount > 0 && (
              <button
                className="btn btn-primary"
                onClick={markAllAsRead}
              >
                一键已读
              </button>
            )}
            <button
              className="btn btn-danger"
              onClick={deleteAllNotifications}
              style={{ background: '#dc3545', borderColor: '#dc3545' }}
            >
              清空所有
            </button>
          </div>
        )}
      </div>
      <div className="profile-tab-content-inner">
        {notifications.length > 0 ? (
          <PagedScrollList
            ref={pagedListRef}
            fetchPage={fetchNotificationPage}
            pageSize={PAGE_SIZE}
            renderItem={item => <NotificationCard notification={item} onMarkAsRead={markAsRead} onDelete={deleteNotification} />}
            sortOrder="desc"
            initialPage={initialPage}
            style={{ maxHeight: 475, margin: '24px 32px' }}
            items={notifications}
          />
        ) : (
          <div className="profile-notification-empty">暂无通知</div>
        )}
      </div>
    </div>
  );
} 