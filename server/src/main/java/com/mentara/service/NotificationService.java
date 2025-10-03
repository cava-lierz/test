package com.mentara.service;

import com.mentara.dto.response.BaseNotificationResponse;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mentara.dto.request.SystemNotificationRequest;

public interface NotificationService {
    
    /**
     * 获取用户的所有通知
     */
    Page<BaseNotificationResponse> getUnreadNotificationsForUser(Long userId, Pageable pageable);

    /*
     * 创建并发送系统通知
     */
    void createAndSendSystemNotification(SystemNotificationRequest request);

    /**
     * 创建并发送帖子点赞通知
     */
    void createAndSendPostLikeNotification(Post post, User liker);

    /**
     * 创建并发送帖子回复通知
     */
    void createAndSendPostReplyNotification(Post post, Comment reply, User replyUser);

    /**
     * 创建并发送评论点赞通知
     */
    void createAndSendCommentLikeNotification(Comment comment, User liker);

    /**
     * 创建并发送评论回复通知
     */
    void createAndSendCommentReplyNotification(Comment comment, Comment reply, User replyUser);

    /**
     * 获取用户的未读通知
     *
    List<BaseNotificationResponse> getUnreadNotifications(Long userId);
    
    /**
     * 获取用户的未读通知数量
     *
    long getUnreadNotificationCount(Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 删除单条通知
     */
    void deleteNotification(Long notificationId, Long userId);
    
    /**
     * 删除用户的所有通知
     */
    void deleteAllNotifications(Long userId);
}