package com.mentara.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostReplyNotificationResponse extends BaseNotificationResponse {
    private Long replyUserId;
    private String replyUserNickname;
    private Long postId;
    private String postTitle;
    private Long replyId;
    private String replyContent;
} 