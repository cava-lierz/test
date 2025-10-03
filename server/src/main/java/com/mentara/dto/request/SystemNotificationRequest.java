package com.mentara.dto.request;

import lombok.Data;

@Data
public class SystemNotificationRequest {
    private String title;
    private String content;
    private String actionUrl;
} 