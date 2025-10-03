package com.mentara.service;

import java.util.List;

public interface UserFollowService {
    
    /**
     * 关注用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void followUser(Long followerId, Long followingId);
    
    /**
     * 取消关注用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     */
    void unfollowUser(Long followerId, Long followingId);
    
    /**
     * 检查用户是否已关注另一个用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isUserFollowing(Long followerId, Long followingId);
    
    /**
     * 获取用户关注的所有用户ID列表
     * @param followerId 关注者ID
     * @return 被关注用户ID列表
     */
    List<Long> getFollowingUserIds(Long followerId);
    
    /**
     * 获取关注用户的所有用户ID列表
     * @param followingId 被关注者ID
     * @return 关注者用户ID列表
     */
    List<Long> getFollowerUserIds(Long followingId);
    
    /**
     * 统计用户关注的人数
     * @param followerId 关注者ID
     * @return 关注人数
     */
    Long countFollowing(Long followerId);
    
    /**
     * 统计关注用户的人数
     * @param followingId 被关注者ID
     * @return 粉丝数
     */
    Long countFollowers(Long followingId);
} 