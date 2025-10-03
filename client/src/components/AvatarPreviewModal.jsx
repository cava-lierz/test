import React from 'react';
import { createPortal } from 'react-dom';
import '../styles/components.css';

const AvatarPreviewModal = ({ isOpen, onClose, avatarUrl, userName }) => {
  if (!isOpen) return null;

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return createPortal(
    <div className="avatar-preview-modal-overlay" onClick={handleBackdropClick}>
      <div className="avatar-preview-modal">
        <div className="avatar-preview-header">
          <h3 className="avatar-preview-title">
            {userName || '头像预览'}
          </h3>
          <button 
            className="avatar-preview-close-btn" 
            onClick={onClose}
            aria-label="关闭"
          >
            ✕
          </button>
        </div>
        
        <div className="avatar-preview-content">
          <img
            src={avatarUrl}
            alt={userName ? `${userName}` : '用户头像'}
            className="avatar-preview-image"
            onError={(e) => {
              e.target.src = 'https://i.pravatar.cc/150?u=default';
            }}
          />
        </div>
      </div>
    </div>,
    document.body
  );
};

export default AvatarPreviewModal; 