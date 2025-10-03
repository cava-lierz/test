 package com.mentara.service;

import com.mentara.dto.response.StatisticsResponse;

public interface StatisticsService {
    /**
     * 获取指定周的统计数据
     * @param year 年份
     * @param weekNumber 周数（1-53）
     * @return 统计数据
     */
    StatisticsResponse getWeeklyStatistics(int year, int weekNumber);

    /**
     * 获取指定月份的统计数据
     * @param year 年份
     * @param month 月份（1-12）
     * @return 统计数据
     */
    StatisticsResponse getMonthlyStatistics(int year, int month);
}