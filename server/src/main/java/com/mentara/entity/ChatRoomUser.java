package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "chat_room_users")
public class ChatRoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "display_nickname")
    private String displayNickname;

    @Column(name = "display_avatar")
    private String displayAvatar;

    @Column(name = "version", nullable = false)
    private Integer version; //已删除用户的版本为0，已退房的用户版本为-1

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    // 移除级联关系，避免循环查询
    // @OneToMany(mappedBy = "chatRoomUser", cascade = CascadeType.ALL)
    // private Set<ChatMessage> chatMessages = new HashSet<>();
} 