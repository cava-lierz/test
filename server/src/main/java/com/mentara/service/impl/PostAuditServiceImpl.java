package com.mentara.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentara.dto.response.PostAuditResponse;
import com.mentara.entity.Post;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.PostRepository;
import com.mentara.service.DeepSeekService;
import com.mentara.service.PostAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Post内容审核服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostAuditServiceImpl implements PostAuditService {
    
    private final DeepSeekService deepSeekService;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public PostAuditResponse auditPostContent(Post post) {
        try {
            log.info("开始审核Post内容，标题: {}", post.getTitle());
            
            // 构建发送给DeepSeek的提示信息
            String prompt = buildContentAuditPrompt(post);
            
            // 调用DeepSeek API进行审核
            String jsonResponse = deepSeekService.generateResponse(prompt, "deepseek-chat");
            
            // 解析JSON响应
            PostAuditResponse auditResponse = parseAuditResponse(jsonResponse);
            
            log.info("Post内容审核完成，结果: {}", auditResponse.getIsCompliant());
            return auditResponse;
            
        } catch (Exception e) {
            log.error("审核Post内容时发生异常", e);
            // 返回默认的审核结果
            return PostAuditResponse.builder()
                    .isCompliant(false)
                    .auditReason("审核过程中发生错误，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    @Override
    public PostAuditResponse auditPostById(Long postId) {
        try {
            // 根据Post ID查找帖子
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
            
            return auditPostContent(post);
            
        } catch (Exception e) {
            log.error("根据Post ID审核内容时发生异常，postId: {}", postId, e);
            return PostAuditResponse.builder()
                    .isCompliant(false)
                    .auditReason("查找帖子时发生错误")
                    .needAdminCheck(true)
                    .build();
        }
    }
    
    @Override
    public PostAuditResponse auditPostContent(String title, String content) {
        // 创建一个临时的Post对象用于审核
        Post tempPost = new Post();
        tempPost.setTitle(title);
        tempPost.setContent(content);
        tempPost.setIsAnnouncement(false);
        
        return auditPostContent(tempPost);
    }
    
    /**
     * 构建内容审核提示信息
     */
    private String buildContentAuditPrompt(Post post) {
        return String.format("""
            你是一个内容审核专家，拥有较高的审核权限。请审核以下帖子内容是否符合社区规范：
            
            帖子标题：%s
            帖子内容：%s
            是否为公告：%s
            
            请严格按照以下JSON格式返回审核结果：
            {
                "isCompliant": true/false,
                "auditReason": "审核理由说明",
                "needAdminCheck": true/false
            }
            
            审核标准（宽松版）：
            1. 允许轻微的不文明用语，只要不影响社区整体环境
            2. 允许合理的情绪表达，包括轻微的抱怨和吐槽
            3. 允许适度的争议性话题讨论，只要不涉及人身攻击
            4. 允许商业推广，但必须是合法且不欺诈的
            5. 公告帖子需要稍严格的审核标准
            
            严重违规标准（AI可直接删除）：
            1. 违法内容：涉及法律禁止的内容，如暴力、色情、毒品等
            2. 严重暴力：包含具体暴力行为描述或威胁
            3. 严重歧视：针对特定群体的恶意歧视言论
            4. 严重骚扰：恶意骚扰、人肉搜索等
            5. 严重欺诈：明显的诈骗、虚假广告等
            6. 严重垃圾信息：大量重复、无意义的垃圾内容
            
            审核建议：
            - 对于轻微不文明但无恶意的内容，可以宽容处理
            - 对于有争议但理性讨论的内容，可以允许
            - 对于明显违规的内容，AI有权直接拒绝
            - 对于难以判断的内容，建议人工审核
            
            请根据内容的严重程度和社区影响，给出合理的审核结果。
            """, 
            post.getTitle() != null ? post.getTitle() : "无标题",
            post.getContent() != null ? post.getContent() : "无内容",
            post.getIsAnnouncement() != null && post.getIsAnnouncement() ? "是" : "否"
        );
    }
    
    /**
     * 解析审核响应
     */
    private PostAuditResponse parseAuditResponse(String jsonResponse) {
        try {
            // 清理JSON字符串
            String cleanJson = jsonResponse
                    .replaceFirst("^\\s*```json\\s*", "")
                    .replaceFirst("\\s*```\\s*$", "");
            
            return objectMapper.readValue(cleanJson, PostAuditResponse.class);
            
        } catch (Exception e) {
            log.error("解析审核响应失败: {}", e.getMessage());
            // 如果解析失败，返回默认结果
            return PostAuditResponse.builder()
                    .isCompliant(false)
                    .auditReason("响应解析失败，建议人工审核")
                    .needAdminCheck(true)
                    .build();
        }
    }
} 