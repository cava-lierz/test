import React from 'react';
import { Link } from 'react-router-dom';
import { getUserAvatarUrl } from '../utils/avatarUtils';

const UserCard = ({ 
  user, 
  onAction, 
  actionText, 
  actionType = 'secondary', 
  showBio = true,
  isSelected = false,
  onSelect = null,
  showCheckbox = false,
  onAvatarClick = null
}) => {
  return (
    <div className={`user-card ${actionType === 'danger' ? 'user-card-blocked' : ''} ${isSelected ? 'user-card-selected' : ''}`}>
      {showCheckbox && onSelect && (
        <div className="user-card-checkbox">
          <input
            type="checkbox"
            checked={isSelected}
            onChange={() => onSelect(user.id)}
          />
        </div>
      )}
      
      <div className="user-card-info">
        <div className="user-card-avatar-link">
          <img 
            src={getUserAvatarUrl(user)} 
            alt="用户头像" 
            className="user-card-avatar"
            onClick={() => onAvatarClick && onAvatarClick(user.id)}
            style={{ cursor: onAvatarClick ? 'pointer' : 'default' }}
          />
        </div>
        <div className="user-card-details">
          <div className="user-card-name">
            {user.name}
          </div>
          <div className="user-card-email">{user.email}</div>
          {showBio && user.bio && (
            <div className="user-card-bio">{user.bio}</div>
          )}
          {user.reason && (
            <div className="user-card-reason">
              <strong>拉黑原因：</strong>{user.reason}
            </div>
          )}
        </div>
      </div>
      <div className="user-card-actions">
        <button 
          className={`btn btn-${actionType}`}
          onClick={() => onAction(user.id)}
        >
          {actionText}
        </button>
      </div>
    </div>
  );
};

export default UserCard; 