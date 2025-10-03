package com.mentara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCommentRequest {
    private String reason; // 举报原因（可选）
} 