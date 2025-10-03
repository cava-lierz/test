import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import NotificationCard from './NotificationCard';
import PagedScrollList from './PagedScrollList';
import { useNotification } from '../context/NotificationContext';
import { getUserAvatarUrl } from '../utils/avatarUtils';

const PAGE_SIZE = 10;

export default function Header() {
  const { user, isAdmin, isExpert } = useAuth();
  const { notifications, unreadCount, markAsRead, deleteNotification } = useNotification();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [hasNotifications, setHasNotifications] = useState(true); // 仅用于红点消失逻辑
  const [showNotificationPanel, setShowNotificationPanel] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleMobileMenuToggle = (event) => {
      setIsMobileMenuOpen(event.detail.isOpen);
    };
    window.addEventListener("mobileMenuToggle", handleMobileMenuToggle);
    return () => {
      window.removeEventListener("mobileMenuToggle", handleMobileMenuToggle);
    };
  }, []);

  if (!user) return null;

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
    const event = new CustomEvent("mobileMenuToggle", {
      detail: { isOpen: !isMobileMenuOpen },
    });
    window.dispatchEvent(event);
  };

  const toggleNotificationPanel = () => {
    setShowNotificationPanel(!showNotificationPanel);
    if (hasNotifications) {
      setHasNotifications(false);
    }
  };

  const handleNotificationClick = (notification) => {
    navigate(`/notifications?nid=${notification.id}`);
    setShowNotificationPanel(false);
  };

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
    <header className="header">
      <div className="header-content">
        <div className="header-left">
          <button 
            className="mobile-menu-button"
            onClick={toggleMobileMenu}
            aria-label="切换菜单"
          >
            <span className={`hamburger ${isMobileMenuOpen ? 'active' : ''}`}>
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
          
          <Link to="/" className="header-logo">
            <div className="header-logo-icon">
              M
            </div>
            <span className="header-logo-text">
              Mentara
            </span>
          </Link>
        </div>
        <div className="header-spacer" />

        <div className="header-user">
          <div className="header-notification">
            <button
              className="header-notification-button"
              onClick={toggleNotificationPanel}
              aria-label="通知"
            >
              <span className="header-notification-icon">🔔</span>
              {hasNotifications && unreadCount > 0 && (
                <span className="header-notification-badge">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </span>
              )}
            </button>

            {showNotificationPanel && (
              <div className="header-notification-panel">
                <div className="header-notification-header">
                  <h3>通知</h3>
                  <h4>详情</h4>
                  <button
                    className="header-notification-close"
                    onClick={() => setShowNotificationPanel(false)}
                  >
                    ×
                  </button>
                </div>
                  {notifications.length > 0 ? (
                    <PagedScrollList
                      fetchPage={fetchNotificationPage}
                      pageSize={PAGE_SIZE}
                      renderItem={item => (
                        <div style={{ cursor: 'pointer' }}>
                          <NotificationCard 
                            notification={item} 
                            showDot={!item.read} 
                            onMarkAsRead={markAsRead} 
                            onClick={handleNotificationClick}
                            onDelete={deleteNotification}
                          />
                        </div>
                      )}
                      sortOrder="desc"
                      style={{ maxHeight: 350 }}
                      items={notifications}
                    />
                  ) : (
                    <div className="header-notification-empty">暂无通知</div>
                  )}
              </div>
            )}
          </div>

          <Link to="/profile" className="header-user-link">
            <div className="header-user-button">
              <img
                src={getUserAvatarUrl(user)}
                alt={user.nickname || user.username}
                className="header-user-avatar"
              />
              <span className="header-user-name">
                {user.nickname || user.username}
                {isAdmin() && <span className="admin-crown" style={{marginLeft: '4px'}}>👑</span>}
                {isExpert() && <span className="expert-badge" style={{marginLeft: '4px'}}>🧑‍⚕️</span>}
              </span>
            </div>
          </Link>
        </div>
      </div>
    </header>
  );
}
