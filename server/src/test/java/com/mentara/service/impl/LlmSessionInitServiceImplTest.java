package com.mentara.service.impl;

import com.mentara.document.LlmMessageDocument;
import com.mentara.document.LlmSessionDocument;
import com.mentara.enums.ChatType;
import com.mentara.repository.LlmSessionRepository;
import com.mentara.repository.LlmMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LlmSessionInitServiceImplTest {
    @Mock
    private LlmSessionRepository llmSessionRepository;
    @Mock
    private LlmMessageRepository llmMessageRepository;
    @InjectMocks
    private LlmSessionInitServiceImpl llmSessionInitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDefaultMessage_shouldSaveMessage() {
        LlmSessionDocument session = new LlmSessionDocument();
        session.setId("session1");
        ChatType chatType = ChatType.friend;
        LlmMessageDocument message = new LlmMessageDocument();
        when(llmMessageRepository.save(any(LlmMessageDocument.class))).thenReturn(message);
        LlmMessageDocument result = llmSessionInitService.createDefaultMessage(session, chatType);
        assertEquals(message, result);
        verify(llmMessageRepository, times(1)).save(any(LlmMessageDocument.class));
    }
} 