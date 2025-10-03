package com.mentara.service.impl;

import com.mentara.dto.response.AdminStatsResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.entity.User;
import com.mentara.repository.PostRepository;
import com.mentara.repository.UserRepository;
import com.mentara.repository.PostLikeRepository;
import com.mentara.repository.CommentRepository;
import com.mentara.repository.CheckinRepository;
import com.mentara.repository.ReportRepository;
import com.mentara.repository.CommentReportRepository;
import com.mentara.entity.Report;
import com.mentara.entity.CommentReport;
import com.mentara.service.AdminService;
import com.mentara.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private CheckinRepository checkinRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CommentReportRepository commentReportRepository;

    @Autowired
    private UserService userService;

    @Override
    @Cacheable(value = "statistics", key = "'admin_stats'")
    public AdminStatsResponse getAdminStats() {
        // 获取总用户数
        long totalUsers = userService.countUsers();
        
        // 获取活跃用户数（最近30天有登录活动的用户）
        long activeUsers = userService.getActiveUsersCount();
        
        // 获取本月新增用户数
        long newUsersThisMonth = userService.getNewUsersThisMonth();
        
        // 获取总帖子数
        long totalPosts = postRepository.count();
        
        // 获取总评论数
        long totalComments = commentRepository.count();
        
        // 获取总点赞数
        long totalLikes = postLikeRepository.count();
        
        // 计算平均心情评分
        Double avgMoodRating = checkinRepository.findGlobalAverageRating();
        double averageMoodScore = avgMoodRating != null ? avgMoodRating : 3.8; // 默认值3.8
        
        // 获取待处理的举报数量 - 统计需要人工处理的举报
        // 1. 帖子举报：状态为WAITING的举报记录
        int pendingPostReports = reportRepository.countByState(Report.State.WAITING);
        // 2. 评论举报：状态为WAITING的举报记录
        int pendingCommentReports = commentReportRepository.countByState(CommentReport.State.WAITING);
        int pendingReports = pendingPostReports + pendingCommentReports;
        int pendingPosts = 0;   // 帖子审核功能待实现

        return new AdminStatsResponse(
            pendingReports,
            pendingPosts,
            activeUsers,
            newUsersThisMonth,
            totalUsers,
            totalPosts,
            totalComments,
            totalLikes,
            averageMoodScore
        );
    }

    @Override
    public Page<UserProfileResponse> getAllUsersWithStats(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(this::convertToUserProfileResponse);
    }

    @Override
    public Page<UserProfileResponse> searchUsersWithStats(String keyword, Pageable pageable) {
        Page<User> usersPage = userService.searchUsers(keyword, pageable);
        return usersPage.map(this::convertToUserProfileResponse);
    }

    @Override
    @Cacheable(value = "statistics", key = "'user_stats_' + #userId")
    public UserProfileResponse getUserStatsById(Long userId) {
        return userRepository.findById(userId)
            .map(this::convertToUserProfileResponse)
            .orElse(null);
    }

    @Override
    @Cacheable(value = "statistics", key = "'new_users_this_month'")
    public long getNewUsersThisMonth() {
        return userService.getNewUsersThisMonth();
    }

    @Override
    @Cacheable(value = "statistics", key = "'active_users_count'")
    public long getActiveUsersCount() {
        return userService.getActiveUsersCount();
    }
    
    @Override
    @Cacheable(value = "statistics", key = "'total_users_count'")
    public long getTotalUsersCount() {
        return userService.countUsers();
    }

    private UserProfileResponse convertToUserProfileResponse(User user) {
        try {
            // 获取用户发布的帖子数量
            Integer postsCount = postRepository.countByAuthorId(user.getId());
            
            // 获取用户获得的总点赞数（用户发布的帖子被点赞的总数）
            Integer totalLikes = postLikeRepository.countByPostAuthorId(user.getId());
            
            // 获取用户评论数量
            Integer commentsCount = commentRepository.countByAuthorId(user.getId());
            
            // 获取用户平均心情评分（从打卡记录中计算）
            Double averageMoodRating = checkinRepository.findAverageRatingByUserId(user.getId());
            
            // 使用用户表中的被举报次数（持久化存储）
            Integer reportedCount = user.getReportedCount() != null ? user.getReportedCount() : 0;
            
            return UserProfileResponse.fromUser(user, postsCount, totalLikes, commentsCount, averageMoodRating, reportedCount);
        } catch (Exception e) {
            // 返回一个基本的用户信息，避免整个请求失败
            return UserProfileResponse.fromUser(user, 0, 0, 0, 0.0, 0);
        }
    }
} 