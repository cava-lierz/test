package com.mentara.service;

import com.mentara.dto.response.DepressCheckResponse;
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
 * 抑郁检查服务
 * 调用Python微服务进行抑郁倾向检查
 */
@Slf4j
@Service
public class DepressCheckService {

    private final WebClient webClient;
    
    @Value("${python.microservice.url:http://localhost:8000}")
    private String pythonServiceUrl;
    
    @Value("${python.microservice.depress.endpoint:/depress_check}")
    private String depressEndpoint;
    
    @Value("${python.microservice.timeout:10000}")
    private int timeout;

    @Autowired
    public DepressCheckService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 检查文本的抑郁倾向
     * 
     * @param text 需要检查的文本
     * @param checkType 检查类型（可选）
     * @return 抑郁检查结果，包含label和score，如果失败返回null
     */
    public DepressCheckResponse checkDepress(String text, String checkType) {
        try {
            log.info("开始检查抑郁倾向，文本长度: {}", text.length());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            if (checkType != null) {
                requestBody.put("check_type", checkType);
            }
            
            String fullUrl = pythonServiceUrl + depressEndpoint;
            
            Map<String, Object> response = webClient.post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofMillis(timeout))
                    .block();
            
            if (response != null && response.containsKey("score") && response.containsKey("label")) {
                Double score = (Double) response.get("score");
                Integer label = (Integer) response.get("label");
                log.info("抑郁倾向检查成功，得分: {}, 标签: {}", score, label);
                return new DepressCheckResponse(label, score);
            } else {
                log.error("Python微服务返回无效响应: {}", response);
                return null;
            }
            
        } catch (WebClientResponseException e) {
            log.error("调用Python微服务失败，状态码: {}, 错误信息: {}", e.getStatusCode(), e.getMessage());
            return null;
            
        } catch (Exception e) {
            log.error("抑郁倾向检查过程中发生异常", e);
            return null;
        }
    }

    /**
     * 检查文本的抑郁倾向（简化版本，不指定检查类型）
     * 
     * @param text 需要检查的文本
     * @return 抑郁检查结果，包含label和score，如果失败返回null
     */
    public DepressCheckResponse checkDepress(String text) {
        return checkDepress(text, null);
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