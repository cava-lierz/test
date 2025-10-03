package com.mentara.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class QdrantConfig {

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6334}")
    private int qdrantPort;

    @Value("${qdrant.timeout:30}")
    private int timeoutSeconds;

    @Value("${qdrant.collection.name}")
    private String collectionName;

    @Bean
    public QdrantClient qdrantClient() {
        try {
            System.out.println("正在连接Qdrant服务器: " + qdrantHost + ":" + qdrantPort);
            
            QdrantClient client = new QdrantClient(
                    QdrantGrpcClient.newBuilder(qdrantHost, qdrantPort, false)
                            .withTimeout(Duration.ofSeconds(timeoutSeconds))
                            .build());
            
            // 检查连接
            try {
                Collections.CollectionInfo collectionInfo = client.getCollectionInfoAsync(collectionName).get();
                if (collectionInfo != null) {
                    System.out.println("成功连接到Qdrant服务器");
                    return client;
                }
            } catch (Exception e) {
                System.out.println("警告: 无法获取集合列表，但继续尝试创建集合: " + e.getMessage());
            }
            
            // 尝试创建集合
            try {
                client.createCollectionAsync(collectionName,
                        Collections.VectorParams.newBuilder()
                                .setDistance(Collections.Distance.Dot)
                                .setSize(768)
                                .build()).get();
                System.out.println("成功创建集合: " + collectionName);
            } catch (Exception e) {
                System.out.println("集合可能已存在或创建失败: " + e.getMessage());
            }
            
            return client;
        } catch (Exception e) {
            System.err.println("创建Qdrant客户端失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create Qdrant client: " + e.getMessage(), e);
        }
    }
} 
