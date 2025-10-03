package com.mentara.controller;

import com.mentara.dto.request.ChatRoomRequest;
import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;

@RestController
@RequestMapping("/chat-room")
public class ChatRoomController {
    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomResponse createRoom(
            @RequestBody ChatRoomRequest request,
            @CurrentUser UserPrincipal currentUser) {
        return chatRoomService.createRoom(request, currentUser.getId());
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public List<ChatRoomResponse> getAllRooms() {
        return chatRoomService.getAllRooms();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomResponse getRoomById(
            @PathVariable Long id) {
        return chatRoomService.getRoomById(id);
    }
} 