package com.mentara.service;

import com.mentara.dto.request.CommentRequest;
import com.mentara.dto.response.CommentResponse;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse createComment(CommentRequest commentRequest, Long authorId);

    Page<CommentResponse> getCommentsOfPost(Long postId, Pageable pageable, Long currentUserId);

    Page<CommentResponse> getRepliesOfComment(Long commentId, Pageable pageable, Long currentUserId);

    CommentResponse getComment(Long commentId, Long currentUserId);

    Page<CommentResponse> getRepliesOfTopComment(Long commentId, Pageable pageable, Long currentUserId);

    int calculateLastPageOfPost(Long postId, int size);

    int calculateLastPageOfTopComment(Long commentId, int size);

    int calculatePageOfReplyInTopComment(Long parentId, int size);

    void deleteComment(Long commentId, Long userId);

    void likeComment(Long commentId, Long userId);

    void unlikeComment(Long commentId, Long userId);

    boolean isCommentLikedByUser(Long commentId, Long userId);

    boolean canUserDeleteComment(Long commentId, Long userId);
    
    // 举报相关方法
    void reportComment(Long commentId, Long currentUserId, String reason);
    org.springframework.data.domain.Page<com.mentara.dto.response.ReportedCommentResponse> getReportedComments(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<com.mentara.dto.response.ReportedCommentResponse> getWaitingCommentReports(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<com.mentara.dto.response.ReportedCommentResponse> getValidCommentReports(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<com.mentara.dto.response.ReportedCommentResponse> getInvalidCommentReports(org.springframework.data.domain.Pageable pageable);
    void deleteCommentByAdmin(Long commentId);
    void forceDeleteComment(Long commentId);
    void approveCommentReport(Long commentId);
    void ignoreCommentReports(Long commentId);
    Integer getTotalReportedCommentsCount();
    
    // ================== 软删除相关方法 ==================
    
    /**
     * 恢复已删除的评论
     */
    void restoreComment(Long commentId);
    
    /**
     * 获取所有已删除的评论（管理员查看）
     */
    Page<CommentResponse> getDeletedComments(Pageable pageable);
    
    /**
     * 获取指定用户的已删除评论
     */
    Page<CommentResponse> getDeletedCommentsByUser(Long userId, Pageable pageable);
} 