package com.mentara.repository;

import com.mentara.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
    List<ChatMessage> findByChatRoomIdAndChatRoomUserIdOrderBySentAtAsc(Long chatRoomId, Long chatRoomUserId);
    List<ChatMessage> findByChatRoomIdAndSentAtAfterOrderBySentAtAsc(Long chatRoomId, LocalDateTime afterTimestamp);
    List<ChatMessage> findByChatRoomIdAndSentAtLessThanOrderBySentAtDesc(Long chatRoomId, java.time.LocalDateTime before, Pageable pageable);
    List<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Pageable pageable);
    
    /**
     * 优化查询：使用子查询避免N+1问题，同时保持分页准确性
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.id IN (" +
           "  SELECT cm2.id FROM ChatMessage cm2 " +
           "  WHERE cm2.chatRoom.id = :chatRoomId " +
           "  ORDER BY cm2.sentAt DESC" +
           ") " +
           "ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByChatRoomIdOptimizedOrderBySentAtDesc(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
    
    /**
     * 优化查询：带时间限制的子查询
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.sentAt < :before " +
           "AND cm.id IN (" +
           "  SELECT cm2.id FROM ChatMessage cm2 " +
           "  WHERE cm2.chatRoom.id = :chatRoomId " +
           "  AND cm2.sentAt < :before " +
           "  ORDER BY cm2.sentAt DESC" +
           ") " +
           "ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByChatRoomIdAndSentAtLessThanOptimizedOrderBySentAtDesc(
        @Param("chatRoomId") Long chatRoomId, 
        @Param("before") LocalDateTime before, 
        Pageable pageable);
} 