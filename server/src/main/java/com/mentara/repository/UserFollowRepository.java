package com.mentara.repository;

import com.mentara.entity.UserFollow;
import com.mentara.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {
    
    /**
     * 检查用户是否已关注另一个用户
     */
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    /**
     * 根据关注者和被关注者查找关注记录
     */
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);
    
    /**
     * 获取用户关注的所有用户ID列表
     */
    @Query("SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :followerId")
    List<Long> findFollowingUserIdsByFollowerId(@Param("followerId") Long followerId);
    
    /**
     * 获取关注用户的所有用户ID列表
     */
    @Query("SELECT uf.follower.id FROM UserFollow uf WHERE uf.following.id = :followingId")
    List<Long> findFollowerUserIdsByFollowingId(@Param("followingId") Long followingId);
    
    /**
     * 删除关注记录
     */
    void deleteByFollowerAndFollowing(User follower, User following);
    
    /**
     * 统计用户关注的人数
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :followerId")
    Long countByFollowerId(@Param("followerId") Long followerId);
    
    /**
     * 统计关注用户的人数
     */
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :followingId")
    Long countByFollowingId(@Param("followingId") Long followingId);
} 