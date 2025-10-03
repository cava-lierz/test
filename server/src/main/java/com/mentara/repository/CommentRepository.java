package com.mentara.repository;

import com.mentara.entity.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    Page<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);
    
    Page<Comment> findByTopCommentIdOrderByCreatedAtAsc(Long topCommentId, Pageable pageable);

    int countByPostIdAndParentIsNull(Long postId);

    int countByTopCommentId(Long topCommentId);

    @Modifying
    @Query("UPDATE Comment c SET c.replysCount = c.replysCount + :increment WHERE c.id = :commentId")
    void updateReplyCount(@Param("commentId") Long commentId, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + :increment WHERE c.id = :commentId")
    void updateLikeCount(@Param("commentId") Long commentId, @Param("increment") int increment);

    @Query("SELECT COUNT(c.id) FROM Comment c WHERE c.topComment.id = :topCommentId AND c.createdAt < :createdAt")
    int countRepliesBeforeInTopComment(@Param("topCommentId") Long topCommentId, @Param("createdAt") LocalDateTime createdAt);
    
    // 软删除版本的统计方法
    @Query("SELECT COUNT(c.id) FROM Comment c WHERE c.topComment.id = :topCommentId AND c.createdAt < :createdAt AND c.isDeleted = false")
    int countRepliesBeforeInTopCommentAndIsDeletedFalse(@Param("topCommentId") Long topCommentId, @Param("createdAt") LocalDateTime createdAt);
    
    // 统计用户评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId")
    Integer countByAuthorId(@Param("authorId") Long authorId);
    
    // 根据帖子ID查找所有评论ID
    @Query("SELECT c.id FROM Comment c WHERE c.post.id = :postId")
    List<Long> findCommentIdsByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.author.id = :authorId")
    void deleteByAuthorId(@Param("authorId") Long authorId);
    
    // 举报相关查询方法
    @Query("SELECT c FROM Comment c WHERE c.reportCount > 0 ORDER BY c.reportCount DESC")
    org.springframework.data.domain.Page<Comment> findByReportCountGreaterThanOrderByReportCountDesc(
        org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.reportCount > 0")
    Integer countByReportCountGreaterThan();
    
    @Modifying
    @Query("UPDATE Comment c SET c.reportCount = c.reportCount + :increment WHERE c.id = :commentId")
    void updateReportCount(@Param("commentId") Long commentId, @Param("increment") int increment);
    
    // ================== 软删除相关查询方法 ==================
    
    // 查询未删除的评论（按帖子）
    Page<Comment> findByPostIdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    // 查询未删除的回复（按父评论）
    Page<Comment> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentId, Pageable pageable);
    
    // 查询未删除的回复（按顶级评论）
    Page<Comment> findByTopCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long topCommentId, Pageable pageable);
    
    // 统计未删除的评论数量
    int countByPostIdAndParentIsNullAndIsDeletedFalse(Long postId);
    
    // 统计未删除的回复数量
    int countByTopCommentIdAndIsDeletedFalse(Long topCommentId);
    
    // 查询已删除的评论（管理员查看）
    Page<Comment> findByIsDeletedTrueOrderByDeletedAtDesc(Pageable pageable);
    
    // 查询指定用户的已删除评论
    Page<Comment> findByAuthorIdAndIsDeletedTrueOrderByDeletedAtDesc(Long authorId, Pageable pageable);
    
    // 查询被举报删除的评论（已删除且举报次数大于0）
    @Query("SELECT c FROM Comment c WHERE c.isDeleted = true AND c.reportCount > 0 ORDER BY c.deletedAt DESC")
    Page<Comment> findByIsDeletedTrueAndReportCountGreaterThanOrderByDeletedAtDesc(Pageable pageable);
    
    // 统计用户未删除的评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId AND c.isDeleted = false")
    Integer countActiveCommentsByAuthorId(@Param("authorId") Long authorId);
    
    // 举报相关查询方法（只查询未删除的评论）
    @Query("SELECT c FROM Comment c WHERE c.isDeleted = false AND c.reportCount > 0 ORDER BY c.reportCount DESC")
    org.springframework.data.domain.Page<Comment> findByIsDeletedFalseAndReportCountGreaterThanOrderByReportCountDesc(
        org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.isDeleted = false AND c.reportCount > 0")
    Integer countByIsDeletedFalseAndReportCountGreaterThan();
    
    // 软删除评论
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true, c.deletedAt = :deletedAt, c.deletedBy = :deletedBy, c.deleteReason = :deleteReason WHERE c.id = :commentId")
    void softDeleteComment(@Param("commentId") Long commentId, @Param("deletedAt") LocalDateTime deletedAt, 
                          @Param("deletedBy") Long deletedBy, @Param("deleteReason") String deleteReason);
    
    // 恢复评论
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = false, c.deletedAt = null, c.deletedBy = null, c.deleteReason = null WHERE c.id = :commentId")
    void restoreComment(@Param("commentId") Long commentId);
}