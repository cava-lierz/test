package com.mentara.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long chatRoomUserId;  // 房间用户ID
    private String content;
    private Integer version;
    private LocalDateTime sentAt;
} 