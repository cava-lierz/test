package com.mentara.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentLikeNotificationResponse extends BaseNotificationResponse {
    private Long lastLikerId;
    private String lastLikerNickname;
    private Integer likerCount;
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentContent;
} 