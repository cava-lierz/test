package com.mentara.controller;

import com.mentara.dto.response.BaseNotificationResponse;
import com.mentara.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.mentara.security.UserPrincipal;
import com.mentara.security.CurrentUser;
import com.mentara.dto.request.SystemNotificationRequest;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * HTTP API: 获取通知列表
     */
    @GetMapping("/me/unread")
    public ResponseEntity<Page<BaseNotificationResponse>> getUnreadNotifications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser
    ) {
        Long userId = currentUser == null ? null : currentUser.getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<BaseNotificationResponse> notifications = notificationService.getUnreadNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /*
     * HTTP API: 发送系统通知
     */
    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendSystemNotification(@RequestBody SystemNotificationRequest request) {
        notificationService.createAndSendSystemNotification(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 标记单条通知为已读
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long notificationId, @CurrentUser UserPrincipal currentUser) {
        notificationService.markAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 一键全部标记为已读
     */
    @PatchMapping("/me/readAll")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除单条通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
        @PathVariable("id") Long notificationId, 
        @CurrentUser UserPrincipal currentUser
    ) {
        notificationService.deleteNotification(notificationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 删除所有通知
     */
    @DeleteMapping("/me/all")
    public ResponseEntity<Void> deleteAllNotifications(@CurrentUser UserPrincipal currentUser) {
        notificationService.deleteAllNotifications(currentUser.getId());
        return ResponseEntity.ok().build();
    }

} 