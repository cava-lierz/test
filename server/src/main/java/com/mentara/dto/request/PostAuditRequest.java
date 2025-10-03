package com.mentara.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Post内容审核请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAuditRequest {
    
    /**
     * Post标题
     */
    private String title;
    
    /**
     * Post内容
     */
    private String content;
    
    /**
     * 作者ID（可选）
     */
    private Long authorId;
    
    /**
     * 是否为公告帖子
     */
    private Boolean isAnnouncement = false;
} 