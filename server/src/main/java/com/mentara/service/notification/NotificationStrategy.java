package com.mentara.service.notification;

import com.mentara.entity.Notification;

/**
 * 通知策略接口
 */
public interface NotificationStrategy {
    
    /**
     * 检查是否应该发送通知
     */
    boolean shouldSend(NotificationContext context);
    
    /**
     * 创建或更新通知
     */
    Notification createOrUpdate(NotificationContext context);
    
    /**
     * 获取通知类型
     */
    String getType();
}