import React, { useEffect } from 'react';

export default function SuccessModal({ message, onClose, duration = 3000 }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, duration);

    return () => clearTimeout(timer);
  }, [onClose, duration]);

  return (
    <div className="success-modal-overlay">
      <div className="success-modal-content">
        <div className="success-modal-icon">
          <span className="success-modal-icon-text">
            ✓
          </span>
        </div>
        
        <h3 className="success-modal-title">
          成功！
        </h3>
        
        <p className="success-modal-message">
          {message}
        </p>
        
        <button
          onClick={onClose}
          className="success-modal-button"
        >
          确定
        </button>
      </div>
    </div>
  );
} 