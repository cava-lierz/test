package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mood_scores", indexes = {
    @Index(name = "idx_mood_scores_user_created", columnList = "user_id, created_at DESC")
})
public class MoodScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关联Post（可选）
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 关联Checkin（可选）
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id")
    private Checkin checkin;

    // 内容类型：POST 或 CHECKIN
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    // 原始文本内容
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    // AI分析的心情得分（0-1分）
    @Column(name = "mood_grade")
    private Integer moodGrade = 1;

    // 是否触发报警
    @Column(name = "is_alert", nullable = false)
    private Boolean isAlert = false;

    @Column(name = "alert_socre")
    private double alertScore;

    // 创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 内容类型枚举
    public enum ContentType {
        POST,       // 帖子
        CHECKIN     // 打卡记录
    }
}
