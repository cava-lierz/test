package com.mentara.repository;

import com.mentara.entity.Comment;
import com.mentara.entity.CommentLike;
import com.mentara.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
    void deleteByCommentAndUser(Comment comment, User user);
    
    // 根据评论ID列表删除点赞记录
    void deleteByCommentIdIn(List<Long> commentIds);

    // 根据用户ID删除评论点赞记录
    void deleteByUserId(Long userId);
    
    // 批量查询用户对多个评论的点赞状态
    @org.springframework.data.jpa.repository.Query("SELECT cl FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<CommentLike> findByUserIdAndCommentIdIn(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("commentIds") List<Long> commentIds);
} 