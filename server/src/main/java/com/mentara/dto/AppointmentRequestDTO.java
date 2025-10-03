package com.mentara.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约请求数据传输对象
 */
@Data
public class AppointmentRequestDTO {
    private Long expertUserId;
    private LocalDateTime appointmentTime;
    private String description;
    private String contactInfo;
    private Integer duration = 60;
    
    @Override
    public String toString() {
        return "AppointmentRequestDTO{" +
                "expertUserId=" + expertUserId +
                ", appointmentTime=" + appointmentTime +
                ", description='" + description + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", duration=" + duration +
                '}';
    }
} 