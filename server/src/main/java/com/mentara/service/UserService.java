package com.mentara.service;

import com.mentara.dto.response.UserProfileResponse;
import com.mentara.dto.ExpertUserDTO;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface UserService {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByUsernameIncludingDeleted(String username);
    
    boolean existsByEmail(String email);
    
    User updateUser(User user);
    
    void deleteUser(Long id);
    
    User updateLastLoginTime(String username);
    
    // 新增认证相关方法
    Optional<User> findByStudentId(String studentId);
    
    boolean existsByStudentId(String studentId);
    
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    User createUser(String studentId, String email, String password, String nickname);
    
    User resetUserPassword(String studentId, String email, String newPassword);
    
    // 获取用户个人中心统计信息
    UserProfileResponse getUserProfileStats(Long userId);
    
    // 管理员功能：禁用用户
    void disableUser(Long userId);
    
    // 管理员功能：启用用户
    void enableUser(Long userId);
    
    // 增加用户被举报次数
    void incrementReportedCount(Long userId);
    
    // 减少用户被举报次数
    void decrementReportedCount(Long userId, int count);
    
    // 获取用户总数（未删除的）
    long countUsers();
    
    // 搜索用户（支持分页，只搜索未删除的用户）
    org.springframework.data.domain.Page<User> searchUsers(String keyword, org.springframework.data.domain.Pageable pageable);
    
    // 获取活跃用户数（最近30天有活动）
    long getActiveUsersCount();
    
    // 获取本月新增用户数
    long getNewUsersThisMonth();
    
    // 批量查询用户信息
    List<User> findByIds(Set<Long> userIds);
    
    // 软删除用户（保留评论和点赞数据）
    void softDeleteUser(Long userId);
    
    // 恢复已删除的用户
    void restoreUser(Long userId);
    
    // 检查用户名是否可用（包括已删除用户）
    boolean isUsernameAvailable(String username);
    
    // 恢复已删除用户（如果用户名冲突则抛出异常）
    void restoreUserWithUsernameCheck(Long userId);
    
    // 更新用户角色
    void updateUserRole(Long userId, String role);
    
    // 获取所有专家用户（包含专家详细信息）
    List<ExpertUserDTO> getAllExpertUsers();
}
    