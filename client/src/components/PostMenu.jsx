import React, { useState } from 'react';

export default function PostMenu({ onReport, onBlock, onDelete, showReport = true, showBlock = true, showDelete = false }) {
  const [showMenu, setShowMenu] = useState(false);
  const handleMenuToggle = () => setShowMenu((v) => !v);
  const handleMenuClose = () => setShowMenu(false);
  const handleReport = () => {
    setShowMenu(false);
    if (onReport) onReport();
  };
  const handleBlock = () => {
    setShowMenu(false);
    if (onBlock) onBlock();
  };
  const handleDelete = () => {
    setShowMenu(false);
    if (onDelete) onDelete();
  };

  // 如果没有任何菜单项，不显示菜单按钮
  const hasAnyMenuItem = showReport || showBlock || showDelete;
  
  if (!hasAnyMenuItem) {
    return null;
  }

  return (
    <div style={{position: 'absolute', right: 24, top: 24, zIndex: 20}}>
      <button
        className="post-menu-button"
        onClick={handleMenuToggle}
        style={{background: 'none', border: 'none', cursor: 'pointer', fontSize: 24, padding: 4}}
        aria-label="更多操作"
      >
        &#8942;
      </button>
      {showMenu && (
        <div
          className="post-menu-dropdown"
          style={{position: 'absolute', right: 0, top: 32, background: '#fff', boxShadow: '0 2px 8px rgba(0,0,0,0.15)', borderRadius: 6, minWidth: 80, padding: 4, zIndex: 100}}
          onMouseLeave={handleMenuClose}
        >
          {showReport && (
            <button className="post-menu-item" style={{width: '100%', minWidth: '120px', padding: '8px 12px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8}} onClick={handleReport}>
              <span role="img" aria-label="举报">🚩</span> 举报
            </button>
          )}
          {showBlock && (
            <button className="post-menu-item" style={{width: '100%', minWidth: '120px', padding: '8px 12px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8}} onClick={handleBlock}>
              <span role="img" aria-label="拉黑">⛔</span> 拉黑
            </button>
          )}
          {showDelete && onDelete && (
            <button className="post-menu-item" style={{width: '100%', minWidth: '120px', padding: '8px 12px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8}} onClick={handleDelete}>
              <span role="img" aria-label="删除">🗑️</span> 删除
            </button>
          )}
        </div>
      )}
    </div>
  );
} 