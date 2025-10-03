package com.mentara.repository;

import com.mentara.entity.Report;
import com.mentara.entity.User;
import com.mentara.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    /**
     * 检查用户是否已经举报过某个帖子
     */
    boolean existsByUserAndPost(User user, Post post);
    
    /**
     * 根据用户和帖子查找举报记录
     */
    Optional<Report> findByUserAndPost(User user, Post post);
    
    /**
     * 统计某个帖子的举报次数
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    /**
     * 根据帖子ID删除所有相关举报记录
     */
    void deleteByPostId(Long postId);
    
    /**
     * 根据帖子查找所有举报记录，按创建时间倒序排列
     */
    List<Report> findByPostOrderByCreatedAtDesc(Post post);
    
    /**
     * 统计特定状态的举报数量
     */
    int countByState(Report.State state);
} 