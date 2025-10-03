package com.mentara.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mentara.enums.MoodType;
import com.mentara.enums.PostState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_posts_author_created", columnList = "author_id, created_at DESC"),
    @Index(name = "idx_posts_state_deleted_created", columnList = "state, is_deleted, created_at DESC"),
    @Index(name = "idx_posts_state_deleted_likes", columnList = "state, is_deleted, likes_count DESC"),
    @Index(name = "idx_posts_author_state_deleted", columnList = "author_id, state, is_deleted, created_at DESC"),
})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = true)
    private MoodType mood; // 心情枚举，公告帖子可为空

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private PostState state = PostState.PENDING;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_tags", 
        joinColumns = @JoinColumn(name = "post_id"), 
        inverseJoinColumns = @JoinColumn(name = "tag_id"),
        indexes = {
            @Index(name = "idx_post_tags_post_id", columnList = "post_id"),
            @Index(name = "idx_post_tags_tag_id", columnList = "tag_id"),
        }
    )
    @OrderBy("id ASC")
    @JsonIgnore
    private List<PostTag> tags;


    @ElementCollection
    @CollectionTable(
        name = "post_images",
        joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "image_order")
    private List<String> imageUrls;

    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy; // 删除操作的用户ID

    @Column(name = "delete_reason")
    private String deleteReason; // 删除原因

    @Column(name = "is_announcement", nullable = false)
    private Boolean isAnnouncement = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    // 移除关联关系，避免循环查询
    // 评论关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<Comment> comments;

    // 移除关联关系，避免循环查询
    // 点赞关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PostLike> likes;

    // 关联的心情评分记录
    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MoodScore moodScore;

    // 移除关联关系，避免循环查询
    // 举报关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "post", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<Report> reports;

    // 移除关联关系，避免循环查询
    // 通知关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "post", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PostLikeNotification> postLikeNotifications;

    // @OneToMany(mappedBy = "post", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PostReplyNotification> postReplyNotifications;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}