package com.mentara.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Post举报审核请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportAuditRequest {
    
    /**
     * 举报原因
     */
    private String reportReason;
    
    /**
     * 帖子标题
     */
    private String postTitle;
    
    /**
     * 帖子内容
     */
    private String postContent;
} 