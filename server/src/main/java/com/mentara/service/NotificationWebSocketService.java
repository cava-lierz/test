package com.mentara.service;

import com.mentara.dto.response.BaseNotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知 WebSocket 异步服务
 * 专门负责异步发送 WebSocket 通知
 */
@Service
@Slf4j
public class NotificationWebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 异步发送 WebSocket 通知
     * @param receiverId 接收者ID
     * @param response 通知响应对象
     */
    @Async
    public void sendNotificationAsync(Long receiverId, BaseNotificationResponse response) {
        try {
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/notifications",
                response
            );
            log.debug("WebSocket通知发送成功: receiverId={}", receiverId);
        } catch (Exception e) {
            log.error("WebSocket通知发送失败: receiverId={}, error={}", receiverId, e.getMessage(), e);
        }
    }
    
    /**
     * 批量异步发送 WebSocket 通知
     * @param receiverIds 接收者ID列表
     * @param response 通知响应对象
     */
    @Async
    public void sendNotificationToMultipleUsersAsync(java.util.List<Long> receiverIds, BaseNotificationResponse response) {
        receiverIds.forEach(receiverId -> {
            try {
                messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/notifications",
                    response
                );
                log.debug("WebSocket通知发送成功: receiverId={}", receiverId);
            } catch (Exception e) {
                log.error("WebSocket通知发送失败: receiverId={}, error={}", receiverId, e.getMessage(), e);
            }
        });
    }
    
    /**
     * 异步广播系统通知到所有用户
     * @param response 通知响应对象
     */
    @Async
    public void sendSystemNotificationAsync(BaseNotificationResponse response) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", response);
            log.debug("系统通知广播发送成功");
        } catch (Exception e) {
            log.error("系统通知广播发送失败: error={}", e.getMessage(), e);
        }
    }
} 