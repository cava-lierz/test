package com.mentara.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long postId;

    private Long parentId;

    private Long topCommentId;
} 