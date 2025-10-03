package com.mentara.service.impl;

import com.mentara.entity.UserBlock;
import com.mentara.entity.User;
import com.mentara.repository.UserBlockRepository;
import com.mentara.service.UserBlockService;
import com.mentara.service.UserService;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserBlockServiceImpl implements UserBlockService {

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    @CacheEvict(value = {"userBlocks", "users"}, allEntries = true)
    public void blockUser(Long blockerId, Long blockedId, String reason) {
        // 验证用户存在
        User blocker = userService.findById(blockerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockerId));
        User blocked = userService.findById(blockedId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockedId));

        // 不能拉黑自己
        if (blockerId.equals(blockedId)) {
            throw new RuntimeException("不能拉黑自己");
        }

        // 不能拉黑管理员
        if (blocked.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("不能拉黑管理员");
        }

        // 检查是否已经拉黑
        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new RuntimeException("已经拉黑该用户");
        }

        // 创建拉黑记录
        UserBlock userBlock = new UserBlock(blocker, blocked, reason);
        userBlockRepository.save(userBlock);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userBlocks", "users"}, allEntries = true)
    public void unblockUser(Long blockerId, Long blockedId) {
        // 验证用户存在
        User blocker = userService.findById(blockerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockerId));
        User blocked = userService.findById(blockedId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockedId));

        // 检查是否已经拉黑
        if (!userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new RuntimeException("未拉黑该用户");
        }

        // 删除拉黑记录
        userBlockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }

    @Override
    @Cacheable(value = "userBlocks", key = "#blockerId + '_' + #blockedId")
    public boolean isUserBlocked(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) {
            return false;
        }
        
        // 使用缓存避免重复查询
        User blocker = getUserFromCache(blockerId);
        User blocked = getUserFromCache(blockedId);
        
        if (blocker == null || blocked == null) {
            return false;
        }
        
        return userBlockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    // 使用Spring Boot缓存替代简单内存缓存
    @Cacheable(value = "users", key = "#userId")
    private User getUserFromCache(Long userId) {
        return userService.findById(userId).orElse(null);
    }

    @Override
    @Cacheable(value = "userBlocks", key = "'blocked_' + #blockerId")
    public List<Long> getBlockedUserIds(Long blockerId) {
        return userBlockRepository.findBlockedUserIdsByBlockerId(blockerId);
    }

    @Override
    @Cacheable(value = "userBlocks", key = "'blocker_' + #blockedId")
    public List<Long> getBlockerUserIds(Long blockedId) {
        return userBlockRepository.findBlockerUserIdsByBlockedId(blockedId);
    }

    @Override
    public List<Long> filterBlockedUsers(Long userId, List<Long> authorIds) {
        if (userId == null || authorIds == null || authorIds.isEmpty()) {
            return authorIds;
        }
        
        List<Long> blockedUserIds = getBlockedUserIds(userId);
        if (blockedUserIds.isEmpty()) {
            return authorIds;
        }
        
        return authorIds.stream()
            .filter(authorId -> !blockedUserIds.contains(authorId))
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "userBlocks", key = "'count_blocked_' + #blockerId")
    public Long countBlockedUsers(Long blockerId) {
        return userBlockRepository.countByBlockerId(blockerId);
    }

    @Override
    @Cacheable(value = "userBlocks", key = "'count_blocker_' + #blockedId")
    public Long countBlockers(Long blockedId) {
        return userBlockRepository.countByBlockedId(blockedId);
    }
} 