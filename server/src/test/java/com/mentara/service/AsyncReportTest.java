package com.mentara.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 异步举报处理测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class AsyncReportTest {
    
    @Autowired
    private PostService postService;
    
    @Test
    public void testAsyncReportProcessing() {
        System.out.println("=== 开始测试异步举报处理 ===");
        System.out.println("测试线程: " + Thread.currentThread().getName());
        
        // 模拟举报请求
        Long postId = 1L;
        Long userId = 1L;
        String reason = "测试举报原因";
        
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("=== 调用举报方法前 ===");
            // 调用举报方法
            postService.reportPost(postId, userId, reason);
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            System.out.println("=== 举报方法执行完成 ===");
            System.out.println("执行时间: " + executionTime + "ms");
            System.out.println("当前线程: " + Thread.currentThread().getName());
            
            // 验证方法是否快速返回（异步处理）
            if (executionTime < 2000) {
                System.out.println("✓ 异步处理验证成功：方法快速返回");
                System.out.println("✓ 说明AI审核在后台异步执行，没有阻塞主线程");
            } else {
                System.out.println("✗ 异步处理验证失败：方法执行时间过长");
                System.out.println("✗ 说明AI审核可能仍在同步执行");
            }
            
        } catch (Exception e) {
            System.out.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 等待一段时间让异步处理完成
        try {
            System.out.println("=== 等待异步处理完成 ===");
            System.out.println("等待10秒让AI审核在后台完成...");
            Thread.sleep(10000); // 等待10秒
            System.out.println("异步处理等待完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("等待被中断");
        }
        
        System.out.println("=== 测试完成 ===");
        System.out.println("如果看到'AIAudit-'开头的线程名称，说明异步处理成功");
    }
} 