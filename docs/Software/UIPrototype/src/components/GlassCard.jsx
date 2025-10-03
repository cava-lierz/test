import React from 'react';

export default function GlassCard({ children, style = {}, className = '' }) {
  return (
    <div
      className={`glass-card ${className}`}
      style={style}
    >
      {/* 内容容器 */}
      <div className="glass-card-content">
        {children}
      </div>
    </div>
  );
} 