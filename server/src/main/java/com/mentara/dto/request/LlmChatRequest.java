package com.mentara.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * LLM聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmChatRequest {
    
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
} 