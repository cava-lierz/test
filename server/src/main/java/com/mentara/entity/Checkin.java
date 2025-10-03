package com.mentara.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.DayOfWeek;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "checkins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
    }, 
    indexes = {
        @Index(name = "idx_checkins_user_checkin_date", columnList = "user_id, checkin_date DESC")
    }
)
public class Checkin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 心情评分 1-5

    @Column(name = "note")
    private String note; // 备注

    @Column(name = "checkin_date")
    private LocalDate checkinDate;

    @Column(name = "weekday")
    private DayOfWeek weekday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关联的心情评分记录
    @OneToOne(mappedBy = "checkin", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MoodScore moodScore;

    @PrePersist
    protected void onCreate() {
        checkinDate = LocalDate.now();
        weekday = LocalDate.now().getDayOfWeek();
    }
}