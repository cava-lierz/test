package com.mentara.service;

import com.mentara.dto.request.ChatRoomRequest;
import com.mentara.dto.response.ChatRoomResponse;
import java.util.List;

public interface ChatRoomService {
    ChatRoomResponse createRoom(ChatRoomRequest request, Long creatorUserId);
    List<ChatRoomResponse> getAllRooms();
    ChatRoomResponse getRoomById(Long id);
    void deleteRoom(Long roomId);
} 