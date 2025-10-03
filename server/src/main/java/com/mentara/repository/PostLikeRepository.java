package com.mentara.repository;

import com.mentara.entity.PostLike;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    Optional<PostLike> findByPostAndUser(Post post, User user);
    
    boolean existsByPostAndUser(Post post, User user);
    
    long countByPost(Post post);
    
    void deleteByPostAndUser(Post post, User user);
    
    // 统计用户获得的总点赞数（用户发布的帖子被点赞的总数）
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author.id = :authorId")
    Integer countByPostAuthorId(@Param("authorId") Long authorId);
    
    // 根据帖子ID删除点赞记录
    void deleteByPostId(Long postId);
    
    // 批量查询用户对多个帖子的点赞状态
    @Query("SELECT pl FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    List<PostLike> findByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}