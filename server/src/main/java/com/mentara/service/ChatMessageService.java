package com.mentara.service;

import com.mentara.dto.request.ChatMessageRequest;
import com.mentara.dto.response.ChatMessageResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(Long userId, ChatMessageRequest request);
    List<ChatMessageResponse> getMessagesByChatRoom(Long chatRoomId, Long userId);
    List<ChatMessageResponse> getMessagesByChatRoomAfterTimestamp(Long chatRoomId, Long userId, LocalDateTime timestamp);
    List<ChatMessageResponse> getMessagesByChatRoomPaged(Long chatRoomId, Long userId, java.time.LocalDateTime before, int limit);
} 