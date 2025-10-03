import React from 'react';

export default function ConfirmModal({ title, message, onConfirm, onCancel, confirmText = '确定', cancelText = '取消' }) {
  return (
    <div className="confirm-modal-overlay">
      <div className="confirm-modal-content">
        <div className="confirm-modal-icon">
          <span className="confirm-modal-icon-text">
            ⚠️
          </span>
        </div>
        
        <h3 className="confirm-modal-title">
          {title}
        </h3>
        
        <p className="confirm-modal-message">
          {message}
        </p>
        
        <div className="confirm-modal-actions">
          <button
            onClick={onCancel}
            className="confirm-modal-cancel-button"
          >
            {cancelText}
          </button>
          
          <button
            onClick={() => {
              if (onConfirm) {
                onConfirm();
              } else {
                onCancel(); // Fallback to onCancel if onConfirm is not provided
              }
            }}
            className="confirm-modal-confirm-button"
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
} 