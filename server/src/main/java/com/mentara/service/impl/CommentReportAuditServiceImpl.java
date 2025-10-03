package com.mentara.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentara.dto.request.CommentReportAuditRequest;
import com.mentara.dto.response.CommentReportAuditResponse;
import com.mentara.entity.CommentReport;
import com.mentara.entity.Comment;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.CommentReportRepository;
import com.mentara.service.DeepSeekService;
import com.mentara.service.CommentReportAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 评论举报审核服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentReportAuditServiceImpl implements CommentReportAuditService {
    
    private final DeepSeekService deepSeekService;
    private final CommentReportRepository commentReportRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public CommentReportAuditResponse auditCommentReport(CommentReportAuditRequest request) {
        try {
            log.info("开始审核评论举报，举报原因: {}", request.getReportReason());
            
            // 构建发送给DeepSeek的提示信息
            String prompt = buildAuditPrompt(request);
            
            // 调用DeepSeek API进行审核
            String jsonResponse = deepSeekService.generateResponse(prompt, "deepseek-chat");
            
            // 解析JSON响应
            CommentReportAuditResponse auditResponse = parseAuditResponse(jsonResponse);
            
            log.info("评论举报审核完成，结果: {}", auditResponse.getIsValidReport());
            return auditResponse;
            
        } catch (Exception e) {
            log.error("审核评论举报时发生异常", e);
            // 返回默认的审核结果
            return CommentReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("审核过程中发生错误，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    @Override
    public CommentReportAuditResponse auditCommentReportById(Long reportId) {
        try {
            // 根据举报ID查找举报记录
            CommentReport report = commentReportRepository.findById(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("CommentReport", "id", reportId));
            
            Comment comment = report.getComment();
            
            // 构建审核请求
            CommentReportAuditRequest request = CommentReportAuditRequest.builder()
                    .reportReason(report.getReason())
                    .commentContent(comment.getContent())
                    .authorUsername(comment.getAuthor().getUsername())
                    .postTitle(comment.getPost().getTitle())
                    .build();
            
            return auditCommentReport(request);
            
        } catch (Exception e) {
            log.error("根据举报ID审核评论举报时发生异常，reportId: {}", reportId, e);
            return CommentReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("查找举报记录时发生错误")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    /**
     * 构建审核提示信息
     */
    private String buildAuditPrompt(CommentReportAuditRequest request) {
        return String.format("""
            你是一个内容审核专家。请根据以下信息判断这个评论举报是否合理：
            
            举报原因：%s
            评论内容：%s
            评论作者：%s
            所属帖子：%s
            
            请严格按照以下JSON格式返回审核结果：
            {
                "isValidReport": true/false,
                "auditReason": "审核理由说明",
                "needAdminCheck": true/false,
            }
            
            审核标准：
            1. 如果评论内容包含违法、暴力、色情、歧视、骚扰、人身攻击等不当内容，举报有效
            2. 如果评论内容正常，举报无效
            3. 如果举报原因不明确或与内容不符，举报无效
            4. 如果难以分辨举报原因和内容的符合程度，需要管理员人工检查
            5. 评论的审核标准相对帖子更严格，因为评论更容易产生冲突
            
            请仔细分析评论内容和举报原因，给出准确的审核结果。
            """, 
            request.getReportReason() != null ? request.getReportReason() : "无",
            request.getCommentContent() != null ? request.getCommentContent() : "无内容",
            request.getAuthorUsername() != null ? request.getAuthorUsername() : "未知用户",
            request.getPostTitle() != null ? request.getPostTitle() : "未知帖子"
        );
    }
    
    /**
     * 解析审核响应
     */
    private CommentReportAuditResponse parseAuditResponse(String jsonResponse) {
        try {
            // 尝试直接解析JSON
            return objectMapper.readValue(jsonResponse, CommentReportAuditResponse.class);
        } catch (Exception e) {
            log.warn("直接解析JSON失败，尝试提取JSON部分: {}", jsonResponse);
            
            // 如果直接解析失败，尝试从响应中提取JSON部分
            try {
                // 查找JSON开始和结束的位置
                int startIndex = jsonResponse.indexOf('{');
                int endIndex = jsonResponse.lastIndexOf('}');
                
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String jsonPart = jsonResponse.substring(startIndex, endIndex + 1);
                    return objectMapper.readValue(jsonPart, CommentReportAuditResponse.class);
                }
            } catch (Exception ex) {
                log.error("提取JSON部分也失败", ex);
            }
            
            // 如果都失败了，返回默认结果
            return CommentReportAuditResponse.builder()
                    .isValidReport(false)
                    .auditReason("AI响应格式错误，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
} 