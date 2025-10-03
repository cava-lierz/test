package com.mentara.dto.response;

import com.mentara.document.LlmSessionDocument;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * LLM会话响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmSessionResponse {
    
    private String id;
    private Long userId;
    private String model;
    private Boolean isSensitive;
    private String sensitiveType;
    private LocalDateTime createdAt;
    
    /**
     * 从LlmSessionDocument转换为LlmSessionResponse
     */
    public static LlmSessionResponse fromDocument(LlmSessionDocument document) {
        return LlmSessionResponse.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .model(document.getModel())
                .isSensitive(document.getIsSensitive())
                .sensitiveType(document.getSensitiveType())
                .createdAt(document.getCreatedAt())
                .build();
    }
} 