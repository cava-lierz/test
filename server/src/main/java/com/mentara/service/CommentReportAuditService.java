package com.mentara.service;

import com.mentara.dto.request.CommentReportAuditRequest;
import com.mentara.dto.response.CommentReportAuditResponse;

/**
 * 评论举报审核服务接口
 */
public interface CommentReportAuditService {
    
    /**
     * 审核评论举报是否符合标准
     * 
     * @param request 审核请求对象
     * @return 审核响应对象
     */
    CommentReportAuditResponse auditCommentReport(CommentReportAuditRequest request);
    
    /**
     * 根据举报ID审核评论举报
     * 
     * @param reportId 举报ID
     * @return 审核响应对象
     */
    CommentReportAuditResponse auditCommentReportById(Long reportId);
} 