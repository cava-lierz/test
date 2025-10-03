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
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-chatroomcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class ChatRoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createRoom_shouldReturnOk() throws Exception {
        String json = "{\"name\":\"testRoom\",\"type\":\"REALNAME\"}";
        mockMvc.perform(post("/chat-room/create")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllRooms_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room/list"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRoomById_shouldReturnOkOrNotFound() throws Exception {
        mockMvc.perform(get("/chat-room/1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/chat-room/99999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void unauthenticatedAccess_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/chat-room/list"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/chat-room/create")
                .contentType("application/json")
                .content("{\"name\":\"testRoom\",\"type\":\"GROUP\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getChatRoomById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/chat-rooms/99999")).andExpect(status().isNotFound());
    }

    @Test
    void getChatRoomById_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/chat-rooms/1")).andExpect(status().isUnauthorized());
    }
} 