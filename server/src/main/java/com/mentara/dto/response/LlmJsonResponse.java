package com.mentara.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * LLM JSON响应DTO
 * 用于解析LLM返回的JSON格式响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmJsonResponse {
    
    /**
     * 回复内容
     */
    private String content;
    
    /**
     * 是否敏感
     */
    private Boolean sensitive;
} 