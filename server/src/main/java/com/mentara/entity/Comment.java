package com.mentara.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_post_parent_created", columnList = "post_id, parent_id, created_at ASC"),
    @Index(name = "idx_comments_top_comment_created", columnList = "top_comment_id, created_at ASC"),
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "评论内容不能为空")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;
    


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_comment_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Comment topComment;

    @Column(name = "replys_count")
    private int replysCount = 0;

    // 移除关联关系，避免循环查询
    // 回复关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // @JsonIgnore
    // private Set<Comment> replies = new HashSet<>();

    @Column(name = "likes_count")
    private int likesCount = 0;

    // 移除关联关系，避免循环查询
    // 点赞关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "comment", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<CommentLike> likes;

    // 移除关联关系，避免循环查询
    // 通知关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "comment", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<CommentLikeNotification> commentLikeNotifications;

    // @OneToMany(mappedBy = "comment", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<CommentReplyNotification> commentReplyNotifications;

    // @OneToMany(mappedBy = "reply", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<PostReplyNotification> asReplyPostNotifications;

    // @OneToMany(mappedBy = "reply", orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<CommentReplyNotification> asReplyCommentNotifications;

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

    // 移除关联关系，避免循环查询
    // 举报关系通过Repository查询，而不是实体关联
    // @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<CommentReport> reports;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}