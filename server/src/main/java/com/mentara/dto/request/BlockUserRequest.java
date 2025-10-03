package com.mentara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlockUserRequest {
    
    @NotNull(message = "被拉黑用户ID不能为空")
    private Long blockedUserId;
    
    private String reason; // 拉黑原因，可选
} 