package com.mentara.service;

import com.mentara.dto.request.CheckinRequest;
import com.mentara.dto.response.CheckinResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CheckinService {
    void createCheckin(CheckinRequest checkinRequest, Long userId);
    List<CheckinResponse> getCurrentWeekData(Long userId);
    List<CheckinResponse> getWeekData(Long userId, int weeksAgo);
    List<CheckinResponse> getWeekDataByDate(Long userId, LocalDate targetDate);
    CheckinResponse getTodayData(Long userId);
    Page<CheckinResponse> getUserCheckins(Long userId, Pageable pageable);
}