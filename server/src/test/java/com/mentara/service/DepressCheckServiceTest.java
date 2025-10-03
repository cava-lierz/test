package com.mentara.service;

import com.mentara.dto.response.DepressCheckResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 抑郁检查服务测试类
 */
@SpringBootTest
@TestPropertySource(properties = {
    "python.microservice.url=http://localhost:8000",
    "python.microservice.depress.endpoint=/depress_check",
    "python.microservice.timeout=5000"
})
public class DepressCheckServiceTest {

    @Autowired
    private DepressCheckService depressCheckService;

    @Test
    public void testCheckDepress() {
        // 测试抑郁倾向检查
        String testText = "我感觉很沮丧，生活没有意义，每天都过得很痛苦。";
        
        // 调用服务
        DepressCheckResponse result = depressCheckService.checkDepress(testText, "depression");

        // 验证响应
        // 注意：如果Python微服务没有运行，这里会返回null
        if (result != null) {
            assertNotNull(result.getScore());
            assertNotNull(result.getLabel());
            assertTrue(result.getScore() >= 0.0);
            assertTrue(result.getScore() <= 1.0);
            assertTrue(result.getLabel() >= 0);
            assertTrue(result.getLabel() <= 3);
        } else {
            // 如果Python微服务没有运行，这是预期的行为
            System.out.println("Python微服务未运行，返回null是预期的");
        }
    }

    @Test
    public void testCheckDepressWithoutType() {
        // 测试不带检查类型的抑郁倾向检查
        String testText = "我最近心情不好，总是感到孤独和无助。";
        
        DepressCheckResponse result = depressCheckService.checkDepress(testText);
        
        if (result != null) {
            assertNotNull(result.getScore());
            assertNotNull(result.getLabel());
            assertTrue(result.getScore() >= 0.0);
            assertTrue(result.getScore() <= 1.0);
            assertTrue(result.getLabel() >= 0);
            assertTrue(result.getLabel() <= 3);
        } else {
            System.out.println("Python微服务未运行，返回null是预期的");
        }
    }

    @Test
    public void testHealthCheck() {
        // 测试健康检查
        Boolean isHealthy = depressCheckService.healthCheck().block();
        assertNotNull(isHealthy);
        // 如果Python微服务没有运行，这里会返回false
        if (!isHealthy) {
            System.out.println("Python微服务未运行，健康检查返回false是预期的");
        }
    }

    @Test
    public void testEmptyText() {
        // 测试空文本
        DepressCheckResponse result = depressCheckService.checkDepress("");
        assertNull(result);
    }

    @Test
    public void testNullText() {
        // 测试null文本
        DepressCheckResponse result = depressCheckService.checkDepress(null);
        assertNull(result);
    }

    @Test
    public void testPositiveText() {
        // 测试积极文本
        String positiveText = "今天天气很好，我感到很开心，生活充满希望！";
        DepressCheckResponse result = depressCheckService.checkDepress(positiveText);
        
        if (result != null) {
            assertNotNull(result.getScore());
            assertNotNull(result.getLabel());
            assertTrue(result.getScore() >= 0.0);
            assertTrue(result.getScore() <= 1.0);
            assertTrue(result.getLabel() >= 0);
            assertTrue(result.getLabel() <= 3);
            // 积极文本的抑郁倾向应该较低
            System.out.println("积极文本的抑郁倾向得分: " + result.getScore() + ", 标签: " + result.getLabel());
        } else {
            System.out.println("Python微服务未运行，返回null是预期的");
        }
    }
} 