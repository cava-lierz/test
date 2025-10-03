package com.mentara.service.impl;

import com.mentara.entity.Post;
import com.mentara.service.EmbeddingService;
import com.mentara.service.QdrantService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
public class QdrantServiceImpl implements QdrantService {

    @Autowired
    private QdrantClient qdrantClient;

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${qdrant.collection.name:post_vector}")
    private String collectionName;

    @Override
    public Long upsertPostVector(Post post) {
        try {
            log.info("开始存储帖子向量，帖子ID: {}", post.getId());

            // 构建帖子文本内容
            String postText = buildPostText(post);
            
            // 获取文本向量
            log.info("开始向量化");
            List<Float> vector = embeddingService.getEmbedding(postText);
            if (vector == null || vector.isEmpty()) {
                log.error("无法获取帖子文本的向量");
                return null;
            }

            qdrantClient.upsertAsync(
                    collectionName,List.of(
                            Points.PointStruct.newBuilder()
                                    .setId(id(post.getId()))
                                    .setVectors(vectors(vector))
                                    .build()
                    )
            );

            log.info("成功存储帖子向量，帖子ID: {}", post.getId());
            return post.getId();

        } catch (Exception e) {
            log.error("存储帖子向量失败，帖子ID: {}", post.getId(), e);
            return post.getId();
        }
    }

    @Override
    public List<Points.ScoredPoint> queryPostVector(String query) {
        try {
            
            // 获取查询向量
            List<Float> queryVector = embeddingService.getEmbedding(query);
            if (queryVector == null || queryVector.isEmpty()) {
                log.error("无法获取查询文本的向量");
                return null;
            }

            List<Points.ScoredPoint> searchResult =
                    qdrantClient.queryAsync(Points.QueryPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .setLimit(10)
                            .setQuery(nearest(queryVector))
                            .build()).get();


            if (searchResult != null) {
                log.info("找到最近似结果：{}", searchResult.size());
                return searchResult;
            } else {
                log.info("未找到相似帖子");
                return null;
            }

        } catch (Exception e) {
            log.error("查询相似帖子失败，搜索词 {}", query, e);
            return null;
        }
    }

    @Override
    public Long deletePostVector(Post post) {
        try {
            log.info("开始删除帖子向量，帖子ID: {}", post.getId());

            qdrantClient.deleteAsync(
                    collectionName,
                    List.of(id(post.getId()))
            );
            
            log.info("成功删除帖子向量，帖子ID: {}", post.getId());
            return post.getId();

        } catch (Exception e) {
            log.error("删除帖子向量失败，帖子ID: {}", post.getId(), e);
            return post.getId();
        }
    }

    @Override
    public List<Points.ScoredPoint> accurateQueryPostVector(String query){
        try {

            // 获取查询向量
            List<Float> queryVector = embeddingService.getEmbedding(query);
            if (queryVector == null || queryVector.isEmpty()) {
                log.error("无法获取查询文本的向量");
                return null;
            }

            List<Points.ScoredPoint> searchResult =
                    qdrantClient.queryAsync(Points.QueryPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .setLimit(10)
                            .setFilter(Points.Filter.newBuilder().addMust(matchKeyword("text", query)))
                            .setQuery(nearest(queryVector))
                            .build()).get();


            if (searchResult != null) {
                log.info("找到最近似结果：{}", searchResult.size());
                return searchResult;
            } else {
                log.info("未找到相似帖子");
                return null;
            }

        } catch (Exception e) {
            log.error("查询相似帖子失败，搜索词 {}", query, e);
            return null;
        }
    }
    /**
     * 构建帖子文本内容
     */
    private String buildPostText(Post post) {
        StringBuilder text = new StringBuilder();
        
        if (post.getTitle() != null) {
            text.append(post.getTitle()).append(" ");
        }
        
        if (post.getContent() != null) {
            text.append(post.getContent()).append(" ");
        }
        
        if (post.getMood() != null) {
            text.append("心情: ").append(post.getMood().name()).append(" ");
        }
        
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            text.append("标签: ");
            post.getTags().forEach(tag -> text.append(tag.getId()).append(" "));
        }
        
        return text.toString().trim();
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
