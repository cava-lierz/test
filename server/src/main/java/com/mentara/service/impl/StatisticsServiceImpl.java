 package com.mentara.service.impl;

import com.mentara.dto.response.StatisticsResponse;
import com.mentara.repository.UserRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.ReportRepository;
import com.mentara.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    @Override
    @Cacheable(value = "statistics", key = "'weekly_' + #year + '_' + #weekNumber")
    public StatisticsResponse getWeeklyStatistics(int year, int weekNumber) {
        // 计算指定周的开始和结束时间
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate date = LocalDate.of(year, 1, 1)
            .with(weekFields.weekOfWeekBasedYear(), weekNumber)
            .with(weekFields.dayOfWeek(), 1);
        
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(7).atStartOfDay().minusNanos(1);

        return getStatistics(startDate, endDate);
    }

    @Override
    @Cacheable(value = "statistics", key = "'monthly_' + #year + '_' + #month")
    public StatisticsResponse getMonthlyStatistics(int year, int month) {
        // 计算指定月的开始和结束时间
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.with(TemporalAdjusters.lastDayOfMonth())
            .withHour(23).withMinute(59).withSecond(59);

        return getStatistics(startDate, endDate);
    }

    private StatisticsResponse getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // 统计新增用户数
        int newUsers = userRepository.findAll().stream()
            .filter(user -> user.getCreatedAt() != null &&
                          user.getCreatedAt().isAfter(startDate) &&
                          user.getCreatedAt().isBefore(endDate))
            .mapToInt(user -> 1)
            .sum();

        // 统计新增帖子数
        int totalPosts = postRepository.findAll().stream()
            .filter(post -> post.getCreatedAt().isAfter(startDate) &&
                          post.getCreatedAt().isBefore(endDate))
            .mapToInt(post -> 1)
            .sum();

        // 统计举报数
        int reportCount = reportRepository.findAll().stream()
            .filter(report -> report.getCreatedAt().isAfter(startDate) &&
                            report.getCreatedAt().isBefore(endDate))
            .mapToInt(report -> 1)
            .sum();

        // 统计活跃用户数（未被禁用的用户）
        int activeUsers = (int) userRepository.findAll().stream()
            .filter(user -> user.getIsDisabled() == null || !user.getIsDisabled())
            .count();

        return StatisticsResponse.builder()
            .newUsers(newUsers)
            .totalPosts(totalPosts)
            .reportCount(reportCount)
            .activeUsers(activeUsers)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}