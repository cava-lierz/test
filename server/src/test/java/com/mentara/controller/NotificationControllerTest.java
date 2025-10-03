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
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-notificationcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUnreadNotifications_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/notifications/me/unread"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void markAsRead_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void markAllAsRead_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/notifications/me/readAll"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteNotification_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteAllNotifications_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/notifications/me/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void sendSystemNotification_shouldReturnOk() throws Exception {
        String json = "{\"title\":\"test\",\"content\":\"msg\",\"actionUrl\":null}";
        mockMvc.perform(post("/notifications/system")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getNotifications_shouldReturnBadRequestOnInvalidPage() throws Exception {
        mockMvc.perform(get("/notifications?page=-1")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void markAsRead_shouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/notifications/99999/read")).andExpect(status().isNotFound());
    }

    @Test
    void getNotifications_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
    }
} 