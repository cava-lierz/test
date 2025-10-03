package com.mentara.service.notification.strategy;

import org.springframework.stereotype.Component;

import com.mentara.entity.Notification;
import com.mentara.entity.SystemNotification;
import com.mentara.service.notification.NotificationContext;
import com.mentara.service.notification.NotificationStrategy;

@Component
public class SystemNotificationStrategy implements NotificationStrategy {
    
    @Override
    public boolean shouldSend(NotificationContext context) {
        return true;
    }

    @Override
    public Notification createOrUpdate(NotificationContext context) {
        SystemNotification notification = new SystemNotification();
        notification.setRead(false);
        notification.setTitle(context.getTitle());
        notification.setContent(context.getContent());
        notification.setActionUrl(context.getActionUrl());
        
        return notification;
    }

    @Override
    public String getType() {
        return "SYSTEM";
    }
    
}
