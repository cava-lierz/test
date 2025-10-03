package com.mentara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportPostRequest {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    private String reason; // 举报原因（可选）
} 