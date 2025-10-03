package com.mentara.service.impl;

import com.mentara.entity.Checkin;
import com.mentara.entity.User;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.dto.request.CheckinRequest;
import com.mentara.dto.response.CheckinResponse;
import com.mentara.converter.CheckinConverter;
import com.mentara.repository.CheckinRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.CheckinService;
import com.mentara.service.MoodScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinConverter checkinConverter;

    @Autowired
    private MoodScoreService moodScoreService;

    @Override
    @Transactional
    public void createCheckin(CheckinRequest checkinRequest, Long userId) {
        Checkin checkin = new Checkin();
        checkin.setRating(checkinRequest.getRating());
        checkin.setNote(checkinRequest.getNote());
        checkin.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
        Checkin savedCheckin = checkinRepository.save(checkin);

        // 创建心情评分记录
        if (checkinRequest.getNote() != null && !checkinRequest.getNote().trim().isEmpty()) {
            try {
                moodScoreService.createMoodScoreForCheckin(savedCheckin.getId(), userId, checkinRequest.getNote());
            } catch (Exception e) {
                // 记录错误但不影响checkin的创建
                System.err.println("创建心情评分记录失败: " + e.getMessage());
            }
        }
    }

    @Override
    public List<CheckinResponse> getCurrentWeekData(Long userId) {
        return getWeekDataByDate(userId, LocalDate.now());
    }

    @Override
    public List<CheckinResponse> getWeekData(Long userId, int weeksAgo) {
        LocalDate targetDate = LocalDate.now().minusWeeks(weeksAgo);
        return getWeekDataByDate(userId, targetDate);
    }

    @Override
    public List<CheckinResponse> getWeekDataByDate(Long userId, LocalDate targetDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = targetDate.with(DayOfWeek.SUNDAY);

        List<Checkin> checkins = checkinRepository.findByUserAndCheckinDateBetween(user, monday, sunday);
        
        Map<LocalDate, Checkin> checkinMap = checkins.stream()
            .collect(Collectors.toMap(Checkin::getCheckinDate, Function.identity()));

        List<CheckinResponse> weekData = new ArrayList<>();
        for (LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)) {
            Checkin checkin = checkinMap.get(date);
            if (checkin != null) {
                weekData.add(checkinConverter.toCheckinResponse(checkin));
            } else {
                weekData.add(emptyCheckinResponse(user, date));
            }
        }
        return weekData;
    }

    @Override
    public CheckinResponse getTodayData(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        LocalDate today = LocalDate.now();
        return checkinRepository.findByUserAndCheckinDate(user, today)
            .map(checkinConverter::toCheckinResponse)
            .orElse(
                emptyCheckinResponse(user, today)
            );
    }

    private CheckinResponse emptyCheckinResponse(User user, LocalDate date) {
        return CheckinResponse.builder()
            .id(null)
            .rating(null)
            .note(null)
            .checkinDate(date)
            .weekday(date.getDayOfWeek())
            .userId(user.getId())
            .nickname(user.getNickname())
            .build();
    }

    @Override
    public Page<CheckinResponse> getUserCheckins(Long userId, Pageable pageable) {
        // 验证用户是否存在
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Page<Checkin> checkins = checkinRepository.findByUserIdOrderByCheckinDateDesc(userId, pageable);
        return checkins.map(checkinConverter::toCheckinResponse);
    }
}