package com.mentara.controller;

import com.mentara.dto.response.StatisticsResponse;
import com.mentara.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取指定周的统计数据
     * @param year 年份
     * @param week 周数（1-53）
     * @return 统计数据
     */
    @GetMapping("/weekly")
    public StatisticsResponse getWeeklyStatistics(
            @RequestParam int year,
            @RequestParam int week) {
        return statisticsService.getWeeklyStatistics(year, week);
    }

    /**
     * 获取指定月份的统计数据
     * @param year 年份
     * @param month 月份（1-12）
     * @return 统计数据
     */
    @GetMapping("/monthly")
    public StatisticsResponse getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        return statisticsService.getMonthlyStatistics(year, month);
    }
}