package com.mentara.service.impl;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.repository.LlmMessageRepository;
import com.mentara.repository.LlmSessionRepository;
import com.mentara.service.DeepSeekService;
import com.mentara.service.LlmSessionInitService;
import com.mentara.util.PromptManager;
import com.mentara.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LlmChatServiceImplTest {
    @Mock
    private LlmMessageRepository llmMessageRepository;
    @Mock
    private LlmSessionRepository llmSessionRepository;
    @Mock
    private DeepSeekService deepSeekService;
    @Mock
    private LlmSessionInitService llmSessionInitService;
    @Mock
    private PromptManager promptManager;
    @Mock
    private LlmSessionInitServiceImpl llmSessionInitServiceImpl;
    @InjectMocks
    private LlmChatServiceImpl llmChatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchSessions_shouldReturnSessions() {
        UserPrincipal user = mock(UserPrincipal.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("test");
        List<LlmSessionDocument> sessions = Collections.emptyList();
        when(llmSessionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(sessions);
        List<LlmSessionDocument> result = llmChatService.fetchSessions(user);
        assertEquals(sessions, result);
    }
} 