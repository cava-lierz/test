package com.mentara.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步配置
 * 根据性能测试结果优化线程池配置
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);      // 核心线程数 - 从8提升到12
        executor.setMaxPoolSize(24);       // 最大线程数 - 从16提升到24
        executor.setQueueCapacity(200);    // 队列容量 - 从100提升到200
        executor.setThreadNamePrefix("Async-");
        executor.setKeepAliveSeconds(60);  // 线程空闲时间
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
    
    /**
     * AI审核专用线程池
     * 特点：CPU密集型，需要较多线程
     */
    @Bean("aiAuditExecutor")
    public Executor aiAuditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);        // 核心线程数 - 从4提升到6
        executor.setMaxPoolSize(12);        // 最大线程数 - 从8提升到12
        executor.setQueueCapacity(30);      // 队列容量 - 从20提升到30
        executor.setThreadNamePrefix("AIAudit-");
        executor.setKeepAliveSeconds(60);   // 线程空闲时间
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 向量存储专用线程池
     * 特点：IO密集型，需要适中线程
     */
    @Bean("vectorExecutor")
    public Executor vectorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // 核心线程数 - 从2提升到4
        executor.setMaxPoolSize(8);         // 最大线程数 - 从4提升到8
        executor.setQueueCapacity(20);      // 队列容量 - 从10提升到20
        executor.setThreadNamePrefix("Vector-");
        executor.setKeepAliveSeconds(60);   // 线程空闲时间
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 心情评分专用线程池
     * 特点：CPU密集型，需要适中线程
     */
    @Bean("moodScoreExecutor")
    public Executor moodScoreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 核心线程数 - 从3提升到5
        executor.setMaxPoolSize(10);        // 最大线程数 - 从6提升到10
        executor.setQueueCapacity(25);      // 队列容量 - 从15提升到25
        executor.setThreadNamePrefix("MoodScore-");
        executor.setKeepAliveSeconds(60);   // 线程空闲时间
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 通知发送专用线程池
     * 特点：IO密集型，需要较多线程
     */
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);        // 核心线程数 - 从4提升到6
        executor.setMaxPoolSize(15);        // 最大线程数 - 从10提升到15
        executor.setQueueCapacity(80);      // 队列容量 - 从50提升到80
        executor.setThreadNamePrefix("Notification-");
        executor.setKeepAliveSeconds(60);   // 线程空闲时间
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}