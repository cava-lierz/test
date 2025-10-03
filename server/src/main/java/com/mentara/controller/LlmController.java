package com.mentara.controller;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.dto.request.LlmChatRequest;
import com.mentara.dto.request.SessionIdRequest;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import com.mentara.service.LlmChatService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * LLM历史记录控制器
 */
@RestController
@RequestMapping("/llm")
@Slf4j
public class LlmController {

    @Autowired
    private LlmChatService llmChatService;

    
    @GetMapping("/chat/fetch")
    public ResponseEntity<List<LlmSessionDocument>> fetchSessions(@CurrentUser UserPrincipal currentUser) {
        List<LlmSessionDocument> sessions = llmChatService.fetchSessions(currentUser);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/chat/messages/{sessionId}")
    public ResponseEntity<List<LlmMessageDocument>> getMessages(@PathVariable String sessionId, @CurrentUser UserPrincipal currentUser) {
        List<LlmMessageDocument> messages = llmChatService.getMessages(sessionId, currentUser);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/chat/send")
    public ResponseEntity<LlmMessageDocument> sendMessage(@Valid @RequestBody LlmChatRequest request, @CurrentUser UserPrincipal currentUser) {
        try {
            // 发送聊天消息
            LlmMessageDocument response = llmChatService.sendMessage(request.getMessage(), request.getSessionId(), currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送聊天消息失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/chat/clear")
    public ResponseEntity<List<LlmMessageDocument>> clearLlmMemory(@Valid @RequestBody SessionIdRequest sessionId, @CurrentUser UserPrincipal currentUser) {
        try {
            // 清除聊天历史
            List<LlmMessageDocument> clearedMessages = llmChatService.clearLlmMemory(sessionId.getSessionId(), currentUser);
            
            return ResponseEntity.ok(clearedMessages);
        } catch (Exception e) {
            log.error("清除聊天历史失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 