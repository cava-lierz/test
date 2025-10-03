package com.mentara.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentara.config.DeepSeekConfig;
import com.mentara.dto.request.DeepSeekRequest;
import com.mentara.dto.response.DeepSeekResponse;
import com.mentara.dto.response.LlmJsonResponse;
import com.mentara.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

/**
 * DeepSeek API服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekServiceImpl implements DeepSeekService {
    
    private final DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    
    @Override
    public DeepSeekResponse sendChatRequest(DeepSeekRequest request) {
        try {
            log.info("发送请求到DeepSeek API，模型: {}, 消息数量: {}", 
                    request.getModel(), request.getMessages().size());
            
            return webClient.post()
                    .uri(deepSeekConfig.getBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekConfig.getApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(DeepSeekResponse.class)
                    .timeout(Duration.ofMillis(deepSeekConfig.getTimeout()))
                    .retry(deepSeekConfig.getMaxRetries())
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("DeepSeek API请求失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("调用DeepSeek API时发生异常", e);
            throw new RuntimeException("调用DeepSeek API时发生异常", e);
        }
    }
    
    @Override
    public String generateResponse(String message, String model) {
        try {
            // 构建请求
            DeepSeekRequest request = DeepSeekRequest.builder()
                    .model(model != null ? model : deepSeekConfig.getDefaultModel())
                    .messages(Collections.singletonList(
                            DeepSeekRequest.Message.builder()
                                    .role("user")
                                    .content(message)
                                    .build()
                    ))
                    .temperature(0.7)
                    .maxTokens(1000)
                    .stream(false)
                    .responseFormat(DeepSeekRequest.ResponseFormat.builder()
                            .type("json_object")
                            .build())
                    .build();
            
            // 发送请求
            DeepSeekResponse response = sendChatRequest(request);
            
            // 提取响应内容
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("DeepSeek API响应成功，内容长度: {}", content.length());
                return content;
            } else {
                log.warn("DeepSeek API响应为空或格式不正确");
                return "{\"content\": \"抱歉，我暂时无法回应您的问题。\", \"sensitive\": false}";
            }
            
        } catch (Exception e) {
            log.error("生成DeepSeek响应时发生异常", e);
            return "{\"content\": \"抱歉，服务暂时不可用，请稍后再试。\", \"sensitive\": false}";
        }
    }
    
    /**
     * 解析JSON格式的LLM响应
     * @param jsonResponse JSON格式的响应字符串
     * @return LlmJsonResponse对象
     */
    public LlmJsonResponse parseJsonResponse(String jsonResponse) {
        try {
            String cleanJson = jsonResponse
                    .replaceFirst("^\\s*```json\\s*", "")  // 去除开头的```json
                    .replaceFirst("\\s*```\\s*$", "");     // 去除结尾的```
            return objectMapper.readValue(cleanJson, LlmJsonResponse.class);
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", e.getMessage());
            // 如果解析失败，返回默认值
            return LlmJsonResponse.builder()
                    .content("抱歉，响应格式解析失败。")
                    .sensitive(false)
                    .build();
        }
    }
} 