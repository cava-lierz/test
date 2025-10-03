package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * 专家预约实体
 */
@Entity
@Getter
@Setter
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 预约用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    /** 专家用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_user_id", nullable = false)
    @JsonIgnore
    private User expertUser;

    /** 关联的专家（对应expert_id字段） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    @JsonIgnore
    private Expert expert;

    /** 预约时间 */
    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    /** 预约状态：pending(待确认), confirmed(已确认), cancelled(已取消), completed(已完成) */
    @Column(length = 20, nullable = false)
    private String status = "pending";

    /** 预约主题/问题描述 */
    @Column(length = 500)
    private String description;

    /** 联系方式 */
    @Column(length = 128)
    private String contactInfo;

    /** 预约时长（分钟） */
    @Column(nullable = false)
    private Integer duration = 60;

    /** 创建时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 专家回复 */
    @Column(length = 1000)
    private String expertReply;

    /** 用户评价 */
    @Column(length = 500)
    private String userRating;

    /** 评分（1-5星） */
    @Column
    private Integer rating;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 