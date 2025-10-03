package com.mentara.repository;

import com.mentara.entity.Post;
import com.mentara.enums.MoodType;
import com.mentara.enums.PostState;
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
public interface PostRepository extends JpaRepository<Post, Long> {
    
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // 新增：按点赞数排序（最热）
    Page<Post> findAllByOrderByLikesCountDescCreatedAtDesc(Pageable pageable);
    
    // 新增：筛选有心情的帖子
    Page<Post> findByMoodIsNotNullOrderByCreatedAtDesc(Pageable pageable);
    
    // 新增：按心情类型筛选
    Page<Post> findByMoodOrderByCreatedAtDesc(MoodType mood, Pageable pageable);
    
    // 新增：按标签筛选
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id IN :tagIds GROUP BY p HAVING COUNT(t) = :tagCount ORDER BY p.createdAt DESC")
    Page<Post> findByTagIds(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount, Pageable pageable);
    
    // 新增：按标签筛选（软删除版本）
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.isDeleted = false AND t.id IN :tagIds GROUP BY p HAVING COUNT(t) = :tagCount ORDER BY p.createdAt DESC")
    Page<Post> findByTagIdsAndIsDeletedFalse(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount, Pageable pageable);
    
    // 新增：按时间范围筛选（最近6小时）
    Page<Post> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime time, Pageable pageable);
    
    // 新增：按时间范围筛选（软删除版本）
    Page<Post> findByIsDeletedFalseAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime time, Pageable pageable);
    
    // 新增：按点赞数筛选（最热）
    Page<Post> findByLikesCountGreaterThanEqualOrderByLikesCountDesc(Integer minLikes, Pageable pageable);

    // 新增：按点赞数筛选（软删除版本）
    Page<Post> findByIsDeletedFalseAndLikesCountGreaterThanEqualOrderByLikesCountDesc(Integer minLikes, Pageable pageable);
    
    // 新增：按心情筛选（软删除版本）
    Page<Post> findByMoodIsNotNullAndIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    // 新增：按心情类型筛选（软删除版本）
    Page<Post> findByMoodAndIsDeletedFalseOrderByCreatedAtDesc(MoodType mood, Pageable pageable);
    
    // 新增：按时间范围筛选（已通过且软删除版本）- 公告帖子置顶
    Page<Post> findByStateAndIsDeletedFalseAndCreatedAtAfterOrderByIsAnnouncementDescCreatedAtDesc(PostState state, LocalDateTime time, Pageable pageable);
    
    // 新增：按点赞数筛选（已通过且软删除版本）- 公告帖子置顶
    Page<Post> findByStateAndIsDeletedFalseAndLikesCountGreaterThanEqualOrderByIsAnnouncementDescLikesCountDesc(PostState state, Integer minLikes, Pageable pageable);
    
    // 新增：按心情筛选（已通过且软删除版本）- 公告帖子置顶
    Page<Post> findByStateAndMoodIsNotNullAndIsDeletedFalseOrderByIsAnnouncementDescCreatedAtDesc(PostState state, Pageable pageable);
    
    // 新增：按心情类型筛选（已通过且软删除版本）- 公告帖子置顶
    Page<Post> findByStateAndMoodAndIsDeletedFalseOrderByIsAnnouncementDescCreatedAtDesc(PostState state, MoodType mood, Pageable pageable);
    
    
    // 新增：按标签筛选（已通过且软删除版本）- 公告帖子置顶
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.state = :state AND p.isDeleted = false AND t.id IN :tagIds GROUP BY p HAVING COUNT(t) = :tagCount ORDER BY p.isAnnouncement DESC, p.createdAt DESC")
    Page<Post> findByStateAndTagIdsAndIsDeletedFalse(@Param("state") PostState state, @Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount, Pageable pageable);
    
    // 新增：按作者ID筛选（软删除版本）
    Page<Post> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    // 新增：按ID列表查询（用于向量搜索）
    @Query("SELECT p FROM Post p WHERE p.id IN :postIds ORDER BY p.createdAt DESC")
    List<Post> findByIdInOrderByCreatedAtDesc(@Param("postIds") List<Long> postIds);
    
    // 新增：按ID列表查询（软删除版本）
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.id IN :postIds ORDER BY p.createdAt DESC")
    List<Post> findByIdInAndIsDeletedFalseOrderByCreatedAtDesc(@Param("postIds") List<Long> postIds);
    
    // 新增：按举报次数筛选（软删除版本）
    Page<Post> findByIsDeletedFalseAndReportCountGreaterThanOrderByReportCountDesc(Integer minReports, Pageable pageable);
    
    // 统计被举报的帖子数量（所有状态）
    int countByReportCountGreaterThan(int minReportCount);
    
    // 统计某个用户发布的帖子数量
    int countByAuthorId(Long authorId);
    
    // 新增：按软删除状态筛选
    Page<Post> findByIsDeletedTrueOrderByDeletedAtDesc(Pageable pageable);
    
    // 新增：按作者ID和软删除状态筛选
    Page<Post> findByAuthorIdAndIsDeletedTrueOrderByDeletedAtDesc(Long authorId, Pageable pageable);
    
    // ================== 基于PostState的查询方法 ==================
    
    // 查询所有已通过的帖子（正常显示）
    Page<Post> findByStateOrderByCreatedAtDesc(PostState state, Pageable pageable);
    
    // 查询用户的所有帖子（按状态）
    Page<Post> findByAuthorIdAndStateOrderByCreatedAtDesc(Long authorId, PostState state, Pageable pageable);
    
    // 查询用户的所有帖子（按状态且未软删除）
    Page<Post> findByAuthorIdAndStateAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId, PostState state, Pageable pageable);
    
    // 查询所有已通过且未软删除的帖子（用于正常浏览）
    Page<Post> findByStateAndIsDeletedFalseOrderByCreatedAtDesc(PostState state, Pageable pageable);
    
    // 查询待审核的帖子（管理员功能）
    @Query("SELECT p FROM Post p WHERE p.state = 'PENDING' AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPendingAuditPosts(Pageable pageable);
    
    // 查询需要人工审核的帖子（管理员功能）
    @Query("SELECT p FROM Post p WHERE p.state = 'WAITING' AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findNeedAdminCheckPosts(Pageable pageable);
    
    // 查询已通过的帖子（正常显示）- 公告帖子置顶
    @Query("SELECT p FROM Post p WHERE p.state = 'VALID' AND p.isDeleted = false ORDER BY p.isAnnouncement DESC, p.createdAt DESC")
    Page<Post> findValidPosts(Pageable pageable);
    
    // 查询已拒绝的帖子（管理员查看）
    @Query("SELECT p FROM Post p WHERE p.state = 'INVALID' ORDER BY p.createdAt DESC")
    Page<Post> findInvalidPosts(Pageable pageable);
    
    // 查询已删除的帖子（管理员查看）
    @Query("SELECT p FROM Post p WHERE p.isDeleted = true ORDER BY p.deletedAt DESC")
    Page<Post> findDeletedPosts(Pageable pageable);
    
    // 更新点赞数
    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + :increment WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);
    
    // 更新评论数
    @Modifying
    @Query("UPDATE Post p SET p.commentsCount = p.commentsCount + :increment WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);

    Integer countByIsDeletedFalseAndReportCountGreaterThan(Integer minReports);
    
    // ================== 区分删除原因的查询方法 ==================
    
    // 查询被举报删除的帖子（reportCount >0且已删除）
    @Query("SELECT p FROM Post p WHERE p.isDeleted = true AND p.reportCount > 0 ORDER BY p.deletedAt DESC")
    Page<Post> findReportedDeletedPosts(Pageable pageable);
    
    // 查询被审核删除的帖子（reportCount =0且已删除）
    @Query("SELECT p FROM Post p WHERE p.isDeleted = true AND p.reportCount = 0 ORDER BY p.deletedAt DESC")
    Page<Post> findAuditDeletedPosts(Pageable pageable);
    
    // 查询被举报删除的帖子数量
    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = true AND p.reportCount >0")
    int countReportedDeletedPosts();
    
    // 查询被审核删除的帖子数量
    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = true AND p.reportCount =0")
    int countAuditDeletedPosts();
}