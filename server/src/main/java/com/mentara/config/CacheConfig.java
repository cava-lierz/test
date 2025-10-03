package com.mentara.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置类
 * 使用Spring Boot内置的缓存机制，无需额外安装Redis
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 配置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "users",           // 用户缓存
            "posts",           // 帖子缓存
            "comments",        // 评论缓存
            "notifications",   // 通知缓存
            "userBlocks",      // 用户拉黑缓存
            "userFollows",     // 用户关注缓存
            "tags",            // 标签缓存
            "experts",         // 专家缓存
            "appointments",    // 预约缓存
            "chatRooms",       // 聊天室缓存
            "moodScores",      // 心情评分缓存
            "statistics"       // 统计数据缓存
        ));
        
        return cacheManager;
    }
} 