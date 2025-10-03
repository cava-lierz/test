package com.mentara.converter;

import com.mentara.dto.response.*;
import com.mentara.entity.*;
import org.springframework.stereotype.Component;

@Component
public class NotificationResponseFactory {
    
    public BaseNotificationResponse createResponse(Notification notification) {
        if (notification instanceof PostLikeNotification) {
            return createPostLikeNotificationResponse((PostLikeNotification) notification);
        } else if (notification instanceof CommentLikeNotification) {
            return createCommentLikeNotificationResponse((CommentLikeNotification) notification);
        } else if (notification instanceof PostReplyNotification) {
            return createPostReplyNotificationResponse((PostReplyNotification) notification);
        } else if (notification instanceof CommentReplyNotification) {
            return createCommentReplyNotificationResponse((CommentReplyNotification) notification);
        } else if (notification instanceof SystemNotification) {
            return createSystemNotificationResponse((SystemNotification) notification);
        } else {
            return createGenericNotificationResponse(notification);
        }
    }
    
    private PostLikeNotificationResponse createPostLikeNotificationResponse(
        PostLikeNotification notification) {

        PostLikeNotificationResponse response = new PostLikeNotificationResponse();
        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        response.setType("post_like");
        response.setLikerCount(notification.getLikerCount());

        User lastLiker = notification.getLastLiker();
        Post post = notification.getPost();
        
        response.setLastLikerId(lastLiker.getId());
        response.setLastLikerNickname(lastLiker.getNickname());
        response.setPostId(post.getId());
        response.setPostTitle(post.getTitle());
        
        return response;
    }
    
    private CommentLikeNotificationResponse createCommentLikeNotificationResponse(
        CommentLikeNotification notification) {

        CommentLikeNotificationResponse response = new CommentLikeNotificationResponse();
        
        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        response.setType("comment_like");
        response.setLikerCount(notification.getLikerCount());

        User lastLiker = notification.getLastLiker();
        Comment comment = notification.getComment();
        Post post = comment.getPost();
        
        response.setLastLikerId(lastLiker.getId());
        response.setLastLikerNickname(lastLiker.getNickname());
        response.setPostId(post.getId());
        response.setPostTitle(post.getTitle());
        response.setCommentId(comment.getId());
        response.setCommentContent(comment.getContent().length() > 30 
            ? comment.getContent().substring(0, 30) + "..." 
            : comment.getContent());
        
        return response;
    }
    
    private PostReplyNotificationResponse createPostReplyNotificationResponse(
        PostReplyNotification notification) {
        PostReplyNotificationResponse response = new PostReplyNotificationResponse();

        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());

        response.setType("post_reply");

        User replyUser = notification.getReplyUser();
        Post post = notification.getPost();
        Comment reply = notification.getReply();

        response.setReplyUserId(replyUser.getId());
        response.setReplyUserNickname(replyUser.getNickname());
        response.setPostId(post.getId());
        response.setPostTitle(post.getTitle());
        response.setReplyId(reply.getId());
        response.setReplyContent(reply.getContent().length() > 30 
            ? reply.getContent().substring(0, 30) + "..." 
            : reply.getContent());

        return response;
    }
    
    private CommentReplyNotificationResponse createCommentReplyNotificationResponse(
        CommentReplyNotification notification) {
        CommentReplyNotificationResponse response = new CommentReplyNotificationResponse();
        
        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());
        response.setType("comment_reply");
        
        User replyUser = notification.getReplyUser();
        Comment comment = notification.getComment();
        Comment reply = notification.getReply();
        Post post = comment.getPost();

        response.setReplyUserId(replyUser.getId());
        response.setReplyUserNickname(replyUser.getNickname());
        response.setPostId(post.getId());
        response.setPostTitle(post.getTitle());
        response.setCommentId(comment.getId());
        response.setCommentContent(comment.getContent().length() > 30 
            ? comment.getContent().substring(0, 30) + "..." 
            : comment.getContent());
        response.setReplyId(reply.getId());
        response.setReplyContent(reply.getContent().length() > 30 
            ? reply.getContent().substring(0, 30) + "..." 
            : reply.getContent());

        return response;
    }

    private SystemNotificationResponse createSystemNotificationResponse(
        SystemNotification notification) {
        SystemNotificationResponse response = new SystemNotificationResponse();

        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());
        response.setType("system");
        
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setActionUrl(notification.getActionUrl());

        return response;
    }
    
    private BaseNotificationResponse createGenericNotificationResponse(Notification notification) {
        BaseNotificationResponse response = new BaseNotificationResponse();
        
        // 设置基础字段
        response.setId(notification.getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setRead(notification.isRead());
        response.setType("other");
        
        return response;
    }
} 