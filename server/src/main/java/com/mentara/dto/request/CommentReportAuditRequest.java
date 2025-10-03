package com.mentara.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 评论举报审核请求DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentReportAuditRequest {
    
    /**
     * 举报原因
     */
    private String reportReason;
    
    /**
     * 评论内容
     */
    private String commentContent;
    
    /**
     * 评论作者用户名（可选，用于上下文判断）
     */
    private String authorUsername;
    
    /**
     * 所属帖子标题（可选，用于上下文判断）
     */
    private String postTitle;
} 