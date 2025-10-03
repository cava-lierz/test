package com.mentara.controller;

import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.service.PrivateChatService;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat-room/private")
public class PrivateChatController {

    @Autowired
    private PrivateChatService privateChatService;

    /**
     * 获取当前用户的私聊房间列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public List<ChatRoomResponse> getPrivateRooms(@CurrentUser UserPrincipal currentUser) {
        return privateChatService.getPrivateRooms(currentUser.getId());
    }

    /**
     * 创建或获取与指定用户的私聊房间
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomResponse createOrGetPrivateRoom(
            @RequestBody Map<String, Long> request,
            @CurrentUser UserPrincipal currentUser) {
        Long targetUserId = request.get("targetUserId");
        return privateChatService.createOrGetPrivateRoom(currentUser.getId(), targetUserId);
    }

    /**
     * 删除私聊房间（退出私聊）
     */
    @DeleteMapping("/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> deletePrivateRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser) {
        privateChatService.deletePrivateRoom(chatRoomId, currentUser.getId());
        return Map.of("message", "私聊已删除");
    }

    /**
     * 获取私聊房间中的对方用户信息
     */
    @GetMapping("/{chatRoomId}/other-user")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse getOtherUser(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser) {
        return privateChatService.getOtherUser(chatRoomId, currentUser.getId());
    }
} 