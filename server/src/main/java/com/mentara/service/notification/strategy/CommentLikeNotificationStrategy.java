package com.mentara.service.notification.strategy;

import com.mentara.entity.CommentLikeNotification;
import com.mentara.entity.Notification;
import com.mentara.repository.NotificationRepository;
import com.mentara.service.notification.NotificationContext;
import com.mentara.service.notification.NotificationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 评论点赞通知策略
 */
@Component
public class CommentLikeNotificationStrategy implements NotificationStrategy {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Override
    public boolean shouldSend(NotificationContext context) {
        return !context.getSender().getId().equals(context.getReceiver().getId());
    }
    
    @Override
    public Notification createOrUpdate(NotificationContext context) {
        Optional<CommentLikeNotification> existingNotification = 
            notificationRepository.findUnreadCommentLikeNotificationByReceiverAndComment(
                context.getReceiver(), context.getComment());
        
        CommentLikeNotification notification;
        if (existingNotification.isPresent()) {
            // 更新现有通知
            notification = existingNotification.get();
            notification.setLastLiker(context.getSender());
            notification.setLikerCount(notification.getLikerCount() + 1);
            notification.setCreatedAt(LocalDateTime.now());
        } else {
            // 创建新通知
            notification = new CommentLikeNotification();
            notification.setReceiver(context.getReceiver());
            notification.setRead(false);
            notification.setLastLiker(context.getSender());
            notification.setLikerCount(1);
            notification.setComment(context.getComment());
        }
        
        return notification;
    }
    
    @Override
    public String getType() {
        return "COMMENT_LIKE";
    }
}