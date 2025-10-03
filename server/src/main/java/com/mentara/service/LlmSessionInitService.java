package com.mentara.service;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.entity.User;
import com.mentara.enums.ChatType;

/**
 * LLM会话初始化服务接口
 */
public interface LlmSessionInitService {
    
    /**
     * 为新用户初始化默认的LLM会话
     * @param user 新注册的用户
     */
    void initializeDefaultSessions(User user);

    LlmMessageDocument createDefaultMessage(LlmSessionDocument session, ChatType chatType);
} 