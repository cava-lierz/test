package com.mentara.service.impl;

import com.mentara.entity.UserFollow;
import com.mentara.entity.User;
import com.mentara.repository.UserFollowRepository;
import com.mentara.service.UserFollowService;
import com.mentara.service.UserService;
import com.mentara.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        // 验证用户存在
        User follower = userService.findById(followerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));
        User following = userService.findById(followingId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", followingId));

        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 检查是否已经关注
        if (userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("已经关注该用户");
        }

        // 创建关注记录
        UserFollow userFollow = new UserFollow(follower, following);
        userFollowRepository.save(userFollow);
    }

    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        // 验证用户存在
        User follower = userService.findById(followerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));
        User following = userService.findById(followingId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", followingId));

        // 检查是否已经关注
        if (!userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("未关注该用户");
        }

        // 删除关注记录
        userFollowRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Override
    public boolean isUserFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        
        // 使用缓存避免重复查询
        User follower = getUserFromCache(followerId);
        User following = getUserFromCache(followingId);
        
        if (follower == null || following == null) {
            return false;
        }
        
        return userFollowRepository.existsByFollowerAndFollowing(follower, following);
    }

    // 简单的内存缓存
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();
    
    private User getUserFromCache(Long userId) {
        return userCache.computeIfAbsent(userId, id -> 
            userService.findById(id).orElse(null)
        );
    }

    @Override
    public List<Long> getFollowingUserIds(Long followerId) {
        return userFollowRepository.findFollowingUserIdsByFollowerId(followerId);
    }

    @Override
    public List<Long> getFollowerUserIds(Long followingId) {
        return userFollowRepository.findFollowerUserIdsByFollowingId(followingId);
    }

    @Override
    public Long countFollowing(Long followerId) {
        return userFollowRepository.countByFollowerId(followerId);
    }

    @Override
    public Long countFollowers(Long followingId) {
        return userFollowRepository.countByFollowingId(followingId);
    }
} 