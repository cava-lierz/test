package com.mentara.dto.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long chatRoomId;
    private String content;
} 