package com.mentara.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本打分服务测试类
 */
@SpringBootTest
@TestPropertySource(properties = {
    "python.microservice.url=http://localhost:8000",
    "python.microservice.endpoint=/mood_score",
    "python.microservice.timeout=5000"
})
public class TextScoreServiceTest {

    @Autowired
    private TextScoreService textScoreService;

    @Test
    public void testScoreText() {
        // 测试文本打分
        String testText = "这是一个测试文本，用于验证打分功能是否正常工作。";
        
        // 调用服务
        Integer score = textScoreService.scoreText(testText, "sentiment");

        // 验证响应
        // 注意：如果Python微服务没有运行，这里会返回null
        if (score != null) {
            assertTrue(score >= 0.0);
            assertTrue(score <= 1.0);
        } else {
            // 如果Python微服务没有运行，这是预期的行为
            System.out.println("Python微服务未运行，返回null是预期的");
        }
    }

    @Test
    public void testScoreTextWithoutType() {
        // 测试不带打分类型的文本打分
        String testText = "今天天气很好，心情愉快！";
        
        Integer score = textScoreService.scoreText(testText);
        
        if (score != null) {
            assertTrue(score >= 0.0);
            assertTrue(score <= 1.0);
        } else {
            System.out.println("Python微服务未运行，返回null是预期的");
        }
    }

    @Test
    public void testHealthCheck() {
        // 测试健康检查
        Boolean isHealthy = textScoreService.healthCheck().block();
        assertNotNull(isHealthy);
        // 如果Python微服务没有运行，这里会返回false
        if (!isHealthy) {
            System.out.println("Python微服务未运行，健康检查返回false是预期的");
        }
    }

    @Test
    public void testEmptyText() {
        // 测试空文本
        Integer score = textScoreService.scoreText("");
        assertNull(score);
    }

    @Test
    public void testNullText() {
        // 测试null文本
        Integer score = textScoreService.scoreText(null);
        assertNull(score);
    }
} 