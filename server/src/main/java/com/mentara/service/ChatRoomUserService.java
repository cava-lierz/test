package com.mentara.service;

import com.mentara.dto.request.ChatRoomUserRequest;
import com.mentara.dto.response.ChatRoomUserResponse;
import com.mentara.entity.ChatRoomUser;

import java.util.List;

public interface ChatRoomUserService {
    ChatRoomUser getChatRoomUserByByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    ChatRoomUserResponse addUserToRoom(ChatRoomUserRequest request);

    ChatRoomUserResponse getUserByRoomAndId(Long chatRoomId, Long id);

    boolean isInRoom(Long chatRoomId, Long userId);

    ChatRoomUserResponse getUserFromRoom(Long chatRoomId, Long userId);

    List<ChatRoomUserResponse> getUsersByRoom(Long chatRoomId);

    ChatRoomUserResponse joinRoom(Long chatRoomId, Long userId);

    void save(ChatRoomUser chatRoomUser);

    void deleteUser(Long id);

    void quitRoom(Long id);
}