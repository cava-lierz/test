package com.mentara.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 文本打分服务
 * 调用Python微服务进行文本打分
 */
@Slf4j
@Service
public class TextScoreService {

    private final WebClient webClient;
    
    @Value("${python.microservice.url:http://localhost:8000}")
    private String pythonServiceUrl;
    
    @Value("${python.microservice.endpoint:/mood_score}")
    private String scoreEndpoint;
    
    @Value("${python.microservice.timeout:10000}")
    private int timeout;

    @Autowired
    public TextScoreService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 对文本进行打分
     * 
     * @param text 需要打分的文本
     * @param scoreType 打分类型（可选）
     * @return 打分结果，如果失败返回null
     */
    public Integer scoreText(String text, String scoreType) {
        try {
            log.info("开始对文本进行打分，文本长度: {}", text.length());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            if (scoreType != null) {
                requestBody.put("score_type", scoreType);
            }
            
            String fullUrl = pythonServiceUrl + scoreEndpoint;
            
            Map<String, Object> response = webClient.post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofMillis(timeout))
                    .block();
            
            if (response != null && response.containsKey("label")) {
                    Integer score = (Integer) response.get("label");
                log.info("文本打分成功，得分: {}", score);
                return score;
            } else {
                log.error("Python微服务返回无效响应: {}", response);
                return null;
            }
            
        } catch (WebClientResponseException e) {
            log.error("调用Python微服务失败，状态码: {}, 错误信息: {}", e.getStatusCode(), e.getMessage());
            return null;
            
        } catch (Exception e) {
            log.error("文本打分过程中发生异常", e);
            return null;
        }
    }

    /**
     * 对文本进行打分（简化版本，不指定打分类型）
     * 
     * @param text 需要打分的文本
     * @return 打分结果，如果失败返回null
     */
    public Integer scoreText(String text) {
        return scoreText(text, null);
    }

    /**
     * 健康检查 - 检查Python微服务是否可用
     */
    public Mono<Boolean> healthCheck() {
        String healthUrl = pythonServiceUrl + "/health";
        return webClient.get()
                .uri(healthUrl)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorReturn(false);
    }
} 