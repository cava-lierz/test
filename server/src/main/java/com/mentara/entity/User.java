package com.mentara.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 64, message = "用户名长度必须在2-64个字符之间")
    @Column(unique = true, length = 64)
    private String username;


    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String bio; // 个人简介

    // 用户角色：ADMIN（管理员）、USER（普通用户）
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER; // 默认为普通用户
    
    // 用户状态：是否被禁用
    @Column(name = "is_disabled", nullable = false)
    private Boolean isDisabled = false;
    
    // 被举报次数（持久化存储）
    @Column(name = "reported_count", nullable = false)
    private Integer reportedCount = 0;
    
    // 个人信息是否公开：true表示公开，false表示不公开
    @Column(name = "is_profile_public", nullable = false)
    private Boolean isProfilePublic = true; // 默认公开
    
    // 软删除标记：true表示已删除，false表示正常
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    // 删除时间
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private UserAuth userAuth;
    
    // 移除级联关系，因为使用软删除，不需要级联删除
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Post> posts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Checkin> checkins;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;

    // 移除级联删除，使用软删除机制
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<CommentLike> commentLikes;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostLike> postLikes;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Report> reports;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "LastLiker", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostLikeNotification> sentPostLikeNotifications;

    @OneToMany(mappedBy = "lastLiker", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommentLikeNotification> sentCommentLikeNotifications;

    @OneToMany(mappedBy = "replyUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostReplyNotification> sentPostReplyNotifications;

    @OneToMany(mappedBy = "replyUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommentReplyNotification> sentCommentReplyNotifications;

    // Time related
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // 检查是否为管理员
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    // 检查是否为普通用户
    public boolean isUser() {
        return UserRole.USER.equals(this.role);
    }

    // 检查是否为专家
    public boolean isExpert() {
        return UserRole.EXPERT.equals(this.role);
    }
}