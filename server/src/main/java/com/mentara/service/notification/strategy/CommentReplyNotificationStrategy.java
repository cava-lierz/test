package com.mentara.service.notification.strategy;

import com.mentara.entity.CommentReplyNotification;
import com.mentara.entity.Notification;
import com.mentara.service.notification.NotificationContext;
import com.mentara.service.notification.NotificationStrategy;
import org.springframework.stereotype.Component;

/**
 * 评论回复通知策略
 */
@Component
public class CommentReplyNotificationStrategy implements NotificationStrategy {
    
    @Override
    public boolean shouldSend(NotificationContext context) {
        return !context.getSender().getId().equals(context.getReceiver().getId());
    }
    
    @Override
    public Notification createOrUpdate(NotificationContext context) {
        CommentReplyNotification notification = new CommentReplyNotification();
        notification.setReceiver(context.getReceiver());
        notification.setRead(false);
        notification.setComment(context.getComment());
        notification.setReply(context.getReply());
        notification.setReplyUser(context.getSender());
        
        return notification;
    }
    
    @Override
    public String getType() {
        return "COMMENT_REPLY";
    }
}