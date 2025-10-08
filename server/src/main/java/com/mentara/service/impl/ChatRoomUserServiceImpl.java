package com.mentara.service.impl;

import com.mentara.dto.request.ChatRoomUserRequest;
import com.mentara.dto.response.ChatRoomUserResponse;
import com.mentara.entity.ChatRoom;
import com.mentara.entity.ChatRoomUser;
import com.mentara.entity.User;
import com.mentara.enums.ChatRoomType;
import com.mentara.repository.ChatRoomRepository;
import com.mentara.repository.ChatRoomUserRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.ChatRoomUserService;
import com.mentara.util.AvatarUtils;
import com.mentara.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.mentara.enums.ChatRoomType.ANONYMOUS;

@Service
public class ChatRoomUserServiceImpl implements ChatRoomUserService {
    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OssService ossService;

    @Override
    public void save(ChatRoomUser chatRoomUser){
        chatRoomUserRepository.save(chatRoomUser);
    }

    @Override
    public void deleteUser(Long id){
        ChatRoomUser user = chatRoomUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setVersion(0);
        save(user);
    }

    @Override
    public void quitRoom(Long id){
        ChatRoomUser user = chatRoomUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setVersion(-1);
        save(user);
    }

    @Override
    public ChatRoomUser getChatRoomUserByByChatRoomIdAndUserId(Long chatRoomId, Long userId){
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found in chat room");
        }

