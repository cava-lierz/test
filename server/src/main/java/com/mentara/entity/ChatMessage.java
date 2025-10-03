package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_room_sent", columnList = "chat_room_id, sent_at ASC"),
    @Index(name = "idx_chat_messages_room_sent_desc", columnList = "chat_room_id, sent_at DESC"),
    @Index(name = "idx_chat_messages_room_user_sent", columnList = "chat_room_id, chat_room_user_id, sent_at ASC"),
    @Index(name = "idx_chat_messages_sent_at", columnList = "sent_at DESC")
})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_user_id")
    private ChatRoomUser chatRoomUser;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
} 