import { notificationAPI } from './api';

const notificationService = {
  /**
   * 获取未读通知（循环拉取所有分页）
   * @param {number} size
   * @returns {Promise<Array>} 所有未读通知数组
   */
  getUnreadNotifications: (size = 10) => {
    return notificationAPI.getUnreadNotifications(size);
  },

  /**
   * 标记单条通知为已读
   * @param {number} notificationId
   * @returns {Promise<void>}
   */
  markAsRead: (notificationId) => {
    return notificationAPI.markAsRead(notificationId);
  },

  /**
   * 一键全部标记为已读
   * @returns {Promise<void>}
   */
  markAllAsRead: () => {
    return notificationAPI.markAllAsRead();
  },
  
  /**
   * 删除单条通知
   * @param {number} notificationId
   * @returns {Promise<void>}
   */
  deleteNotification: (notificationId) => {
    return notificationAPI.deleteNotification(notificationId);
  },
  
  /**
   * 删除所有通知
   * @returns {Promise<void>}
   */
  deleteAllNotifications: () => {
    return notificationAPI.deleteAllNotifications();
  },
};

export default notificationService; 