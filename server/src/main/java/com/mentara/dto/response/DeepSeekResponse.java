package com.mentara.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek API响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekResponse {
    
    /**
     * 响应ID
     */
    private String id;
    
    /**
     * 对象类型
     */
    private String object;
    
    /**
     * 创建时间戳
     */
    private Long created;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 选择列表
     */
    private List<Choice> choices;
    
    /**
     * 使用情况
     */
    private Usage usage;
    
    /**
     * 选择对象
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;
        
        /**
         * 消息
         */
        private Message message;
        
        /**
         * 完成原因
         */
        private String finishReason;
    }
    
    /**
     * 消息对象
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        /**
         * 角色
         */
        private String role;
        
        /**
         * 内容
         */
        private String content;
    }
    
    /**
     * 使用情况
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        /**
         * 提示token数
         */
        private Integer promptTokens;
        
        /**
         * 完成token数
         */
        private Integer completionTokens;
        
        /**
         * 总token数
         */
        private Integer totalTokens;
    }
} 