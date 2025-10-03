package com.mentara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinResponse {
    private Long id;
    private Integer rating;
    private String note;
    private LocalDate checkinDate;
    private DayOfWeek weekday;
    private Long userId;
    private String nickname;
}