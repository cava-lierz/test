package com.mentara.service.impl;

import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.entity.ChatRoom;
import com.mentara.entity.ChatRoomUser;
import com.mentara.entity.User;
import com.mentara.enums.ChatRoomType;
import com.mentara.repository.ChatRoomRepository;
import com.mentara.repository.ChatRoomUserRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.PrivateChatService;
import com.mentara.service.ChatRoomUserService;
import com.mentara.dto.request.ChatRoomUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChatRoomUserService chatRoomUserService;

    @Autowired
    private com.mentara.service.OssService ossService;

    @Override
    public List<ChatRoomResponse> getPrivateRooms(Long userId) {
        // 获取用户参与的所有私聊房间
        List<ChatRoomUser> userRooms = chatRoomUserRepository.findByUserIdAndChatRoomTypeAndVersionGreaterThan(
                userId, ChatRoomType.PRIVATE, 0);
        
        return userRooms.stream()
                .map(chatRoomUser -> {
                    ChatRoom room = chatRoomUser.getChatRoom();
                    ChatRoomResponse response = toResponse(room);
                    
                    // 为私聊房间设置显示名称（对方的昵称）
                    try {
                        UserProfileResponse otherUser = getOtherUser(room.getId(), userId);
                        response.setName(otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
                    } catch (Exception e) {
                        response.setName("私聊");
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatRoomResponse createOrGetPrivateRoom(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new RuntimeException("不能与自己创建私聊");
        }
        
        // 检查目标用户是否存在
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));
        
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 查找是否已存在私聊房间
        ChatRoom existingRoom = findExistingPrivateRoom(userId, targetUserId);
        if (existingRoom != null) {
            // 确保两个用户都在房间中
            ensureUsersInRoom(existingRoom, userId, targetUserId);
            ChatRoomResponse response = toResponse(existingRoom);
            response.setName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
            return response;
        }
        
        // 创建新的私聊房间
        ChatRoom room = new ChatRoom();
        room.setName("私聊"); // 这个名称会在前端根据对方用户动态显示
        room.setDescription("私聊房间");
        room.setType(ChatRoomType.PRIVATE);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        
        // 添加两个用户到房间
        addUserToPrivateRoom(savedRoom, currentUser);
        addUserToPrivateRoom(savedRoom, targetUser);
        
        ChatRoomResponse response = toResponse(savedRoom);
        response.setName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
        return response;
    }

    @Override
    @Transactional
    public void deletePrivateRoom(Long chatRoomId, Long userId) {
        // 验证房间存在且为私聊房间
        ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("私聊房间不存在"));
        
        if (room.getType() != ChatRoomType.PRIVATE) {
            throw new RuntimeException("只能删除私聊房间");
        }
        
        // 获取用户在该房间的记录
        List<ChatRoomUser> userInRoom = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (userInRoom.isEmpty()) {
            throw new RuntimeException("用户不在该私聊房间中");
        }
        
        // 让用户退出房间
        ChatRoomUser chatRoomUser = userInRoom.get(0);
        chatRoomUserService.quitRoom(chatRoomUser.getId());
        
        // 检查房间中是否还有其他活跃用户
        List<ChatRoomUser> activeUsers = chatRoomUserRepository.findByChatRoomIdAndVersionGreaterThan(chatRoomId, 0);
        if (activeUsers.isEmpty()) {
            // 如果没有活跃用户，软删除房间
            room.setIsDeleted(true);
            room.setDeletedAt(LocalDateTime.now());
            chatRoomRepository.save(room);
        }
    }

    @Override
    public UserProfileResponse getOtherUser(Long chatRoomId, Long userId) {
        // 验证房间存在且为私聊房间
        ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("私聊房间不存在"));
        
        if (room.getType() != ChatRoomType.PRIVATE) {
            throw new RuntimeException("只能查询私聊房间的对方用户");
        }
        
        // 获取房间中的所有用户
        List<ChatRoomUser> roomUsers = chatRoomUserRepository.findByChatRoomIdAndVersionGreaterThan(chatRoomId, 0);
        
        if (roomUsers.size() != 2) {
            throw new RuntimeException("私聊房间应该只有两个用户");
        }
        
        // 找到对方用户
        ChatRoomUser otherChatRoomUser = roomUsers.stream()
                .filter(ru -> !ru.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("找不到对方用户"));
        
        User otherUser = otherChatRoomUser.getUser();
        
        // 转换为UserProfileResponse, 并将avatar从object key转换为presigned url（如果需要）
        UserProfileResponse response = new UserProfileResponse();
        response.setId(otherUser.getId());
        response.setUsername(otherUser.getUsername());
        response.setNickname(otherUser.getNickname());
        String avatar = otherUser.getAvatar();
        if (avatar != null && !avatar.isEmpty() && !avatar.startsWith("http")) {
            try {
                String presigned = ossService.generatePresignedUrl(avatar, 3600L);
                response.setAvatar(presigned != null ? presigned : avatar);
            } catch (Exception ignored) {
                response.setAvatar(avatar);
            }
        } else {
            response.setAvatar(avatar);
        }
        response.setRole(otherUser.getRole().name());
        response.setCreatedAt(otherUser.getCreatedAt());
        
        return response;
    }
    
    /**
     * 查找已存在的私聊房间
     */
    private ChatRoom findExistingPrivateRoom(Long userId1, Long userId2) {
        // 查找两个用户都参与的私聊房间
        List<ChatRoomUser> user1Rooms = chatRoomUserRepository.findByUserIdAndChatRoomTypeAndVersionGreaterThan(
                userId1, ChatRoomType.PRIVATE, 0);
        List<ChatRoomUser> user2Rooms = chatRoomUserRepository.findByUserIdAndChatRoomTypeAndVersionGreaterThan(
                userId2, ChatRoomType.PRIVATE, 0);
        
        for (ChatRoomUser room1 : user1Rooms) {
            for (ChatRoomUser room2 : user2Rooms) {
                if (room1.getChatRoom().getId().equals(room2.getChatRoom().getId())) {
                    return room1.getChatRoom();
                }
            }
        }
        
        return null;
    }
    
    /**
     * 确保两个用户都在房间中
     */
    private void ensureUsersInRoom(ChatRoom room, Long userId1, Long userId2) {
        // 检查并添加用户1
        List<ChatRoomUser> user1InRoom = chatRoomUserRepository.findByChatRoomIdAndUserId(room.getId(), userId1);
        if (user1InRoom.isEmpty() || user1InRoom.get(0).getVersion() <= 0) {
            User user1 = userRepository.findById(userId1)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + userId1));
            addUserToPrivateRoom(room, user1);
        }
        
        // 检查并添加用户2
        List<ChatRoomUser> user2InRoom = chatRoomUserRepository.findByChatRoomIdAndUserId(room.getId(), userId2);
        if (user2InRoom.isEmpty() || user2InRoom.get(0).getVersion() <= 0) {
            User user2 = userRepository.findById(userId2)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + userId2));
            addUserToPrivateRoom(room, user2);
        }
    }
    
    /**
     * 添加用户到私聊房间
     */
    private void addUserToPrivateRoom(ChatRoom room, User user) {
        ChatRoomUserRequest request = new ChatRoomUserRequest();
        request.setChatRoomId(room.getId());
        request.setUserId(user.getId());
        request.setDisplayNickname(user.getNickname());
        // store display avatar as either presigned URL or original value
        String displayAvatar = user.getAvatar();
        if (displayAvatar != null && !displayAvatar.isEmpty() && !displayAvatar.startsWith("http")) {
            try {
                String presigned = ossService.generatePresignedUrl(displayAvatar, 3600L);
                request.setDisplayAvatar(presigned != null ? presigned : displayAvatar);
            } catch (Exception ignored) {
                request.setDisplayAvatar(displayAvatar);
            }
        } else {
            request.setDisplayAvatar(displayAvatar);
        }
        
        chatRoomUserService.addUserToRoom(request);
    }
    
    /**
     * 转换ChatRoom为ChatRoomResponse
     */
    private ChatRoomResponse toResponse(ChatRoom room) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setDescription(room.getDescription());
        response.setType(room.getType());
        response.setCreatedAt(room.getCreatedAt());
        return response;
    }
} 