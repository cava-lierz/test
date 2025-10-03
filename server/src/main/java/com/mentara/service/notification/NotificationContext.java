package com.mentara.service.notification;

import com.mentara.entity.User;
import com.mentara.entity.Post;
import com.mentara.entity.Comment;
import lombok.Builder;
import lombok.Data;

/**
 * 通知上下文，包含创建通知所需的所有信息
 */
@Data
@Builder
public class NotificationContext {
    private User sender;      // 发送者（点赞者、回复者等）
    private User receiver;    // 接收者
    private Post post;        // 相关帖子
    private Comment comment;  // 相关评论
    private Comment reply;    // 相关回复
    
    private String title;     // 系统通知标题
    private String content;   // 系统通知内容
    private String actionUrl; // 系统通知链接
}