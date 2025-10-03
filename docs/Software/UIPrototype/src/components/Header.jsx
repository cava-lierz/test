import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { notifications } from '../utils/notifications';

export default function Header() {
  const { user } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [hasNotifications, setHasNotifications] = useState(true); // 模拟有通知
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
    // 向父组件传递状态变化
    const event = new CustomEvent("mobileMenuToggle", {
      detail: { isOpen: !isMobileMenuOpen },
    });
    window.dispatchEvent(event);
  };

  const toggleNotificationPanel = () => {
    setShowNotificationPanel(!showNotificationPanel);
    if (hasNotifications) {
      setHasNotifications(false); // 点击后清除红点
    }
  };

  const handleNotificationClick = (id) => {
    navigate(`/profile?tab=notifications&nid=${id}`);
    setShowNotificationPanel(false);
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <header className="header">
      <div className="header-content">
        {/* 移动端菜单按钮 */}
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
          {/* 通知铃铛 */}
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

            {/* 通知面板 */}
            {showNotificationPanel && (
              <div className="header-notification-panel">
                <div className="header-notification-header">
                  <h3>通知</h3>
                  <h4>详情</h4>
                  <button
                    className="header-notification-close"
                    onClick={() => setShowNotificationPanel(false)}
                  >
                    ✕
                  </button>
                </div>
                <div className="header-notification-list">
                  {notifications.length > 0 ? (
                    notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className={`header-notification-item ${!notification.read ? "unread" : ""}`}
                        onClick={() => handleNotificationClick(notification.id)}
                        style={{ cursor: 'pointer' }}
                      >
                        <div className="header-notification-content">
                          <div className="header-notification-title">
                            {notification.title}
                          </div>
                          <div className="header-notification-message">
                            {notification.message}
                          </div>
                          <div className="header-notification-time">
                            {notification.time}
                          </div>
                        </div>
                        {!notification.read && (
                          <div className="header-notification-dot"></div>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="header-notification-empty">暂无通知</div>
                  )}
                </div>
              </div>
            )}
          </div>

          <Link to="/profile" className="header-user-link">
            <div className="header-user-button">
              <img
                src={user.avatar}
                alt={user.name}
                className="header-user-avatar"
              />
              <span className="header-user-name">{user.name}</span>
            </div>
          </Link>
        </div>
      </div>
    </header>
  );
}
