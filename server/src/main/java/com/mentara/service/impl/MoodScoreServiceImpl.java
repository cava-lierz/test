package com.mentara.service.impl;

import com.mentara.dto.response.DepressCheckResponse;
import com.mentara.entity.MoodScore;
import com.mentara.entity.Post;
import com.mentara.entity.Checkin;
import com.mentara.entity.User;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.MoodScoreRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.CheckinRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.DepressCheckService;
import com.mentara.service.TextScoreService;
import com.mentara.service.MoodScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
public class MoodScoreServiceImpl implements MoodScoreService {

    @Autowired
    private MoodScoreRepository moodScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private DepressCheckService depressCheckService;

    @Autowired
    private TextScoreService textScoreService;

    // 报警阈值配置
    private static final int MOOD_ALERT_THRESHOLD = 2; // 心情得分低于2分触发报警
    private static final double ALERT_SCORE_THRESHOLD = 0.7; // 报警分数高于0.7触发报警

    @Override
    @Transactional
    @CacheEvict(value = {"moodScores", "statistics"}, allEntries = true)
    public MoodScore createMoodScoreForPost(Long postId, Long userId, String content) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // 调用AI服务进行情绪分析
        Integer moodGrade = textScoreService.scoreText(content, "mood");
        DepressCheckResponse depressCheckResponse = depressCheckService.checkDepress(content, "depression");
        Boolean isAlert = depressCheckResponse.getLabel() == 1 ? true : false;

        // 创建心情评分记录
        MoodScore moodScore = new MoodScore();
        moodScore.setUser(user);
        moodScore.setPost(post);
        moodScore.setContentType(MoodScore.ContentType.POST);
        moodScore.setContentText(content);
        moodScore.setMoodGrade(moodGrade);
        moodScore.setIsAlert(isAlert);
        moodScore.setAlertScore(depressCheckResponse.getScore());

        MoodScore savedMoodScore = moodScoreRepository.save(moodScore);
        
        if (isAlert) {
            log.warn("用户 {} 的帖子触发报警，心情得分: {}, 报警分数: {}", userId, moodGrade, depressCheckResponse.getScore());
        } else {
            log.info("用户 {} 的帖子心情分析完成，心情得分: {}, 报警分数: {}", userId, moodGrade, depressCheckResponse.getScore());
        }

        return savedMoodScore;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"moodScores", "statistics"}, allEntries = true)
    public MoodScore createMoodScoreForCheckin(Long checkinId, Long userId, String content) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Checkin checkin = checkinRepository.findById(checkinId)
            .orElseThrow(() -> new ResourceNotFoundException("Checkin", "id", checkinId));

        // 调用AI服务进行情绪分析
        Integer moodGrade = textScoreService.scoreText(content, "mood");
        DepressCheckResponse depressCheckResponse = depressCheckService.checkDepress(content, "depression");
        Boolean isAlert = depressCheckResponse.getLabel() == 1 ? true : false;
        // 创建心情评分记录
        MoodScore moodScore = new MoodScore();
        moodScore.setUser(user);
        moodScore.setCheckin(checkin);
        moodScore.setContentType(MoodScore.ContentType.CHECKIN);
        moodScore.setContentText(content);
        moodScore.setMoodGrade(moodGrade);
        moodScore.setIsAlert(isAlert);
        moodScore.setAlertScore(depressCheckResponse.getScore());

        MoodScore savedMoodScore = moodScoreRepository.save(moodScore);
        
        if (isAlert) {
            log.warn("用户 {} 的打卡记录触发报警，心情得分: {}, 报警分数: {}", userId, moodGrade, depressCheckResponse.getScore());
        } else {
            log.info("用户 {} 的打卡记录心情分析完成，心情得分: {}, 报警分数: {}", userId, moodGrade, depressCheckResponse.getScore());
        }

        return savedMoodScore;
    }

    @Override
    @Cacheable(value = "moodScores", key = "'alert_check_' + #moodGrade + '_' + #alertScore + '_' + #content.hashCode()")
    public boolean shouldTriggerAlert(Integer moodGrade, Double alertScore, String content) {
        // 检查心情得分是否过低
        boolean lowMood = moodGrade != null && moodGrade <= MOOD_ALERT_THRESHOLD;
        
        // 检查报警分数是否过高
        boolean highAlertScore = alertScore != null && alertScore >= ALERT_SCORE_THRESHOLD;
        
        // 检查内容是否包含危险关键词
        boolean hasDangerousContent = checkDangerousContent(content);
        
        return lowMood || highAlertScore || hasDangerousContent;
    }

    @Override
    @Cacheable(value = "moodScores", key = "'alert_score_' + #content.hashCode()")
    public Double calculateAlertScore(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // 使用抑郁检查服务计算报警分数
            var depressionResult = depressCheckService.checkDepress(content, "depression");
            var anxietyResult = depressCheckService.checkDepress(content, "anxiety");
            var stressResult = depressCheckService.checkDepress(content, "stress");

            // 计算综合报警分数（取最高分）
            double maxScore = 0.0;
            if (depressionResult != null && depressionResult.getScore() != null) {
                maxScore = Math.max(maxScore, depressionResult.getScore());
            }
            if (anxietyResult != null && anxietyResult.getScore() != null) {
                maxScore = Math.max(maxScore, anxietyResult.getScore());
            }
            if (stressResult != null && stressResult.getScore() != null) {
                maxScore = Math.max(maxScore, stressResult.getScore());
            }

            return maxScore;
        } catch (Exception e) {
            log.error("计算报警分数时发生异常", e);
            return 0.0;
        }
    }


    /**
     * 检查是否包含危险内容
     */
    private boolean checkDangerousContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 危险关键词列表
        String[] dangerousKeywords = {
            "自杀", "自残", "死亡", "结束生命", "不想活了", "活不下去了",
            "kill myself", "suicide", "die", "death", "end my life",
            "伤害", "暴力", "攻击", "伤害他人", "报复"
        };

        String lowerContent = content.toLowerCase();
        for (String keyword : dangerousKeywords) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
} 