package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;     

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_receiver_read_created", columnList = "receiver_id, is_read, created_at DESC")
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver; // 通知接收者

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private boolean read = false; // 是否已读
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 