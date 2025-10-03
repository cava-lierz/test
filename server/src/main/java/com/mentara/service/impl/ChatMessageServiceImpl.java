package com.mentara.service.impl;

import com.mentara.dto.request.ChatMessageRequest;
import com.mentara.dto.response.ChatMessageResponse;
import com.mentara.entity.ChatMessage;
import com.mentara.entity.ChatRoom;
import com.mentara.entity.ChatRoomUser;
import com.mentara.repository.ChatMessageRepository;
import com.mentara.repository.ChatRoomRepository;
import com.mentara.repository.ChatRoomUserRepository;
import com.mentara.service.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoom.getId(), userId);
        if (chatRoomUsers.isEmpty()) throw new RuntimeException("User not in chat room");
        ChatRoomUser chatRoomUser = chatRoomUsers.get(0); // 使用第一个记录
        ChatMessage massage = new ChatMessage();
        massage.setChatRoom(chatRoom);
        massage.setChatRoomUser(chatRoomUser);
        massage.setContent(request.getContent());
        ChatMessage saved = chatMessageRepository.save(massage);
        ChatMessageResponse resp = toResponse(saved);
        // 消息推送到聊天室所有成员
        java.util.List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomId(chatRoom.getId());
        logger.info("[WebSocket] 推送消息到聊天室 {}，消息ID: {}，成员数: {}", chatRoom.getId(), resp.getId(), users.size());
        logger.info("[WebSocket] 广播到/topic/chat/{}，内容: {}", chatRoom.getId(), resp);
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId().toString(), resp);
        logger.info("[WebSocket] 消息推送完成，消息ID: {}", resp.getId());
        return resp;
    }

    @Override
    public List<ChatMessageResponse> getMessagesByChatRoom(Long chatRoomId, Long userId) {
        // 检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (users.isEmpty()) throw new RuntimeException("用户还未加入房间！");
        List<ChatMessage> massages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
        return massages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getMessagesByChatRoomAfterTimestamp(
            Long chatRoomId,
            Long userId,
            LocalDateTime timestamp) {

        // 检查聊天室是否存在且未删除
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));

        // 验证用户是否在聊天室
        if (chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId).isEmpty()) {
            throw new RuntimeException("用户还未加入房间！");
        }

        // 获取指定时间后的消息
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndSentAtAfterOrderBySentAtAsc(
                chatRoomId,
                timestamp
        );

        return messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getMessagesByChatRoomPaged(Long chatRoomId, Long userId, java.time.LocalDateTime before, int limit) {
        // 一次性检查聊天室和用户权限
        ChatRoom chatRoom = validateChatRoomAndUser(chatRoomId, userId);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<ChatMessage> messages;
        
        if (before != null) {
            messages = chatMessageRepository.findByChatRoomIdAndSentAtLessThanOrderBySentAtDesc(chatRoomId, before, pageable);
        } else {
            messages = chatMessageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable);
        }
        
        // 批量预加载关联对象，避免N+1查询
        preloadChatRoomUsers(messages);
        
        List<ChatMessageResponse> resp = messages.stream()
            .map(this::toResponseOptimized)
            .collect(java.util.stream.Collectors.toList());
        
        Collections.reverse(resp);
        return resp;
    }

    /**
     * 验证聊天室和用户权限，避免重复查询
     */
    private ChatRoom validateChatRoomAndUser(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(chatRoomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found or has been deleted"));
        
        if (chatRoomUserRepository.findByChatRoomIdAndUserId(chatRoomId, userId).isEmpty()) {
            throw new RuntimeException("用户还未加入房间！");
        }
        
        return chatRoom;
    }

    /**
     * 批量预加载ChatRoomUser，避免N+1查询
     */
    private void preloadChatRoomUsers(List<ChatMessage> messages) {
        if (messages.isEmpty()) return;
        
        // 收集所有需要的chatRoomUserId
        List<Long> chatRoomUserIds = messages.stream()
            .map(msg -> msg.getChatRoomUser().getId())
            .distinct()
            .collect(Collectors.toList());
        
        // 批量查询ChatRoomUser
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllById(chatRoomUserIds);
        Map<Long, ChatRoomUser> chatRoomUserMap = chatRoomUsers.stream()
            .collect(Collectors.toMap(ChatRoomUser::getId, user -> user));
        
        // 预加载到消息对象中
        messages.forEach(msg -> {
            ChatRoomUser chatRoomUser = chatRoomUserMap.get(msg.getChatRoomUser().getId());
            if (chatRoomUser != null) {
                msg.setChatRoomUser(chatRoomUser);
            }
        });
    }

    /**
     * 优化的响应转换方法，避免懒加载
     */
    private ChatMessageResponse toResponseOptimized(ChatMessage message) {
        ChatMessageResponse resp = new ChatMessageResponse();
        resp.setId(message.getId());
        resp.setChatRoomId(message.getChatRoom().getId());
        resp.setChatRoomUserId(message.getChatRoomUser().getId());
        resp.setVersion(message.getChatRoomUser().getVersion());
        resp.setContent(message.getContent());
        resp.setSentAt(message.getSentAt());
        return resp;
    }

    private ChatMessageResponse toResponse(ChatMessage massage) {
        ChatMessageResponse resp = new ChatMessageResponse();
        resp.setId(massage.getId());
        resp.setChatRoomId(massage.getChatRoom().getId());
        resp.setChatRoomUserId(massage.getChatRoomUser().getId());  // 设置用户ID
        
        /*// 处理显示昵称
        String displayNickname = massage.getChatRoomUser().getDisplayNickname();
        if (displayNickname == null || displayNickname.isEmpty()) {
            // 如果显示昵称为空，使用用户昵称
            displayNickname = massage.getChatRoomUser().getUser().getNickname();
        }
        // 如果用户已删除，显示"已删除用户"
        if (massage.getChatRoomUser().getUser().getIsDeleted() != null && massage.getChatRoomUser().getUser().getIsDeleted()) {
            displayNickname = "已删除用户";
        }
        resp.setDisplayNickname(displayNickname);*/
        
        /*// 处理显示头像
        String displayAvatar = massage.getChatRoomUser().getDisplayAvatar();
        if (displayAvatar == null || displayAvatar.isEmpty()) {
            // 如果显示头像为空，使用用户头像
            displayAvatar = massage.getChatRoomUser().getUser().getAvatar();
        }
        // 如果用户已删除，使用默认头像
        if (massage.getChatRoomUser().getUser().getIsDeleted() != null && massage.getChatRoomUser().getUser().getIsDeleted()) {
            displayAvatar = "https://i.pravatar.cc/150?u=deleted";
        }
        resp.setDisplayAvatar(displayAvatar);*/

        resp.setVersion(massage.getChatRoomUser().getVersion());
        resp.setContent(massage.getContent());
        resp.setSentAt(massage.getSentAt());
        return resp;
    }
} 