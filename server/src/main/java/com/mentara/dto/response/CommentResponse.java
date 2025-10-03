package com.mentara.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorNickname;
    private String authorAvatar;
    private Long authorId;
    private String authorRole;
    private LocalDateTime createdAt;
    private Long postId;
    private Long parentId;
    private Long topCommentId;
    private int repliesCount;
    private int likesCount;
    private int reportCount;
    private boolean isLiked;
    private boolean canDelete;
    
    // 软删除相关字段
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private String deleteReason;
    
    //private List<CommentResponse> replies;
} 