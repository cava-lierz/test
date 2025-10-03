package com.mentara.service.impl;

import com.mentara.dto.response.UserProfileResponse;
import com.mentara.dto.ExpertUserDTO;
import com.mentara.entity.User;
import com.mentara.entity.Expert;
import com.mentara.entity.UserAuth;
import com.mentara.entity.UserRole;
import com.mentara.repository.UserRepository;
import com.mentara.repository.ExpertRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.PostLikeRepository;
import com.mentara.repository.CommentRepository;
import com.mentara.repository.CheckinRepository;
import com.mentara.repository.NotificationRepository;
import com.mentara.repository.CommentLikeRepository;
import com.mentara.repository.AppointmentRepository;
import com.mentara.service.UserService;
import com.mentara.util.CryptoUtils;
import com.mentara.util.AvatarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private CryptoUtils cryptoUtils;

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'username_' + #username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return userRepository.existsByUsernameIncludingDeleted(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        // 软删除：不删除用户数据，只标记为已删除
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 标记用户为已删除
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        
        // 保存用户
        userRepository.save(user);
    }

    @Override
    public User updateLastLoginTime(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public Optional<User> findByStudentId(String studentId) {
        // 优化：直接使用哈希后的学号查询，避免全表扫描
        String hashedStudentId = CryptoUtils.hashStudentId(studentId);
        return userRepository.findByUsername(hashedStudentId);
    }

    @Override
    public boolean existsByStudentId(String studentId) {
        return findByStudentId(studentId).isPresent();
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public User createUser(String studentId, String email, String password, String nickname) {
        String hashedStudentId = CryptoUtils.hashStudentId(studentId);
        
        User user = new User();
        user.setUsername(hashedStudentId);
        if (nickname != null) {
            user.setNickname(nickname);
        }

        // 根据学号生成随机默认头像 (0-29)
        user.setAvatar(AvatarUtils.generateRandomDefaultAvatar(studentId));
         
        UserAuth userAuth = new UserAuth();
        userAuth.setEmail(email);
        userAuth.setPassword(passwordEncoder.encode(password));
        
        userAuth.setUser(user);
        user.setUserAuth(userAuth);
        
        return userRepository.save(user);
    }

    @Override
    public User resetUserPassword(String studentId, String email, String newPassword) {
        Optional<User> userOpt = findByStudentId(studentId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (email.equals(user.getUserAuth().getEmail())) {
                user.getUserAuth().setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            }
        }
        return null;
    }
    
    @Override
    @Cacheable(value = "users", key = "'profile_' + #userId")
    public UserProfileResponse getUserProfileStats(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 获取用户发布的帖子数量
        Integer postsCount = postRepository.countByAuthorId(userId);
        
        // 获取用户获得的总点赞数（用户发布的帖子被点赞的总数）
        Integer totalLikes = postLikeRepository.countByPostAuthorId(userId);
        
        // 获取用户评论数量
        Integer commentsCount = commentRepository.countByAuthorId(userId);
        
        // 获取用户平均心情评分（从打卡记录中计算）
        Double averageMoodRating = checkinRepository.findAverageRatingByUserId(userId);
        
        return UserProfileResponse.fromUser(user, postsCount, totalLikes, commentsCount, averageMoodRating);
    }
    
    @Override
    public void disableUser(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsDisabled(true);
            userRepository.save(user);
        }
    }
    
    @Override
    public void enableUser(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsDisabled(false);
            userRepository.save(user);
        }
    }
    
    @Override
    public void incrementReportedCount(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setReportedCount(user.getReportedCount() + 1);
            userRepository.save(user);
        }
    }
    
    @Override
    public void decrementReportedCount(Long userId, int count) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int newCount = Math.max(0, user.getReportedCount() - count);
            user.setReportedCount(newCount);
            userRepository.save(user);
        }
    }
    
    @Override
    public long countUsers() {
        return userRepository.countActiveUsers();
    }
    
    @Override
    public org.springframework.data.domain.Page<User> searchUsers(String keyword, org.springframework.data.domain.Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }
    
    @Override
    public void softDeleteUser(Long userId) {
        deleteUser(userId); // 使用已有的软删除实现
    }
    
    @Override
    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setIsDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        // 检查是否有未删除的用户使用此用户名
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public List<User> findByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findByIdIn(userIds);
    }
    
    @Override
    public void restoreUserWithUsernameCheck(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查恢复后是否会造成用户名冲突
        if (!user.getIsDeleted()) {
            throw new RuntimeException("用户未被删除，无需恢复");
        }
        
        // 检查是否有其他活跃用户使用相同用户名
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("无法恢复用户：用户名已被其他用户使用");
        }
        
        user.setIsDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);
    }
    
    @Override
    public long getActiveUsersCount() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        
        // 由于lastLoginAt可能为null，我们使用createdAt作为fallback
        return userRepository.findAll().stream()
            .filter(user -> {
                LocalDateTime lastActivity = user.getLastLoginAt() != null ? user.getLastLoginAt() : user.getCreatedAt();
                return lastActivity != null && lastActivity.isAfter(thirtyDaysAgo);
            })
            .count();
    }
    
    @Override
    public long getNewUsersThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return userRepository.findAll().stream()
            .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(startOfMonth))
            .count();
    }
    
    @Override
    public void updateUserRole(Long userId, String role) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                user.setRole(userRole);
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的角色类型: " + role);
            }
        } else {
            throw new RuntimeException("用户不存在");
        }
    }

    @Override
    public List<ExpertUserDTO> getAllExpertUsers() {
        List<ExpertUserDTO> expertUsers = new ArrayList<>();
        
        // 获取所有角色为EXPERT的用户
        List<User> expertUserList = userRepository.findByRoleAndIsDeletedFalse(UserRole.EXPERT);
        
        for (User user : expertUserList) {
            ExpertUserDTO dto = new ExpertUserDTO();
            
            // 设置用户基本信息
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            
            // 查找对应的专家详细信息
            Optional<Expert> expertOpt = expertRepository.findByUserId(user.getId());
            if (expertOpt.isPresent()) {
                Expert expert = expertOpt.get();
                dto.setExpertId(expert.getId());
                dto.setExpertName(expert.getName());
                dto.setSpecialty(expert.getSpecialty());
                dto.setContact(expert.getContact());
                dto.setStatus(expert.getStatus());
                dto.setHasExpertDetails(true);
            } else {
                // 如果没有专家详细信息，使用用户基本信息
                dto.setExpertName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                dto.setSpecialty("心理咨询");
                dto.setStatus("online");
                dto.setHasExpertDetails(false);
            }
            
            expertUsers.add(dto);
        }
        
        return expertUsers;
    }
}