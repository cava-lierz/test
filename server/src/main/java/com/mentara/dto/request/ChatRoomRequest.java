package com.mentara.dto.request;

import com.mentara.enums.ChatRoomType;
import lombok.Data;

@Data
public class ChatRoomRequest {
    private String name;
    private String description;
    private ChatRoomType type;
} 