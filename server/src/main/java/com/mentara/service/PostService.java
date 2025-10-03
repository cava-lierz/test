package com.mentara.service;

import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.PostResponse;
import com.mentara.dto.response.ReportedPostResponse;
import com.mentara.enums.MoodType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    // 基础实体操作方法（主要供内部使用）
    PostResponse findById(Long id, Long currentUserId);
    Page<PostResponse> findAllPosts(Pageable pageable, Long currentUserId);
    Page<PostResponse> findPostsByUser(Long userId, Pageable pageable, Long currentUserId);
    PostResponse createPostForUser(PostRequest postRequest, Long currentUserId);
    void deletePost(Long postId, Long currentUserId);
    void likePost(Long postId, Long currentUserId);
    void unlikePost(Long postId, Long currentUserId);
    boolean isPostLikedByUser(Long postId, Long userId);
    boolean canUserDeletePost(Long postId, Long userId);
    
    // 新增：筛选方法
    Page<PostResponse> findPostsByFilter(String filter, Pageable pageable, Long currentUserId);
    Page<PostResponse> findPostsByTags(List<Long> tagIds, Pageable pageable, Long currentUserId);
    Page<PostResponse> findPostsByMood(MoodType mood, Pageable pageable, Long currentUserId);

    Page<PostResponse> searchPosts(String filter, Pageable pageable, Long currentUserId);
    // 举报相关方法
    void reportPost(Long postId, Long currentUserId, String reason);
    Page<ReportedPostResponse> getReportedPosts(Pageable pageable);
    void ignorePostReports(Long postId);
    Integer getTotalReportedPostsCount();
    
    // ================== 软删除相关方法 ==================
    
    /**
     * 恢复已删除的帖子
     */
    void restorePost(Long postId);
    
    /**
     * 获取指定用户的已删除帖子（管理员查看）
     */
    Page<PostResponse> getDeletedPostsByUser(Long userId, Pageable pageable);
    
    // ================== 管理员审核功能 ==================
    
    /**
     * 获取待审核的帖子（管理员功能）
     */
    Page<PostResponse> getPendingAuditPosts(Pageable pageable);
    
    /**
     * 获取需要人工审核的帖子（管理员功能）
     */
    Page<PostResponse> getNeedAdminCheckPosts(Pageable pageable);
    
    /**
     * 获取已拒绝的帖子（管理员功能）
     */
    Page<PostResponse> getInvalidPosts(Pageable pageable);
    
    /**
     * 获取已删除的帖子（管理员功能）
     */
    Page<PostResponse> getDeletedPosts(Pageable pageable);
    
    /**
     * 管理员通过帖子
     */
    void approvePost(Long postId);
    
    /**
     * 管理员拒绝帖子
     */
    void rejectPost(Long postId);
    
    /**
     * 管理员删除帖子
     */
    void deletePostByAdmin(Long postId);
    
    /**
     * 管理员改变帖子状态
     */
    void changePostStatus(Long postId, String status);
    
    /**
     * 管理员通过帖子举报
     */
    void approvePostReport(Long postId);
    
    // ================== 区分删除原因的查询方法 ==================
    
    /**
     * 获取被举报删除的帖子（管理员功能）
     */
    Page<PostResponse> getReportedDeletedPosts(Pageable pageable);
    
    /**
     * 获取被审核删除的帖子（管理员功能）
     */
    Page<PostResponse> getAuditDeletedPosts(Pageable pageable);
    
    /**
     * 获取被举报删除的帖子数量
     */
    Integer getReportedDeletedPostsCount();
    
    /**
     * 获取被审核删除的帖子数量
     */
    Integer getAuditDeletedPostsCount();
}