package com.mentara.service.impl;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.dto.response.LlmJsonResponse;
import com.mentara.entity.User;
import com.mentara.enums.ChatType;
import com.mentara.repository.LlmMessageRepository;
import com.mentara.repository.LlmSessionRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.LlmChatService;
import com.mentara.service.DeepSeekService;
import com.mentara.security.UserPrincipal;
import com.mentara.service.LlmSessionInitService;
import com.mentara.util.PromptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LLM历史记录服务实现类
 */
@Slf4j
@Service
public class LlmChatServiceImpl implements LlmChatService {
    @Autowired
    private LlmMessageRepository llmMessageRepository;
    
    @Autowired
    private LlmSessionRepository llmSessionRepository;
    
    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private LlmSessionInitService llmSessionInitService;
    
    @Autowired
    private PromptManager promptManager;
    @Autowired
    private LlmSessionInitServiceImpl llmSessionInitServiceImpl;

    @Override
    public List<LlmSessionDocument> fetchSessions(UserPrincipal currentUser) {
        log.info("获取用户 {} 的所有会话", currentUser.getUsername());
        
        try {
            List<LlmSessionDocument> sessions = llmSessionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
            log.info("找到 {} 个会话", sessions.size());
            return sessions;
        } catch (Exception e) {
            log.error("获取会话列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<LlmMessageDocument> getMessages(String sessionId, UserPrincipal currentUser) {
        log.info("获取用户 {} 会话 {} 的消息历史", currentUser.getUsername(), sessionId);
        
        try {
            // 验证会话是否属于当前用户
            LlmSessionDocument session = llmSessionRepository.findByUserIdAndSessionId(currentUser.getId(), sessionId);
            if (session == null) {
                log.warn("会话 {} 不属于用户 {}", sessionId, currentUser.getUsername());
                return new ArrayList<>();
            }
            
            // 根据会话ID获取消息历史
            List<LlmMessageDocument> messages = llmMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            log.info("找到 {} 条消息记录", messages.size());
            return messages;
        } catch (Exception e) {
            log.error("获取消息历史失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public LlmMessageDocument sendMessage(String message, String sessionId, UserPrincipal currentUser) {
        log.info("用户 {} 发送消息到会话 {}", currentUser.getUsername(), sessionId);
        
        try {
            // 验证会话是否属于当前用户
            LlmSessionDocument session = llmSessionRepository.findByUserIdAndSessionId(currentUser.getId(), sessionId);
            if (session == null) {
                log.warn("会话 {} 不属于用户 {}", sessionId, currentUser.getUsername());
                throw new RuntimeException("会话不存在或不属于当前用户");
            }
            
            // 检查会话是否已被标记为敏感
            if (session.getIsSensitive()) {
                log.warn("会话 {} 已被标记为敏感，拒绝处理新消息", sessionId);
                // 返回提醒消息
                LlmMessageDocument reminderMessage = new LlmMessageDocument();
                reminderMessage.setSessionId(sessionId);
                reminderMessage.setRole("assistant");
                reminderMessage.setContent("由于检测到不当内容，此会话已被暂停。请保持文明用语，如有需要可以创建新的会话。");
                reminderMessage.setCreatedAt(LocalDateTime.now());
                return llmMessageRepository.save(reminderMessage);
            }
            
            // 创建用户消息记录
            LlmMessageDocument userMessage = new LlmMessageDocument();
            userMessage.setSessionId(sessionId);
            userMessage.setRole("user");
            userMessage.setContent(message);
            llmMessageRepository.save(userMessage);
            
            // 获取会话的所有消息用于上下文
            List<LlmMessageDocument> sessionMessages = llmMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            
            // 生成LLM响应
            String llmResponse = generateLlmResponse(sessionMessages, session.getModel(), session.getChatType());
            
            // 解析JSON响应
            LlmJsonResponse jsonResponse = deepSeekService.parseJsonResponse(llmResponse);
            
            // 检查是否检测到敏感内容
            if (jsonResponse.getSensitive()) {
                log.warn("检测到敏感内容，标记会话 {} 为敏感", sessionId);
                // 更新会话状态为敏感
                session.setIsSensitive(true);
                session.setSensitiveType("用户消息包含不当内容");
                llmSessionRepository.save(session);
                
                // 返回敏感提醒消息
                LlmMessageDocument sensitiveMessage = new LlmMessageDocument();
                sensitiveMessage.setSessionId(sessionId);
                sensitiveMessage.setRole("assistant");
                sensitiveMessage.setContent("检测到不当内容，此会话已被暂停。请保持文明用语，如有需要可以创建新的会话。");
                sensitiveMessage.setCreatedAt(LocalDateTime.now());
                return llmMessageRepository.save(sensitiveMessage);
            }
            
            // 创建正常的LLM响应记录
            LlmMessageDocument llmMessage = new LlmMessageDocument();
            llmMessage.setSessionId(sessionId);
            llmMessage.setRole("assistant");
            llmMessage.setContent(jsonResponse.getContent());
            llmMessage.setCreatedAt(LocalDateTime.now());
            
            // 保存LLM响应
            LlmMessageDocument savedLlmMessage = llmMessageRepository.save(llmMessage);
            
            log.info("LLM响应已保存，消息ID: {}", savedLlmMessage.getId());
            
            return savedLlmMessage;
        } catch (Exception e) {
            log.error("发送聊天消息失败: {}", e.getMessage());
            throw new RuntimeException("发送消息失败", e);
        }
    }

    @Override
    public List<LlmMessageDocument> clearLlmMemory(String sessionId, UserPrincipal currentUser) {
        log.info("用户 {} 清除会话 {} 的聊天历史", currentUser.getUsername(), sessionId);
        
        try {
            // 验证会话是否属于当前用户
            LlmSessionDocument session = llmSessionRepository.findByUserIdAndSessionId(currentUser.getId(), sessionId);
            if (session == null) {
                log.warn("会话 {} 不属于用户 {}", sessionId, currentUser.getUsername());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户与session不匹配");
            }
            
            // 删除指定会话的所有消息
            llmMessageRepository.deleteBySessionId(sessionId);
            
            // 重置会话的敏感状态
            session.setIsSensitive(false);
            session.setSensitiveType(null);
            llmSessionRepository.save(session);
            
            List<LlmMessageDocument> clearedMessages = new ArrayList<>();
            clearedMessages.add(llmSessionInitServiceImpl.createDefaultMessage(session, session.getChatType()));
            log.info("会话 {} 消息清除成功", sessionId);
            return clearedMessages;
        } catch (Exception e) {
            log.error("清除会话 {} 失败: {}", sessionId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "服务器清除失败");
        }
    }
    

    
    /**
     * 生成LLM响应（调用DeepSeek API）
     */
    private String generateLlmResponse(List<LlmMessageDocument> messages, String model, ChatType chatType) {
        try {
            
            // 构建对话历史
            StringBuilder conversation = new StringBuilder();
            for (LlmMessageDocument msg : messages) {
                conversation.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
            
            // 根据聊天类型构建完整的提示词
            String fullPrompt;
            if (chatType != null) {
                fullPrompt = promptManager.buildFullPrompt(chatType, conversation.toString());
                log.info("使用聊天类型: {} 的提示词", chatType);
            } else {
                // 默认使用朋友类型的提示词
                fullPrompt = promptManager.buildFullPrompt(com.mentara.enums.ChatType.friend, conversation.toString());
                log.info("使用默认朋友类型的提示词");
            }
            
            System.out.println("完整提示词: " + fullPrompt);
            
            // 调用DeepSeek API
            return deepSeekService.generateResponse(fullPrompt, model);
            
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            return "{\"content\": \"抱歉，我暂时无法回应您的问题，请稍后再试。\", \"sensitive\": false}";
        }
    }
} 