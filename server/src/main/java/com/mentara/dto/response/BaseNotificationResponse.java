package com.mentara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseNotificationResponse {
    private String type;
    private Long id;
    private LocalDateTime createdAt;
    private boolean read;
    
} 