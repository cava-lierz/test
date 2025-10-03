package com.mentara.dto.request;

import lombok.Data;

@Data
public class ChatRoomUserRequest {
    private Long chatRoomId;
    private Long userId;
    private String displayNickname;
    private String displayAvatar;
} 