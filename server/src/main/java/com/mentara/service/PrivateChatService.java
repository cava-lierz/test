package com.mentara.service;

import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.dto.response.UserProfileResponse;

import java.util.List;

public interface PrivateChatService {
    
    /**
     * 获取用户的私聊房间列表
     */
    List<ChatRoomResponse> getPrivateRooms(Long userId);
    
    /**
     * 创建或获取与指定用户的私聊房间
     */
    ChatRoomResponse createOrGetPrivateRoom(Long userId, Long targetUserId);
    
    /**
     * 删除私聊房间（用户退出私聊）
     */
    void deletePrivateRoom(Long chatRoomId, Long userId);
    
    /**
     * 获取私聊房间中的对方用户信息
     */
    UserProfileResponse getOtherUser(Long chatRoomId, Long userId);
} 