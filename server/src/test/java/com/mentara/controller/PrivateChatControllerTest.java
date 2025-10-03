package com.mentara.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Import;
import com.mentara.config.TestUserDetailsServiceConfig;
import org.springframework.security.test.context.support.WithUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-privatechatcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class PrivateChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPrivateRooms_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room/private/list"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createOrGetPrivateRoom_shouldReturnOk() throws Exception {
        String json = "{\"targetUserId\":2}";
        mockMvc.perform(post("/chat-room/private/create")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePrivateRoom_shouldReturnOk() throws Exception {
        String json = "{\"targetUserId\":2}";
        String response = mockMvc.perform(post("/chat-room/private/create")
                .contentType("application/json")
                .content(json))
                .andReturn().getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        Long chatRoomId = node.has("id") ? node.get("id").asLong() : 1L;
        mockMvc.perform(delete("/chat-room/private/" + chatRoomId))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getOtherUser_shouldReturnOk() throws Exception {
        String json = "{\"targetUserId\":2}";
        String response = mockMvc.perform(post("/chat-room/private/create")
                .contentType("application/json")
                .content(json))
                .andReturn().getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        Long chatRoomId = node.has("id") ? node.get("id").asLong() : 1L;
        mockMvc.perform(get("/chat-room/private/" + chatRoomId + "/other-user"))
                .andExpect(status().isOk());
    }
} 