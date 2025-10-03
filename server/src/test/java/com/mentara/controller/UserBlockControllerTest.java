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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-userblockcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class UserBlockControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void blockUser_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-blocks/block")
                .contentType("application/json")
                .content("{\"blockedUserId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("拉黑成功"));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void unblockUser_shouldReturnOk() throws Exception {
        // 先拉黑再取消
        mockMvc.perform(post("/user-blocks/block")
                .contentType("application/json")
                .content("{\"blockedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/user-blocks/unblock/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("取消拉黑成功"));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void checkBlockStatus_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-blocks/block")
                .contentType("application/json")
                .content("{\"blockedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user-blocks/check/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlocked").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getBlockedUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-blocks/block")
                .contentType("application/json")
                .content("{\"blockedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user-blocks/blocked-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockedUsers").isArray());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getBlockers_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/user-blocks/blockers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockerUserIds").isArray());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getBlockStats_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/user-blocks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockedCount").exists())
                .andExpect(jsonPath("$.blockerCount").exists());
    }
} 