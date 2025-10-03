package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * 心理专家实体
 */
@Entity
@Getter
@Setter
@Table(name = "experts")
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的用户ID，当专家有账号时使用 */
    @Column(name = "user_id")
    private Long userId;

    /** 关联的用户实体 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    /** 专家姓名 */
    @Column(nullable = false, length = 64)
    private String name;

    /** 专业领域 */
    @Column(length = 128)
    private String specialty;

    /** 联系方式（邮箱/电话等） */
    @Column(length = 128)
    private String contact;

    /** 在线状态：online / offline */
    @Column(length = 16)
    private String status;

    // 移除appointments映射，因为现在预约直接通过User实体管理
    // 预约关系：User -> Appointment (expertUser)
    // 专家关系：Expert -> User (userId)
} 