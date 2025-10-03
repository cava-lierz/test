package com.mentara.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentReplyNotificationResponse extends BaseNotificationResponse {
    private Long replyUserId;
    private String replyUserNickname;
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentContent;
    private Long replyId;
    private String replyContent;
} 