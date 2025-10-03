package com.mentara.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 评论举报审核响应DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentReportAuditResponse {
    
    /**
     * 举报是否有效
     */
    private Boolean isValidReport;
    
    /**
     * 审核理由
     */
    private String auditReason;
    
    /**
     * 是否需要管理员人工审核
     */
    private Boolean needAdminCheck;
} 