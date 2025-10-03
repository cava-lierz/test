package com.mentara.repository;

import com.mentara.entity.CommentReport;
import com.mentara.entity.User;
import com.mentara.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    
    /**
     * 检查用户是否已经举报过某个评论
     */
    boolean existsByUserAndComment(User user, Comment comment);
    
    /**
     * 根据用户和评论查找举报记录
     */
    Optional<CommentReport> findByUserAndComment(User user, Comment comment);
    
    /**
     * 统计某个评论的举报次数
     */
    @Query("SELECT COUNT(r) FROM CommentReport r WHERE r.comment.id = :commentId")
    Long countByCommentId(@Param("commentId") Long commentId);
    
    /**
     * 根据评论ID删除所有相关举报记录
     */
    void deleteByCommentId(Long commentId);
    
    /**
     * 根据评论查找所有举报记录，按创建时间倒序排列
     */
    List<CommentReport> findByCommentOrderByCreatedAtDesc(Comment comment);
    
    /**
     * 统计特定状态的举报数量
     */
    int countByState(CommentReport.State state);
    
    /**
     * 根据状态查找举报记录，按创建时间倒序排列
     */
    org.springframework.data.domain.Page<CommentReport> findByStateOrderByCreatedAtDesc(CommentReport.State state, org.springframework.data.domain.Pageable pageable);
} 