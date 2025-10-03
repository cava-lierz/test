package com.mentara.repository;

import com.mentara.document.LlmSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LLM会话Repository
 */
@Repository
public interface LlmSessionRepository extends MongoRepository<LlmSessionDocument, String> {
    
    /**
     * 根据用户ID查找所有会话
     */
    List<LlmSessionDocument> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据会话ID查找会话
     */
    @Query("{'id': ?0}")
    LlmSessionDocument findBySessionId(String sessionId);
    
    /**
     * 根据用户ID和会话ID查找会话
     */
    @Query("{'userId': ?0, 'id': ?1}")
    LlmSessionDocument findByUserIdAndSessionId(Long userId, String sessionId);
    
    /**
     * 删除指定会话
     */
    @Query(value = "{'id': ?0}", delete = true)
    long deleteBySessionId(String sessionId);
} 