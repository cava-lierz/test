package com.mentara.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    /**
     * DeepSeek API密钥
     */
    private String apiKey;
    
    /**
     * DeepSeek API基础URL
     */
    private String baseUrl = "https://api.deepseek.com/v1";
    
    /**
     * 默认模型名称
     */
    private String defaultModel = "deepseek-chat";
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
} 