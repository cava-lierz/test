package com.mentara.controller;

import com.mentara.dto.request.DeepSeekRequest;
import com.mentara.dto.response.DeepSeekResponse;
import com.mentara.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DeepSeek API测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/deepseek")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeepSeekTestController {
    
    private final DeepSeekService deepSeekService;
    
    /**
     * 测试DeepSeek API连接
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("开始测试DeepSeek API连接...");
            
            // 构建测试请求
            DeepSeekRequest request = DeepSeekRequest.builder()
                    .model("deepseek-chat")
                    .messages(Collections.singletonList(
                            DeepSeekRequest.Message.builder()
                                    .role("user")
                                    .content("你好，请简单回复'连接成功'")
                                    .build()
                    ))
                    .temperature(0.7)
                    .maxTokens(50)
                    .stream(false)
                    .build();
            
            // 发送请求
            DeepSeekResponse deepSeekResponse = deepSeekService.sendChatRequest(request);
            
            if (deepSeekResponse != null && deepSeekResponse.getChoices() != null && !deepSeekResponse.getChoices().isEmpty()) {
                String content = deepSeekResponse.getChoices().get(0).getMessage().getContent();
                
                response.put("success", true);
                response.put("message", "DeepSeek API连接成功");
                response.put("response", content);
                response.put("model", deepSeekResponse.getModel());
                response.put("usage", deepSeekResponse.getUsage());
                
                log.info("DeepSeek API连接测试成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "DeepSeek API响应格式异常");
                log.warn("DeepSeek API响应格式异常");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("DeepSeek API连接测试失败", e);
            
            response.put("success", false);
            response.put("message", "DeepSeek API连接失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 简单聊天测试
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> simpleChat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = request.get("message");
            String model = request.getOrDefault("model", "deepseek-chat");
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("发送聊天请求: {}", message);
            
            // 使用简化的方法
            String reply = deepSeekService.generateResponse(message, model);
            
            response.put("success", true);
            response.put("message", "聊天成功");
            response.put("reply", reply);
            response.put("model", model);
            
            log.info("聊天请求处理成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("聊天请求处理失败", e);
            
            response.put("success", false);
            response.put("message", "聊天请求失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 获取API状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("service", "DeepSeek API");
        response.put("status", "available");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
} 