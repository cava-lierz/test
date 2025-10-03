package com.mentara.repository;

import com.mentara.entity.MoodScore;
import com.mentara.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodScoreRepository extends JpaRepository<MoodScore, Long> {
    
    // 根据用户和内容类型查找
    List<MoodScore> findByUserAndContentTypeOrderByCreatedAtDesc(User user, MoodScore.ContentType contentType);
    
    // 根据Post查找
    Optional<MoodScore> findByPostId(Long postId);
    
    // 根据Checkin查找
    Optional<MoodScore> findByCheckinId(Long checkinId);
    
    // 查找触发报警的记录
    List<MoodScore> findByIsAlertTrueOrderByCreatedAtDesc();
    
    // 查找用户最近的报警记录
    List<MoodScore> findByUserAndIsAlertTrueOrderByCreatedAtDesc(User user);
    
    // 查找指定时间范围内的记录
    List<MoodScore> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
        User user, LocalDateTime startTime, LocalDateTime endTime);
    
    // 统计用户的报警次数
    @Query("SELECT COUNT(m) FROM MoodScore m WHERE m.user.id = :userId AND m.isAlert = true")
    Long countAlertsByUserId(@Param("userId") Long userId);
    
    // 分页查询报警记录
    Page<MoodScore> findByIsAlertTrue(Pageable pageable);
    
    // 查找用户平均心情得分
    @Query("SELECT AVG(m.moodGrade) FROM MoodScore m WHERE m.user.id = :userId AND m.moodGrade IS NOT NULL")
    Double findAverageMoodScoreByUserId(@Param("userId") Long userId);
} 