package com.mentara.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约数据传输对象
 */
@Data
public class AppointmentDTO {
    private Long id;
    private Long userId;
    private Long expertUserId;
    private String expertName;
    private String userName;
    private LocalDateTime appointmentTime;
    private String status;
    private String description;
    private String contactInfo;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String expertReply;
    private String userRating;
    private Integer rating;
} 