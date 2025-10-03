package com.mentara.controller;

import com.mentara.config.TestUserDetailsServiceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.mentara.document.LlmSessionDocument;
import com.mentara.repository.LlmSessionRepository;
import com.mentara.enums.ChatType;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-llmcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class LlmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LlmSessionRepository llmSessionRepository;

    @BeforeEach
    void setUp() {
        // 先删除，避免重复
        llmSessionRepository.deleteBySessionId("test-session");
        // 插入属于 user1 (id=2) 的 session
        LlmSessionDocument session = new LlmSessionDocument();
        session.setId("test-session");
        session.setUserId(2L); // user1 的 id
        session.setChatType(ChatType.friend); // 或你需要的类型
        session.setModel("deepseek-chat");
        session.setIsSensitive(false);
        session.setCreatedAt(LocalDateTime.now());
        llmSessionRepository.save(session);
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void fetchSessions_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/llm/chat/fetch"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getMessages_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/llm/chat/messages/test-session"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void sendMessage_shouldReturnOkOrError() throws Exception {
        String json = "{\"message\":\"hello\",\"sessionId\":\"test-session\"}";
        mockMvc.perform(post("/llm/chat/send")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void clearLlmMemory_shouldReturnOkOrError() throws Exception {
        String json = "{\"sessionId\":\"test-session\"}";
        mockMvc.perform(post("/llm/chat/clear")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
} 