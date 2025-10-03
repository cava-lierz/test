package com.mentara.dto.response;

import lombok.Data;

@Data
public class ChatRoomUserResponse {
    private Long chatRoomId;
    private Long chatRoomUserId;  // 房间用户ID
    private Integer version;
    private String displayNickname;
    private String displayAvatar;
} 