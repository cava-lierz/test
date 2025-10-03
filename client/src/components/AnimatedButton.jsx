import React, { useState } from 'react';

export default function AnimatedButton({ 
  children, 
  onClick, 
  style = {}, 
  variant = 'primary',
  size = 'medium',
  disabled = false 
}) {
  const [isPressed, setIsPressed] = useState(false);
  const [isHovered, setIsHovered] = useState(false);

  const baseStyle = {
    position: 'relative',
    border: 'none',
    borderRadius: '12px',
    cursor: disabled ? 'not-allowed' : 'pointer',
    fontWeight: '600',
    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
    overflow: 'hidden',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    transform: isPressed ? 'scale(0.95)' : isHovered ? 'scale(1.02)' : 'scale(1)',
    ...style
  };

  const sizeStyles = {
    small: { padding: '8px 16px', fontSize: '14px' },
    medium: { padding: '12px 24px', fontSize: '16px' },
    large: { padding: '16px 32px', fontSize: '18px' }
  };

  const variantStyles = {
    primary: {
      background: disabled 
        ? '#e9ecef' 
        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      color: disabled ? '#999' : 'white',
      boxShadow: disabled 
        ? 'none' 
        : isHovered 
          ? '0 8px 25px rgba(102, 126, 234, 0.4)' 
          : '0 4px 15px rgba(102, 126, 234, 0.3)'
    },
    secondary: {
      background: disabled 
        ? '#e9ecef' 
        : 'rgba(102, 126, 234, 0.1)',
      color: disabled ? '#999' : '#667eea',
      border: '2px solid',
      borderColor: disabled ? '#e9ecef' : 'rgba(102, 126, 234, 0.2)',
      boxShadow: 'none'
    },
    danger: {
      background: disabled 
        ? '#e9ecef' 
        : 'linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%)',
      color: disabled ? '#999' : 'white',
      boxShadow: disabled 
        ? 'none' 
        : isHovered 
          ? '0 8px 25px rgba(255, 107, 107, 0.4)' 
          : '0 4px 15px rgba(255, 107, 107, 0.3)'
    }
  };

  return (
    <button
      style={{
        ...baseStyle,
        ...sizeStyles[size],
        ...variantStyles[variant]
      }}
      onClick={disabled ? undefined : onClick}
      onMouseEnter={() => !disabled && setIsHovered(true)}
      onMouseLeave={() => !disabled && setIsHovered(false)}
      onMouseDown={() => !disabled && setIsPressed(true)}
      onMouseUp={() => !disabled && setIsPressed(false)}
      onTouchStart={() => !disabled && setIsPressed(true)}
      onTouchEnd={() => !disabled && setIsPressed(false)}
    >
      {/* 涟漪效果 */}
      {isHovered && !disabled && (
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            width: '0',
            height: '0',
            borderRadius: '50%',
            background: 'rgba(255, 255, 255, 0.3)',
            transform: 'translate(-50%, -50%)',
            animation: 'ripple 0.6s ease-out'
          }}
        />
      )}
      
      {/* 内容 */}
      <span style={{ position: 'relative', zIndex: 1 }}>
        {children}
      </span>
    </button>
  );
} 