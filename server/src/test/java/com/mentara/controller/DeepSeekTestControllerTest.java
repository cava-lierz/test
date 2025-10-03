package com.mentara.controller;

import com.mentara.config.TestUserDetailsServiceConfig;
import com.mentara.dto.request.DeepSeekRequest;
import com.mentara.dto.response.DeepSeekResponse;
import com.mentara.service.DeepSeekService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import org.hamcrest.Matchers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-deepseektestcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class DeepSeekTestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeepSeekService deepSeekService;

    @Test
    void testConnection_shouldReturnOk() throws Exception {
        DeepSeekResponse.Message message = new DeepSeekResponse.Message("user", "连接成功");
        DeepSeekResponse.Choice choice = new DeepSeekResponse.Choice(0, message, "stop");
        DeepSeekResponse response = new DeepSeekResponse();
        response.setChoices(Collections.singletonList(choice));
        response.setModel("deepseek-chat");
        response.setUsage(new DeepSeekResponse.Usage(1, 1, 2));
        Mockito.when(deepSeekService.sendChatRequest(any(DeepSeekRequest.class))).thenReturn(response);

        mockMvc.perform(get("/deepseek/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("连接成功"));
    }

    @Test
    void testConnection_shouldReturnFormatError() throws Exception {
        // Case: response is null
        Mockito.when(deepSeekService.sendChatRequest(any(DeepSeekRequest.class))).thenReturn(null);
        mockMvc.perform(get("/deepseek/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("DeepSeek API响应格式异常"));
        // Case: choices is null
        DeepSeekResponse response = new DeepSeekResponse();
        response.setChoices(null);
        Mockito.when(deepSeekService.sendChatRequest(any(DeepSeekRequest.class))).thenReturn(response);
        mockMvc.perform(get("/deepseek/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        // Case: choices is empty
        response.setChoices(Collections.emptyList());
        Mockito.when(deepSeekService.sendChatRequest(any(DeepSeekRequest.class))).thenReturn(response);
        mockMvc.perform(get("/deepseek/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testConnection_shouldHandleException() throws Exception {
        Mockito.when(deepSeekService.sendChatRequest(any(DeepSeekRequest.class))).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/deepseek/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("DeepSeek API连接失败")))
                .andExpect(jsonPath("$.error").value("RuntimeException"));
    }

    @Test
    void getStatus_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/deepseek/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("DeepSeek API"));
    }

    @Test
    void simpleChat_shouldReturnOk() throws Exception {
        Mockito.when(deepSeekService.generateResponse(anyString(), anyString())).thenReturn("你好，世界");
        String json = "{\"message\":\"你好\"}";
        mockMvc.perform(post("/deepseek/chat")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reply").value("你好，世界"));
    }

    @Test
    void simpleChat_shouldReturnBadRequestForEmptyMessage() throws Exception {
        String json = "{\"message\":\"\"}";
        mockMvc.perform(post("/deepseek/chat")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    void simpleChat_shouldReturnBadRequestForMissingMessage() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/deepseek/chat")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("消息内容不能为空"));
    }

    @Test
    void simpleChat_shouldHandleException() throws Exception {
        Mockito.when(deepSeekService.generateResponse(anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        String json = "{\"message\":\"你好\"}";
        mockMvc.perform(post("/deepseek/chat")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("聊天请求失败")))
                .andExpect(jsonPath("$.error").value("RuntimeException"));
    }
} 