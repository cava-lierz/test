package com.mentara.service;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.security.UserPrincipal;

import java.util.List;

/**
 * LLM聊天服务接口
 */
public interface LlmChatService {
    
    /**
     * 获取用户的所有会话
     */
    List<LlmSessionDocument> fetchSessions(UserPrincipal currentUser);
    
    /**
     * 获取指定会话的消息
     */
    List<LlmMessageDocument> getMessages(String sessionId, UserPrincipal currentUser);
    
    /**
     * 发送聊天消息
     */
    LlmMessageDocument sendMessage(String message, String sessionId, UserPrincipal currentUser);
    
    /**
     * 清除会话历史
     */
    List<LlmMessageDocument> clearLlmMemory(String sessionId, UserPrincipal currentUser);
    

}