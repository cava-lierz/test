package com.mentara.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemNotificationResponse extends BaseNotificationResponse {
    private String title;
    private String content;
    private String actionUrl;
}