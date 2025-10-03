import React, { useState, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // 监听移动端菜单切换事件
  useEffect(() => {
    const handleMobileMenuToggle = (event) => {
      setIsMobileMenuOpen(event.detail.isOpen);
    };

    window.addEventListener("mobileMenuToggle", handleMobileMenuToggle);

    return () => {
      window.removeEventListener("mobileMenuToggle", handleMobileMenuToggle);
    };
  }, []);

  // 刷新宽窄屏时Sidebar的展开状态
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 768) {
        setIsMobileMenuOpen(false);
        // 通知Header关闭移动端菜单
        const event = new CustomEvent('mobileMenuToggle', { detail: { isOpen: false } });
        window.dispatchEvent(event);
      }
      // 窄屏时不自动展开，保持用户操作
    };
    window.addEventListener('resize', handleResize);
    // 页面刷新时也要判断
    handleResize();
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  // 移动端点击菜单项后关闭侧边栏
  const handleMenuItemClick = () => {
    if (window.innerWidth <= 768) {
      setIsMobileMenuOpen(false);
      // 通知Header组件更新状态
      const event = new CustomEvent("mobileMenuToggle", {
        detail: { isOpen: false },
      });
      window.dispatchEvent(event);
    }
  };

  const menuItems = [
    { path: "/community", label: "社区", icon: "🏠" },
    { path: "/ai-chat", label: "AI助手", icon: "🤖" },
    { path: "/profile", label: "个人中心", icon: "👤" },
    { path: "/post", label: "发布动态", icon: "✏️" },
    { path: "/admin", label: "管理控制台", icon: "👑" },
  ];

  return (
    <>
      {/* 移动端遮罩层 */}
      {isMobileMenuOpen && (
        <div
          className="mobile-sidebar-overlay"
          onClick={() => {
            setIsMobileMenuOpen(false);
            const event = new CustomEvent("mobileMenuToggle", {
              detail: { isOpen: false },
            });
            window.dispatchEvent(event);
          }}
        />
      )}

      <aside className={`sidebar ${isMobileMenuOpen ? "mobile-open" : ""}`}>
        <nav className="sidebar-nav">
          <ul>
            {menuItems.map((item) => (
              <li key={item.path}>
                <Link
                  to={item.path}
                  className={`sidebar-nav-item ${
                    location.pathname === item.path ? "active" : ""
                  }`}
                  onClick={handleMenuItemClick}
                >
                  <span className="sidebar-nav-icon">{item.icon}</span>
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </aside>
    </>
  );
}
