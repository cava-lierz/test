package com.mentara.repository;

import com.mentara.entity.UserBlock;
import com.mentara.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    
    /**
     * 检查用户是否已拉黑另一个用户
     */
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    
    /**
     * 根据拉黑者和被拉黑者查找拉黑记录
     */
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);
    
    /**
     * 获取用户拉黑的所有用户ID列表
     */
    @Query("SELECT ub.blocked.id FROM UserBlock ub WHERE ub.blocker.id = :blockerId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
    
    /**
     * 获取被用户拉黑的所有用户ID列表
     */
    @Query("SELECT ub.blocker.id FROM UserBlock ub WHERE ub.blocked.id = :blockedId")
    List<Long> findBlockerUserIdsByBlockedId(@Param("blockedId") Long blockedId);
    
    /**
     * 删除拉黑记录
     */
    void deleteByBlockerAndBlocked(User blocker, User blocked);
    
    /**
     * 统计用户拉黑的人数
     */
    @Query("SELECT COUNT(ub) FROM UserBlock ub WHERE ub.blocker.id = :blockerId")
    Long countByBlockerId(@Param("blockerId") Long blockerId);
    
    /**
     * 统计被用户拉黑的人数
     */
    @Query("SELECT COUNT(ub) FROM UserBlock ub WHERE ub.blocked.id = :blockedId")
    Long countByBlockedId(@Param("blockedId") Long blockedId);
} 