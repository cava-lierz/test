package com.mentara.service;

import com.mentara.entity.MoodScore;

public interface MoodScoreService {
    
    /**
     * 为Post创建心情评分记录
     */
    MoodScore createMoodScoreForPost(Long postId, Long userId, String content);
    
    /**
     * 为Checkin创建心情评分记录
     */
    MoodScore createMoodScoreForCheckin(Long checkinId, Long userId, String content);
    
    /**
     * 检查是否需要触发报警
     */
    boolean shouldTriggerAlert(Integer moodGrade, Double alertScore, String content);
    
    /**
     * 计算报警分数
     */
    Double calculateAlertScore(String content);
} 