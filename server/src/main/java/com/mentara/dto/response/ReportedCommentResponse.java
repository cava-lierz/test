package com.mentara.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportedCommentResponse {
    private Long id;
    private String content;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private String authorName;
    private Long authorId;
    private Long postId;
    private String postTitle;
    private Long parentId;
    private Long topCommentId;
    private Integer repliesCount;
    private Integer likesCount;
} 