package com.mentara.repository;

import com.mentara.entity.ChatRoomUser;
import com.mentara.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    ChatRoomUser findById(long id);
    List<ChatRoomUser> findByChatRoomId(Long chatRoomId);
    List<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    ChatRoomUser findByChatRoomIdAndId(Long chatRoomId, Long id);
    void deleteByChatRoomId(Long chatRoomId);
    
    // 新增：根据版本号过滤的查询方法
    List<ChatRoomUser> findByChatRoomIdAndVersionGreaterThan(Long chatRoomId, Integer version);
    
    // 新增：根据用户ID、房间类型和版本号查询
    @Query("SELECT cru FROM ChatRoomUser cru WHERE cru.user.id = :userId AND cru.chatRoom.type = :roomType AND cru.version > :version AND cru.chatRoom.isDeleted = false")
    List<ChatRoomUser> findByUserIdAndChatRoomTypeAndVersionGreaterThan(
            @Param("userId") Long userId, 
            @Param("roomType") ChatRoomType roomType, 
            @Param("version") Integer version);
} 