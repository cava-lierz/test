/**
 * 头像工具函数
 * 统一处理头像URL的构建
 */

/**
 * 构建完整的头像URL
 * @param {string} avatarUrl - 头像URL（可能是相对路径或绝对路径）
 * @param {string} defaultAvatar - 默认头像URL
 * @returns {string} 完整的头像URL
 */
export const buildAvatarUrl = (avatarUrl, defaultAvatar = null) => {
  if (!avatarUrl) {
    return defaultAvatar || 'https://i.pravatar.cc/150?u=default';
  }

  // 如果是绝对URL（包含http或https），直接返回
  if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
    return avatarUrl;
  }

  // 如果是相对路径，添加基础URL
  if (avatarUrl.startsWith('/')) {
    return `http://localhost:8080/api${avatarUrl}`;
  }

  // 如果是其他格式，返回默认头像
  return defaultAvatar || 'https://i.pravatar.cc/150?u=default';
};

/**
 * 获取用户头像URL
 * @param {Object} user - 用户对象
 * @param {string} defaultAvatar - 默认头像URL
 * @returns {string} 完整的头像URL
 */
export const getUserAvatarUrl = (user, defaultAvatar = null) => {
  if (!user || !user.avatar) {
    // 如果没有提供默认头像，基于用户ID或用户名生成唯一的默认头像
    if (!defaultAvatar) {
      const userId = user?.id || user?.username || 'default';
      defaultAvatar = `https://i.pravatar.cc/150?u=${userId}`;
    }
    return defaultAvatar;
  }
  return buildAvatarUrl(user.avatar, defaultAvatar);
};

/**
 * 获取评论头像URL
 * @param {Object} comment - 评论对象
 * @param {string} defaultAvatar - 默认头像URL
 * @returns {string} 完整的头像URL
 */
export const getCommentAvatarUrl = (comment, defaultAvatar = null) => {
  if (!comment || !comment.avatar) {
    // 基于评论用户信息生成唯一的默认头像
    if (!defaultAvatar) {
      const userId = comment?.authorId || comment?.authorName || 'default';
      defaultAvatar = `https://i.pravatar.cc/150?u=${userId}`;
    }
    return defaultAvatar;
  }
  return buildAvatarUrl(comment.avatar, defaultAvatar);
};