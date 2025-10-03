package com.mentara.controller;

import com.mentara.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/basic")
@CrossOrigin(origins = "*")
public class BasicTestController {

    /**
     * 基本健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        try {
            log.info("基本健康检查");
            return ApiResponse.success("服务正常运行", "健康检查通过");
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return ApiResponse.error("500", "健康检查失败: " + e.getMessage());
        }
    }

    /**
     * 测试基本JSON响应
     */
    @GetMapping("/json")
    public ApiResponse<Map<String, Object>> testJson() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Hello World");
            data.put("timestamp", System.currentTimeMillis());
            data.put("status", "success");
            
            return ApiResponse.success("JSON测试成功", data);
        } catch (Exception e) {
            log.error("JSON测试失败", e);
            return ApiResponse.error("500", "JSON测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试POST请求
     */
    @PostMapping("/post")
    public ApiResponse<Map<String, Object>> testPost(@RequestBody Map<String, Object> request) {
        try {
            log.info("收到POST请求: {}", request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("received", request);
            response.put("timestamp", System.currentTimeMillis());
            response.put("processed", true);
            
            return ApiResponse.success("POST测试成功", response);
        } catch (Exception e) {
            log.error("POST测试失败", e);
            return ApiResponse.error("500", "POST测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试异常处理
     */
    @GetMapping("/error")
    public ApiResponse<String> testError() {
        try {
            // 故意抛出一个异常来测试错误处理
            throw new RuntimeException("这是一个测试异常");
        } catch (Exception e) {
            log.error("测试异常处理", e);
            return ApiResponse.error("500", "测试异常: " + e.getMessage());
        }
    }
} 