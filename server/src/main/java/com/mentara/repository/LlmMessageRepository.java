package com.mentara.repository;

import com.mentara.document.LlmMessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LLM历史记录Repository接口
 */
@Repository
public interface LlmMessageRepository extends MongoRepository<LlmMessageDocument, String> {
    
    /**
     * 根据会话ID查找历史记录
     */
    List<LlmMessageDocument> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * 根据角色查找历史记录
     */
    List<LlmMessageDocument> findByRole(String role);
    
    /**
     * 根据创建时间范围查找历史记录
     */
    List<LlmMessageDocument> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计会话的历史记录数量
     */
    long countBySessionId(String sessionId);
    
    /**
     * 根据消息内容进行全文搜索
     */
    @Query("{'content': {$regex: ?0, $options: 'i'}}")
    List<LlmMessageDocument> findByContentContainingIgnoreCase(String content);
    
    /**
     * 根据会话ID和消息内容进行全文搜索
     */
    @Query("{'sessionId': ?0, 'content': {$regex: ?1, $options: 'i'}}")
    List<LlmMessageDocument> findBySessionIdAndContentContainingIgnoreCase(String sessionId, String content);
    
    /**
     * 查找会话最近的历史记录
     */
    Optional<LlmMessageDocument> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    /**
     * 删除会话的所有历史记录
     */
    void deleteBySessionId(String sessionId);
} 