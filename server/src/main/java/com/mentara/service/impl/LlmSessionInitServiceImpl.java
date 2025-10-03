package com.mentara.service.impl;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.entity.User;
import com.mentara.enums.ChatType;
import com.mentara.repository.LlmMessageRepository;
import com.mentara.repository.LlmSessionRepository;
import com.mentara.service.LlmSessionInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM会话初始化服务实现类
 */
@Slf4j
@Service
public class LlmSessionInitServiceImpl implements LlmSessionInitService {

    @Autowired
    private LlmSessionRepository llmSessionRepository;

    @Autowired
    private LlmMessageRepository llmMessageRepository;

    // 默认欢迎消息映射
    private static final Map<ChatType, String> DEFAULT_MESSAGES = new HashMap<>();

    static {
        DEFAULT_MESSAGES.put(ChatType.expert, "你好！我是你的专业顾问，可以为你提供专业的知识和建议。请告诉我你需要什么帮助？");
        DEFAULT_MESSAGES.put(ChatType.friend, "嗨！我是你的朋友，我们可以聊聊天，分享生活中的点点滴滴。今天过得怎么样？");
        DEFAULT_MESSAGES.put(ChatType.lover, "亲爱的，我是你的恋人，永远在这里陪伴你。有什么想和我分享的吗？");
        DEFAULT_MESSAGES.put(ChatType.coach, "你好！我是你的正念心理训练教练，可以帮助你进行正念练习、情绪管理和压力缓解。准备好开始我们的正念之旅了吗？");
    }

    @Override
    public void initializeDefaultSessions(User user) {
        log.info("为用户 {} 初始化默认LLM会话", user.getUsername());

        try {
            // 为每种聊天类型创建一个会话
            for (ChatType chatType : ChatType.values()) {
                // 创建会话
                LlmSessionDocument session = createSession(user, chatType);
                
                // 创建默认消息
                createDefaultMessage(session, chatType);
                
                log.info("为用户 {} 创建了 {} 类型的会话: {}", 
                    user.getUsername(), chatType, session.getId());
            }
            
            log.info("用户 {} 的默认LLM会话初始化完成", user.getUsername());
        } catch (Exception e) {
            log.error("为用户 {} 初始化LLM会话失败: {}", user.getUsername(), e.getMessage(), e);
            // 不抛出异常，避免影响用户注册流程
        }
    }

    /**
     * 创建LLM会话
     */
    private LlmSessionDocument createSession(User user, ChatType chatType) {
        LlmSessionDocument session = new LlmSessionDocument();
        session.setUserId(user.getId());
        session.setChatType(chatType);
        session.setModel("deepseek-chat");
        session.setIsSensitive(false);
        
        return llmSessionRepository.save(session);
    }

    /**
     * 创建默认消息
     */
    public LlmMessageDocument createDefaultMessage(LlmSessionDocument session, ChatType chatType) {
        LlmMessageDocument message = new LlmMessageDocument();
        message.setSessionId(session.getId());
        message.setRole("assistant");
        message.setContent(DEFAULT_MESSAGES.get(chatType));
        
        return llmMessageRepository.save(message);
    }
} 