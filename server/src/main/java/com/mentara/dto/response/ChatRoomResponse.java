package com.mentara.dto.response;

import com.mentara.enums.ChatRoomType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String description;
    private ChatRoomType type;
    private LocalDateTime createdAt;
} 