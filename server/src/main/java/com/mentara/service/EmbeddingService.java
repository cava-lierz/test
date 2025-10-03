package com.mentara.service;

import com.mentara.dto.response.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    @Autowired
    private WebClient webClient;

    @Value("${embedding.host}")
    private String embeddingHost;

    @Value("${embedding.port}")
    private Integer embeddingPort;

    @Value("${embedding.timeout}")
    private Long embeddingTimeout;

    public List<Float> getEmbedding(String text) {
        try {
            log.info("发送请求到embedding服务，文字: {}", text);

            // 创建请求对象
            Map<String, String> request = Map.of("text", text);

            EmbeddingResponse response = webClient.post()
                    .uri("http://" + embeddingHost + ":" + embeddingPort + "/embedding")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .timeout(Duration.ofMillis(embeddingTimeout))
                    .retry(3)
                    .block();

            if (response != null && response.getEmbedding() != null) {
                return response.getEmbedding();
            } else {
                throw new RuntimeException("响应中没有找到embedding数组");
            }

        } catch (WebClientResponseException e) {
            log.error("Embedding API请求失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Embedding API请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("调用Embedding API时发生异常", e);
            throw new RuntimeException("调用Embedding API时发生异常", e);
        }
    }
}
