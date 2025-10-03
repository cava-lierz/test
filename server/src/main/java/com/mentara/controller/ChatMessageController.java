package com.mentara.controller;

import com.mentara.dto.request.ChatMessageRequest;
import com.mentara.dto.response.ChatMessageResponse;
import com.mentara.service.ChatMessageService;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/chat-message")
public class ChatMessageController {
    @Autowired
    private ChatMessageService chatMessageService;

    // 发送消息
    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ChatMessageResponse sendMessage(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody ChatMessageRequest request) {
        return chatMessageService.sendMessage(currentUser.getId(), request);
    }

    // 获取聊天室消息列表
    @GetMapping("/room/{chatRoomId}")
    public List<ChatMessageResponse> getMessagesByChatRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser
    ) {
        List<ChatMessageResponse> messages;
        try {
            messages = chatMessageService.getMessagesByChatRoom(chatRoomId, currentUser.getId());
            return messages;
        } catch (RuntimeException e) {
            return null;
        }
    }

    // 获取某个时间戳以后的消息记录
    @GetMapping("/room/{chatRoomId}/after")
    @PreAuthorize("isAuthenticated()")
    public List<ChatMessageResponse> getMessagesAfterTimestamp(
            @PathVariable Long chatRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @CurrentUser UserPrincipal currentUser
    ) {
        return chatMessageService.getMessagesByChatRoomAfterTimestamp(
                chatRoomId,
                currentUser.getId(),
                timestamp
        );
    }

    @GetMapping("/room/{chatRoomId}/paged")
    public List<ChatMessageResponse> getMessagesByChatRoomPaged(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime before,
            @RequestParam(defaultValue = "20") int limit,
            @CurrentUser com.mentara.security.UserPrincipal currentUser
    ) {
        return chatMessageService.getMessagesByChatRoomPaged(chatRoomId, currentUser.getId(), before, limit);
    }
} 