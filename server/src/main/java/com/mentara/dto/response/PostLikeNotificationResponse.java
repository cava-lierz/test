package com.mentara.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostLikeNotificationResponse extends BaseNotificationResponse {
    private Long lastLikerId;
    private String lastLikerNickname;
    private Integer likerCount;
    private Long postId;
    private String postTitle;

} 