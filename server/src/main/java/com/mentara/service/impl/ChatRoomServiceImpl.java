package com.mentara.service.impl;

import com.mentara.dto.request.ChatRoomRequest;
import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.entity.ChatRoom;
import com.mentara.enums.ChatRoomType;
import com.mentara.repository.ChatRoomRepository;
import com.mentara.service.ChatRoomService;
import com.mentara.service.ChatRoomUserService;
import com.mentara.dto.request.ChatRoomUserRequest;
import com.mentara.entity.User;
import com.mentara.repository.UserRepository;
import com.mentara.util.AvatarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatRoomUserService chatRoomUserService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ChatRoomResponse createRoom(ChatRoomRequest request, Long creatorUserId) {
        ChatRoom room = new ChatRoom();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setType(request.getType());
        ChatRoom saved = chatRoomRepository.save(room);
        // 创建者自动加入房间
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        ChatRoomUserRequest joinReq = new ChatRoomUserRequest();
        joinReq.setChatRoomId(saved.getId());
        joinReq.setUserId(creatorUserId);
        
        // 根据聊天室类型设置默认昵称和头像
        if (request.getType().name().equals("ANONYMOUS")) {
            // 匿名聊天室：使用默认匿名信息
            joinReq.setDisplayNickname("");  // 空字符串，让toResponse方法生成默认匿名昵称
            //joinReq.setDisplayAvatar("");    // 空字符串，让toResponse方法使用默认头像
            // 匿名聊天室：为创建者分配随机默认头像
            String uniqueKey = saved.getId() + "_" + creatorUserId;
            joinReq.setDisplayAvatar(AvatarUtils.generateRandomDefaultAvatar(uniqueKey));
        } else {
            // 实名聊天室：使用创建者真实信息
            joinReq.setDisplayNickname(creator.getNickname());
            joinReq.setDisplayAvatar(creator.getAvatar());
        }
        
        chatRoomUserService.addUserToRoom(joinReq);
        return toResponse(saved);
    }

    @Override
    public List<ChatRoomResponse> getAllRooms() {
        // 只返回非私聊的聊天室（REALNAME 和 ANONYMOUS）
        return chatRoomRepository.findByIsDeletedFalse().stream()
                .filter(room -> room.getType() != ChatRoomType.PRIVATE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChatRoomResponse getRoomById(Long id) {
        ChatRoom room = chatRoomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        return toResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found"));
        
        // 软删除聊天室
        room.setIsDeleted(true);
        room.setDeletedAt(LocalDateTime.now());
        chatRoomRepository.save(room);
    }

    private ChatRoomResponse toResponse(ChatRoom room) {
        ChatRoomResponse resp = new ChatRoomResponse();
        resp.setId(room.getId());
        resp.setName(room.getName());
        resp.setDescription(room.getDescription());
        resp.setType(room.getType());
        resp.setCreatedAt(room.getCreatedAt());
        return resp;
    }
} 