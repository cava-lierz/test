package com.mentara.service.notification.strategy;

import com.mentara.entity.Notification;
import com.mentara.entity.PostReplyNotification;
import com.mentara.service.notification.NotificationContext;
import com.mentara.service.notification.NotificationStrategy;
import org.springframework.stereotype.Component;

/**
 * 帖子回复通知策略
 */
@Component
public class PostReplyNotificationStrategy implements NotificationStrategy {
    
    @Override
    public boolean shouldSend(NotificationContext context) {
        return !context.getSender().getId().equals(context.getReceiver().getId());
    }
    
    @Override
    public Notification createOrUpdate(NotificationContext context) {
        PostReplyNotification notification = new PostReplyNotification();
        notification.setReceiver(context.getReceiver());
        notification.setRead(false);
        notification.setPost(context.getPost());
        notification.setReply(context.getReply());
        notification.setReplyUser(context.getSender());
        
        return notification;
    }
    
    @Override
    public String getType() {
        return "POST_REPLY";
    }
}