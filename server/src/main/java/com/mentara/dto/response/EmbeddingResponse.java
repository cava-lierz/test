package com.mentara.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding服务的响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    
    /**
     * embedding向量数组
     */
    @JsonProperty("embedding")
    private List<Float> embedding;
    
    /**
     * 响应状态码
     */
    @JsonProperty("dimension")
    private Integer dimension;
} 