        return users.get(0);
    }

    @Override
    public ChatRoomUserResponse addUserToRoom(ChatRoomUserRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 检查用户是否已经在聊天室中
        List<ChatRoomUser> existingUsers = chatRoomUserRepository.findByChatRoomIdAndUserId(request.getChatRoomId(), request.getUserId());
        if (!existingUsers.isEmpty()) {
            // 如果用户已存在，返回第一个记录（清理重复记录）
            ChatRoomUser existingUser = existingUsers.get(0);
            if(existingUser.getVersion() == -1){
                existingUser.setVersion(1);
                save(existingUser);
            }
            // 删除重复记录
            for (int i = 1; i < existingUsers.size(); i++) {
                chatRoomUserRepository.delete(existingUsers.get(i));
            }
            return toResponse(existingUser);
        }


        
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setChatRoom(chatRoom);
        chatRoomUser.setUser(user);
        chatRoomUser.setDisplayNickname(request.getDisplayNickname());
        
        // 为匿名聊天室用户分配随机默认头像
        String displayAvatar = request.getDisplayAvatar();
        if (chatRoom.getType() == ChatRoomType.ANONYMOUS) {
            // 匿名聊天室：为每个用户分配随机默认头像
            String uniqueKey = chatRoom.getId() + "_" + user.getId();
            displayAvatar = AvatarUtils.generateRandomDefaultAvatar(uniqueKey);
        }
        chatRoomUser.setDisplayAvatar(displayAvatar);
        
        chatRoomUser.setVersion(1);
        ChatRoomUser saved = chatRoomUserRepository.save(chatRoomUser);
        return toResponse(saved);
    }

    @Override
    public ChatRoomUserResponse getUserByRoomAndId(Long chatRoomId, Long id){
        // 先检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        ChatRoomUser user = chatRoomUserRepository.findByChatRoomIdAndId(chatRoomId, id);
        return toResponse(user);
    }

    @Override
    public boolean isInRoom(Long chatRoomId, Long userId){
        // 先检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        boolean exists = !users.isEmpty();
        if(exists){
            return users.get(0).getVersion() > 0;
        }
        return false;
    }

    @Override
    public ChatRoomUserResponse getUserFromRoom(Long chatRoomId, Long userId){
        // 先检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found in chat room");
        }
        // 返回第一个记录
        return toResponse(users.get(0));
    }

    @Override
    public List<ChatRoomUserResponse> getUsersByRoom(Long chatRoomId) {
        // 先检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomId(chatRoomId);
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ChatRoomUserResponse joinRoom(Long chatRoomId, Long userId) {
        // 获取聊天室信息
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        
        ChatRoomUserRequest req = new ChatRoomUserRequest();
        req.setChatRoomId(chatRoomId);
        req.setUserId(userId);
        
        // 根据聊天室类型设置默认昵称和头像
        if (chatRoom.getType().name().equals("ANONYMOUS")) {
            // 匿名聊天室：使用默认匿名信息
            req.setDisplayNickname("");  // 空字符串，让toResponse方法生成默认匿名昵称
            //req.setDisplayAvatar("");    // 空字符串，让toResponse方法使用默认头像
            String uniqueKey = chatRoomId + "_" + userId;
            req.setDisplayAvatar(AvatarUtils.generateRandomDefaultAvatar(uniqueKey));
        } else {
            // 实名聊天室：使用用户真实信息
            req.setDisplayNickname(user.getNickname());
            req.setDisplayAvatar(user.getAvatar());
        }
        
        return addUserToRoom(req);
    }

    private ChatRoomUserResponse toResponse(ChatRoomUser chatRoomUser) {
        ChatRoomUserResponse resp = new ChatRoomUserResponse();
        resp.setChatRoomId(chatRoomUser.getChatRoom().getId());
        resp.setChatRoomUserId(chatRoomUser.getId());  // 设置房间用户ID
        resp.setVersion(chatRoomUser.getVersion());
        
        // 获取聊天室类型
        boolean isAnonymousRoom = chatRoomUser.getChatRoom().getType().name().equals("ANONYMOUS");
        
        // 处理显示昵称
        String displayNickname = chatRoomUser.getDisplayNickname();
        if (displayNickname == null || displayNickname.isEmpty()) {
            if (isAnonymousRoom) {
                // 匿名聊天室：如果没有设置昵称，使用默认匿名昵称
                displayNickname = "匿名用户" + chatRoomUser.getId();
            } else {
                // 实名聊天室：使用用户昵称
                displayNickname = chatRoomUser.getUser().getNickname();
            }
        }
        // 如果用户已删除，显示"已删除用户"
        if (chatRoomUser.getUser().getIsDeleted() != null && chatRoomUser.getUser().getIsDeleted()) {
            displayNickname = "已删除用户";
        }
        resp.setDisplayNickname(displayNickname);
        
        // 处理显示头像
        String displayAvatar = chatRoomUser.getDisplayAvatar();
        if (displayAvatar == null || displayAvatar.isEmpty()) {
            // 如果显示头像为空，根据聊天室类型处理
            if (chatRoomUser.getChatRoom().getType() == ChatRoomType.ANONYMOUS) {
                // 匿名聊天室：生成随机默认头像
                String uniqueKey = chatRoomUser.getChatRoom().getId() + "_" + chatRoomUser.getUser().getId();
                displayAvatar = AvatarUtils.generateRandomDefaultAvatar(uniqueKey);
            } else {
                // 实名聊天室：使用用户头像
                displayAvatar = chatRoomUser.getUser().getAvatar();
            }
        }
        // 如果用户已删除，使用默认头像
        if (chatRoomUser.getUser().getIsDeleted() != null && chatRoomUser.getUser().getIsDeleted()) {
            displayAvatar = AvatarUtils.getDeletedUserAvatar();
        }
        // 如果头像仍然为空，使用随机默认头像
        if (displayAvatar == null || displayAvatar.isEmpty()) {
            String uniqueKey = chatRoomUser.getChatRoom().getId() + "_" + chatRoomUser.getUser().getId();
            displayAvatar = AvatarUtils.generateRandomDefaultAvatar(uniqueKey);
        }
        // 如果 displayAvatar 看起来像 object key，则转换为临时URL
        if (displayAvatar != null) {
            displayAvatar = ossService.generatePresignedUrl(displayAvatar, 3600L);
        }
        resp.setDisplayAvatar(displayAvatar);
        
        return resp;
    }
} 