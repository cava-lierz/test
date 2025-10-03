package com.mentara.service;

import com.mentara.dto.request.PostReportAuditRequest;
import com.mentara.dto.response.PostReportAuditResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Post举报审核服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class PostReportAuditServiceTest {
    
    @Autowired
    private PostReportAuditService postReportAuditService;
    
    @Test
    public void testAuditValidReport() {
        // 测试有效的举报
        PostReportAuditRequest request = PostReportAuditRequest.builder()
                .reportReason("这个帖子包含暴力内容")
                .postTitle("我要杀人")
                .postContent("我今天要杀死所有人，这个世界太糟糕了")
                .build();
        
        PostReportAuditResponse response = postReportAuditService.auditPostReport(request);
        
        assertNotNull(response);
        assertNotNull(response.getIsValidReport());
        assertNotNull(response.getAuditReason());
        
        System.out.println("审核结果: " + response.getIsValidReport());
        System.out.println("审核原因: " + response.getAuditReason());
    }
    
    @Test
    public void testAuditInvalidReport() {
        // 测试无效的举报
        PostReportAuditRequest request = PostReportAuditRequest.builder()
                .reportReason("我不喜欢这个帖子")
                .postTitle("今天天气真好")
                .postContent("今天天气真的很不错，阳光明媚，适合出去散步")
                .build();
        
        PostReportAuditResponse response = postReportAuditService.auditPostReport(request);
        
        assertNotNull(response);
        assertNotNull(response.getIsValidReport());
        assertNotNull(response.getAuditReason());
        
        System.out.println("审核结果: " + response.getIsValidReport());
        System.out.println("审核原因: " + response.getAuditReason());
    }
    
    @Test
    public void testAuditWithNullValues() {
        // 测试空值处理
        PostReportAuditRequest request = PostReportAuditRequest.builder()
                .reportReason(null)
                .postTitle(null)
                .postContent(null)
                .build();
        
        PostReportAuditResponse response = postReportAuditService.auditPostReport(request);
        
        assertNotNull(response);
        assertNotNull(response.getIsValidReport());
        assertNotNull(response.getAuditReason());
        
        System.out.println("审核结果: " + response.getIsValidReport());
        System.out.println("审核原因: " + response.getAuditReason());
    }
} 