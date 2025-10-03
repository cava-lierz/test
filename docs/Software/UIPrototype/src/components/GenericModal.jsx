import React from 'react';

const GenericModal = ({ isOpen, onClose, children }) => {
  if (!isOpen) return null;

  return (
    <div className="generic-modal-overlay" onClick={onClose}>
      <div className="generic-modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="generic-modal-close-button" onClick={onClose}>
          &times;
        </button>
        {children}
      </div>
    </div>
  );
};

export default GenericModal; 