package com.mentara.service;

import java.util.List;

public interface UserBlockService {
    
    /**
     * 拉黑用户
     * @param blockerId 拉黑者ID
     * @param blockedId 被拉黑者ID
     * @param reason 拉黑原因
     */
    void blockUser(Long blockerId, Long blockedId, String reason);
    
    /**
     * 取消拉黑用户
     * @param blockerId 拉黑者ID
     * @param blockedId 被拉黑者ID
     */
    void unblockUser(Long blockerId, Long blockedId);
    
    /**
     * 检查用户是否已拉黑另一个用户
     * @param blockerId 拉黑者ID
     * @param blockedId 被拉黑者ID
     * @return 是否已拉黑
     */
    boolean isUserBlocked(Long blockerId, Long blockedId);
    
    /**
     * 获取用户拉黑的所有用户ID列表
     * @param blockerId 拉黑者ID
     * @return 被拉黑用户ID列表
     */
    List<Long> getBlockedUserIds(Long blockerId);
    
    /**
     * 获取被用户拉黑的所有用户ID列表
     * @param blockedId 被拉黑者ID
     * @return 拉黑者用户ID列表
     */
    List<Long> getBlockerUserIds(Long blockedId);
    
    /**
     * 过滤掉被拉黑用户的内容
     * @param userId 当前用户ID
     * @param authorIds 作者ID列表
     * @return 过滤后的作者ID列表
     */
    List<Long> filterBlockedUsers(Long userId, List<Long> authorIds);
    
    /**
     * 统计用户拉黑的人数
     * @param blockerId 拉黑者ID
     * @return 拉黑人数
     */
    Long countBlockedUsers(Long blockerId);
    
    /**
     * 统计被用户拉黑的人数
     * @param blockedId 被拉黑者ID
     * @return 被拉黑次数
     */
    Long countBlockers(Long blockedId);
} 