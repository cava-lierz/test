package com.mentara.service;

import com.mentara.dto.response.PostAuditResponse;
import com.mentara.entity.Post;

/**
 * Post内容审核服务接口
 */
public interface PostAuditService {
    
    /**
     * 审核Post内容是否符合社区规范
     * 
     * @param post Post对象
     * @return 审核响应对象
     */
    PostAuditResponse auditPostContent(Post post);
    
    /**
     * 根据Post ID审核Post内容
     * 
     * @param postId Post ID
     * @return 审核响应对象
     */
    PostAuditResponse auditPostById(Long postId);
    
    /**
     * 审核Post标题和内容
     * 
     * @param title Post标题
     * @param content Post内容
     * @return 审核响应对象
     */
    PostAuditResponse auditPostContent(String title, String content);
} 