package com.mentara.controller;

import com.mentara.dto.request.ChatRoomUserRequest;
import com.mentara.dto.response.ChatRoomUserResponse;
import com.mentara.entity.ChatRoomUser;
import com.mentara.entity.User;
import com.mentara.service.ChatRoomUserService;
import com.mentara.service.FileUploadService;
import com.mentara.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat-room-user")
public class ChatRoomUserController {
    @Autowired
    private ChatRoomUserService chatRoomUserService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse addUserToRoom(
            @RequestBody ChatRoomUserRequest request) {
        return chatRoomUserService.addUserToRoom(request);
    }

    @GetMapping("/room/{chatRoomId}/isInRoom")
    @PreAuthorize("isAuthenticated()")
    public  boolean isInRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser
    ){
        return  chatRoomUserService.isInRoom(chatRoomId, currentUser.getId());
    }

    @GetMapping("/room/{chatRoomId}/me")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse getMyRoomUserFromRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser
    ){
        return chatRoomUserService.getUserFromRoom(chatRoomId, currentUser.getId());
    }

    @PostMapping("/quitRoom/chatRoomUserId/{chatRoomUserId}")
    @PreAuthorize("isAuthenticated()")
    public boolean quitRoom(
            @PathVariable Long chatRoomUserId
    ){
        try {
            chatRoomUserService.quitRoom(chatRoomUserId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/room/{chatRoomId}/me/uploadAvatar")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse uploadMyAvatar(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser
    ){
        ChatRoomUser chatRoomUser = chatRoomUserService.getChatRoomUserByByChatRoomIdAndUserId(chatRoomId, currentUser.getId());
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (chatRoomUser.getDisplayAvatar() != null &&
            !chatRoomUser.getDisplayAvatar().isEmpty() &&
            !chatRoomUser.getDisplayAvatar().equals(user.getAvatar())) {
            fileUploadService.deleteAvatar(chatRoomUser.getDisplayAvatar());
        }

        String avatarUrl = fileUploadService.uploadAvatar(file, user.getId());
        chatRoomUser.setDisplayAvatar(avatarUrl);
        Integer version = chatRoomUser.getVersion();
        chatRoomUser.setVersion(version + 1);
        chatRoomUserService.save(chatRoomUser);

        return chatRoomUserService.getUserFromRoom(chatRoomId, currentUser.getId());
    }

    @PutMapping("/room/{chatRoomId}/me/setNickname")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse setMyNickname(
            @RequestParam("displayNickname") String displayNickname,
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal currentUser
    ){
        ChatRoomUser chatRoomUser = chatRoomUserService.getChatRoomUserByByChatRoomIdAndUserId(chatRoomId, currentUser.getId());
        
        chatRoomUser.setDisplayNickname(displayNickname);
        Integer version = chatRoomUser.getVersion();
        chatRoomUser.setVersion(version + 1);
        chatRoomUserService.save(chatRoomUser);

        return chatRoomUserService.getUserFromRoom(chatRoomId, currentUser.getId());
    }

    @GetMapping("/room/{chatRoomId}/RoomUser/{roomUserId}")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse getRoomUserFromRoom(
            @PathVariable Long chatRoomId,
            @PathVariable Long roomUserId){
        return chatRoomUserService.getUserByRoomAndId(chatRoomId, roomUserId);
    }

    @GetMapping("/room/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public List<ChatRoomUserResponse> getUsersByRoom(
            @PathVariable Long chatRoomId) {
        return chatRoomUserService.getUsersByRoom(chatRoomId);
    }

    @PostMapping("/join/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public ChatRoomUserResponse joinRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser UserPrincipal userPrincipal) {
        return chatRoomUserService.joinRoom(chatRoomId, userPrincipal.getId());
    }
} 