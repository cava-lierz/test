 package com.mentara.dto.response;

import com.mentara.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String gender;
    private Integer age;
    private String bio;
    private String role;
    private String email;              // 邮箱信息
    private Boolean isDisabled;        // 是否被禁用
    
    // 统计数据
    private Integer postsCount;        // 发布动态数量
    private Integer totalLikes;        // 获得点赞数量
    private Integer joinDays;          // 加入天数
    private Integer commentsCount;     // 评论数量
    private Double averageMoodRating;  // 平均心情评分
    private Integer reportedPostsCount; // 被举报次数（持久化存储）
    
    // 时间信息
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    public static UserProfileResponse fromUser(User user, Integer postsCount, Integer totalLikes, 
                                             Integer commentsCount, Double averageMoodRating) {
        return fromUser(user, postsCount, totalLikes, commentsCount, averageMoodRating, 0);
    }
    
    public static UserProfileResponse fromUser(User user, Integer postsCount, Integer totalLikes, 
                                             Integer commentsCount, Double averageMoodRating, Integer reportedPostsCount) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setGender(user.getGender());
        response.setAge(user.getAge());
        response.setBio(user.getBio());
        response.setRole(user.getRole().name());
        response.setEmail(user.getUserAuth() != null ? user.getUserAuth().getEmail() : null);
        response.setIsDisabled(user.getIsDisabled() != null ? user.getIsDisabled() : false);
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        
        // 统计数据
        response.setPostsCount(postsCount != null ? postsCount : 0);
        response.setTotalLikes(totalLikes != null ? totalLikes : 0);
        response.setCommentsCount(commentsCount != null ? commentsCount : 0);
        response.setAverageMoodRating(averageMoodRating != null ? averageMoodRating : 0.0);
        response.setReportedPostsCount(reportedPostsCount != null ? reportedPostsCount : 0);
        
        // 计算加入天数
        if (user.getCreatedAt() != null) {
            long days = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
            response.setJoinDays((int) days);
        } else {
            response.setJoinDays(0);
        }
        
        return response;
    }
}