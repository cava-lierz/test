package com.mentara.service.impl;

import com.mentara.entity.Notification;
import com.mentara.entity.User;
import com.mentara.entity.Post;
import com.mentara.entity.Comment;
import com.mentara.repository.NotificationRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.NotificationService;
import com.mentara.service.notification.NotificationContext;
import com.mentara.service.notification.NotificationStrategy;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.converter.NotificationResponseFactory;
import com.mentara.dto.request.SystemNotificationRequest;
import com.mentara.dto.response.BaseNotificationResponse;
import com.mentara.service.NotificationWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 重构后使用策略模式，支持异步发送
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationResponseFactory responseFactory;

    @Autowired
    private NotificationWebSocketService webSocketService;
    
    private final Map<String, NotificationStrategy> strategyMap;
    
    /**
     * 构造函数，初始化策略映射
     */
    public NotificationServiceImpl(List<NotificationStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(NotificationStrategy::getType, Function.identity()));
    }

    @Override
    public Page<BaseNotificationResponse> getUnreadNotificationsForUser(Long userId, Pageable pageable) {
        User receiver = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<Notification> notifications = notificationRepository.findByReceiverAndReadFalseOrderByCreatedAtDesc(receiver, pageable);
        return notifications.map(responseFactory::createResponse);
    }

    @Override
    @Transactional
    @Async("notificationExecutor")
    public void createAndSendSystemNotification(SystemNotificationRequest request) {
        try {
            log.info("开始异步处理系统通知: title={}", request.getTitle());
            
        NotificationContext context = NotificationContext.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .actionUrl(request.getActionUrl())
            .build();
        NotificationStrategy strategy = strategyMap.get("SYSTEM");
        if (strategy == null || !strategy.shouldSend(context)) return;
        Notification notification = strategy.createOrUpdate(context);
        notificationRepository.save(notification);
        BaseNotificationResponse response = responseFactory.createResponse(notification);
        // 系统通知广播给所有用户
        webSocketService.sendSystemNotificationAsync(response);
            
            log.info("系统通知处理完成: title={}", request.getTitle());
        } catch (Exception e) {
            log.error("异步处理系统通知失败: title={}, error={}", 
                request.getTitle(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Async("notificationExecutor")
    public void createAndSendPostLikeNotification(Post post, User liker) {
        try {
            log.info("开始异步处理帖子点赞通知: postId={}, likerId={}", post.getId(), liker.getId());
            
        NotificationContext context = NotificationContext.builder()
            .sender(liker)
            .receiver(post.getAuthor())
            .post(post)
            .build();
        NotificationStrategy strategy = strategyMap.get("POST_LIKE");
        if (strategy == null || !strategy.shouldSend(context)) return;
        Notification notification = strategy.createOrUpdate(context);
        notificationRepository.save(notification);
        BaseNotificationResponse response = responseFactory.createResponse(notification);
        webSocketService.sendNotificationAsync(notification.getReceiver().getId(), response);
            
            log.info("帖子点赞通知处理完成: postId={}, likerId={}", post.getId(), liker.getId());
        } catch (Exception e) {
            log.error("异步处理帖子点赞通知失败: postId={}, likerId={}, error={}", 
                post.getId(), liker.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Async("notificationExecutor")
    public void createAndSendPostReplyNotification(Post post, Comment reply, User replyUser) {
        try {
            log.info("开始异步处理帖子回复通知: postId={}, replyId={}", post.getId(), reply.getId());
            
        NotificationContext context = NotificationContext.builder()
            .sender(replyUser)
            .receiver(post.getAuthor())
            .post(post)
            .reply(reply)
            .build();
        NotificationStrategy strategy = strategyMap.get("POST_REPLY");
        if (strategy == null || !strategy.shouldSend(context)) return;
        Notification notification = strategy.createOrUpdate(context);
        notificationRepository.save(notification);
        BaseNotificationResponse response = responseFactory.createResponse(notification);
        webSocketService.sendNotificationAsync(notification.getReceiver().getId(), response);
            
            log.info("帖子回复通知处理完成: postId={}, replyId={}", post.getId(), reply.getId());
        } catch (Exception e) {
            log.error("异步处理帖子回复通知失败: postId={}, replyId={}, error={}", 
                post.getId(), reply.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Async("notificationExecutor")
    public void createAndSendCommentLikeNotification(Comment comment, User liker) {
        try {
            log.info("开始异步处理评论点赞通知: commentId={}, likerId={}", comment.getId(), liker.getId());
            
        NotificationContext context = NotificationContext.builder()
            .sender(liker)
            .receiver(comment.getAuthor())
            .comment(comment)
            .build();
        NotificationStrategy strategy = strategyMap.get("COMMENT_LIKE");
        if (strategy == null || !strategy.shouldSend(context)) return;
        Notification notification = strategy.createOrUpdate(context);
        notificationRepository.save(notification);
        BaseNotificationResponse response = responseFactory.createResponse(notification);
        webSocketService.sendNotificationAsync(notification.getReceiver().getId(), response);
            
            log.info("评论点赞通知处理完成: commentId={}, likerId={}", comment.getId(), liker.getId());
        } catch (Exception e) {
            log.error("异步处理评论点赞通知失败: commentId={}, likerId={}, error={}", 
                comment.getId(), liker.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Async("notificationExecutor")
    public void createAndSendCommentReplyNotification(Comment comment, Comment reply, User replyUser) {
        try {
            log.info("开始异步处理评论回复通知: commentId={}, replyId={}", comment.getId(), reply.getId());
            
        NotificationContext context = NotificationContext.builder()
            .sender(replyUser)
            .receiver(comment.getAuthor())
            .comment(comment)
            .reply(reply)
            .build();
        NotificationStrategy strategy = strategyMap.get("COMMENT_REPLY");
        if (strategy == null || !strategy.shouldSend(context)) return;
        Notification notification = strategy.createOrUpdate(context);
        notificationRepository.save(notification);
        BaseNotificationResponse response = responseFactory.createResponse(notification);
        webSocketService.sendNotificationAsync(notification.getReceiver().getId(), response);
            
            log.info("评论回复通知处理完成: commentId={}, replyId={}", comment.getId(), reply.getId());
        } catch (Exception e) {
            log.error("异步处理评论回复通知失败: commentId={}, replyId={}, error={}", 
                comment.getId(), reply.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new com.mentara.exception.ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new com.mentara.exception.ResourceNotFoundException("Notification", "id", notificationId); // 或自定义权限异常
        }
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        User receiver = userRepository.findById(userId)
            .orElseThrow(() -> new com.mentara.exception.ResourceNotFoundException("User", "id", userId));
        List<Notification> unreadNotifications = notificationRepository.findByReceiverAndReadFalse(receiver);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new com.mentara.exception.ResourceNotFoundException("Notification", "id", notificationId));
        
        // 检查权限：只有通知的接收者才能删除
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new com.mentara.exception.UnauthorizedException("只能删除自己的通知");
        }
        
        notificationRepository.delete(notification);
    }
    
    @Override
    @Transactional
    public void deleteAllNotifications(Long userId) {
        User receiver = userRepository.findById(userId)
            .orElseThrow(() -> new com.mentara.exception.ResourceNotFoundException("User", "id", userId));
        
        List<Notification> notifications = notificationRepository.findByReceiver(receiver);
        notificationRepository.deleteAll(notifications);
    }

} 