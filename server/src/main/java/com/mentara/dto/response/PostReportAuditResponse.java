package com.mentara.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Post举报审核响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportAuditResponse {
    
    /**
     * 是否符合举报标准
     */
    private Boolean isValidReport;
    
    /**
     * 审核结果说明
     */
    private String auditReason;
    
    /**
     * 建议的处理方式
     */
    private Boolean needAdminCheck;
} 