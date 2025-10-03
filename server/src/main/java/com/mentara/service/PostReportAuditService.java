package com.mentara.service;

import com.mentara.dto.request.PostReportAuditRequest;
import com.mentara.dto.response.PostReportAuditResponse;

/**
 * Post举报审核服务接口
 */
public interface PostReportAuditService {
    
    /**
     * 审核Post举报是否符合标准
     * 
     * @param request 审核请求对象
     * @return 审核响应对象
     */
    PostReportAuditResponse auditPostReport(PostReportAuditRequest request);
    
    /**
     * 根据举报ID审核Post举报
     * 
     * @param reportId 举报ID
     * @return 审核响应对象
     */
    PostReportAuditResponse auditPostReportById(Long reportId);
} 