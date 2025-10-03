package com.mentara.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API请求DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekRequest {
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 消息列表
     */
    private List<Message> messages;
    
    /**
     * 温度参数（0-2）
     */
    private Double temperature;
    
    /**
     * 最大token数
     */
    private Integer maxTokens;
    
    /**
     * 是否流式响应
     */
    private Boolean stream;

    private ResponseFormat responseFormat;
    
    /**
     * 消息对象
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        /**
         * 角色：system, user, assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseFormat {
        private String type = "json_object";
    }
} 