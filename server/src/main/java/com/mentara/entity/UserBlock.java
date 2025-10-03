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
@Table(name = "user_blocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
    }, 
    indexes = {
        @Index(name = "idx_user_blocks_blocker_blocked", columnList = "blocker_id, blocked_id"),
        @Index(name = "idx_user_blocks_blocked_blocker", columnList = "blocked_id, blocker_id")
    }
)
public class UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker; // 拉黑者

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked; // 被拉黑者

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reason", length = 500)
    private String reason; // 拉黑原因

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 构造函数
    public UserBlock(User blocker, User blocked, String reason) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.reason = reason;
    }
} 