package com.mentara.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentara.dto.request.PostReportAuditRequest;
import com.mentara.dto.response.PostReportAuditResponse;
import com.mentara.entity.Report;
import com.mentara.entity.Post;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.ReportRepository;
import com.mentara.service.DeepSeekService;
import com.mentara.service.PostReportAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Post举报审核服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostReportAuditServiceImpl implements PostReportAuditService {
    
    private final DeepSeekService deepSeekService;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public PostReportAuditResponse auditPostReport(PostReportAuditRequest request) {
        try {
            log.info("开始审核Post举报，举报原因: {}", request.getReportReason());
            
            // 构建发送给DeepSeek的提示信息
            String prompt = buildAuditPrompt(request);
            
            // 调用DeepSeek API进行审核
            String jsonResponse = deepSeekService.generateResponse(prompt, "deepseek-chat");
            
            // 解析JSON响应
            PostReportAuditResponse auditResponse = parseAuditResponse(jsonResponse);
            
            log.info("Post举报审核完成，结果: {}", auditResponse.getIsValidReport());
            return auditResponse;
            
        } catch (Exception e) {
            log.error("审核Post举报时发生异常", e);
            // 返回默认的审核结果
            return PostReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("审核过程中发生错误，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    @Override
    public PostReportAuditResponse auditPostReportById(Long reportId) {
        try {
            // 根据举报ID查找举报记录
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
            
            Post post = report.getPost();
            
            // 构建审核请求
            PostReportAuditRequest request = PostReportAuditRequest.builder()
                    .reportReason(report.getReason())
                    .postTitle(post.getTitle())
                    .postContent(post.getContent())
                    .build();
            
            return auditPostReport(request);
            
        } catch (Exception e) {
            log.error("根据举报ID审核Post举报时发生异常，reportId: {}", reportId, e);
            return PostReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("查找举报记录时发生错误")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    /**
     * 构建审核提示信息
     */
    private String buildAuditPrompt(PostReportAuditRequest request) {
        return String.format("""
            你是一个内容审核专家。请根据以下信息判断这个帖子举报是否合理：
            
            举报原因：%s
            帖子标题：%s
            帖子内容：%s
            
            请严格按照以下JSON格式返回审核结果：
            {
                "isValidReport": true/false,
                "auditReason": "审核理由说明",
                "needAdminCheck": true/false,
            }
            
            审核标准：
            1. 如果帖子内容包含违法、暴力、色情、歧视、骚扰等不当内容，举报有效
            2. 如果帖子内容正常，举报无效
            3. 如果举报原因不明确或与内容不符，举报无效
            4. 如果难以分辨举报原因和内容的符合程度，需要管理员人工检查
            
            请仔细分析帖子内容和举报原因，给出准确的审核结果。
            """, 
            request.getReportReason() != null ? request.getReportReason() : "无",
            request.getPostTitle() != null ? request.getPostTitle() : "无标题",
            request.getPostContent() != null ? request.getPostContent() : "无内容"
        );
    }
    
    /**
     * 解析审核响应
     */
    private PostReportAuditResponse parseAuditResponse(String jsonResponse) {
        try {
            // 清理JSON字符串
            String cleanJson = jsonResponse
                    .replaceFirst("^\\s*```json\\s*", "")
                    .replaceFirst("\\s*```\\s*$", "");
            
            return objectMapper.readValue(cleanJson, PostReportAuditResponse.class);
            
        } catch (Exception e) {
            log.error("解析审核响应失败: {}", e.getMessage());
            // 如果解析失败，返回默认结果
            return PostReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("响应解析失败，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
} 