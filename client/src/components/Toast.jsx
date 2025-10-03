import React, { useEffect, useState } from "react";
import "../styles/components.css";

const Toast = ({ message, type = "info", onClose, duration = 4000 }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isHiding, setIsHiding] = useState(false);

  useEffect(() => {
    // 显示Toast
    const showTimer = setTimeout(() => {
      setIsVisible(true);
    }, 100);

    // 自动隐藏Toast
    const hideTimer = setTimeout(() => {
      setIsHiding(true);
      setTimeout(() => {
        onClose();
      }, 300); // 等待淡出动画完成
    }, duration);

    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, [duration, onClose]);

  const handleClose = () => {
    setIsHiding(true);
    setTimeout(() => {
      onClose();
    }, 300);
  };

  const getIcon = () => {
    switch (type) {
      case "success":
        return "✅";
      case "error":
        return "❌";
      case "warning":
        return "⚠️";
      case "info":
      default:
        return "ℹ️";
    }
  };

  return (
    <div
      className={`toast-container toast-${type} ${
        isVisible ? "toast-visible" : ""
      } ${isHiding ? "toast-hidden" : ""}`}
    >
      <div className="toast-content">
        <span className="toast-icon">{getIcon()}</span>
        <span className="toast-message">{message}</span>
        <button className="toast-close" onClick={handleClose}>
          ×
        </button>
      </div>
    </div>
  );
};

export default Toast;
