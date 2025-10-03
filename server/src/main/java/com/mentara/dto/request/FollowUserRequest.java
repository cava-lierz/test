package com.mentara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUserRequest {
    
    @NotNull(message = "被关注用户ID不能为空")
    private Long followedUserId;
} 