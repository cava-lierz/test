package com.mentara.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Post内容审核响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAuditResponse {
    
    /**
     * 内容是否合规
     */
    private Boolean isCompliant;
    
    /**
     * 审核结果说明
     */
    private String auditReason;
    
    /**
     * 是否需要人工审核
     */
    private Boolean needAdminCheck;
} 