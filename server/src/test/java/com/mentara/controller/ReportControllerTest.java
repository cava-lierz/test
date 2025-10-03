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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-reportcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void reportPost_shouldReturnOk() throws Exception {
        String json = "{\"postId\":1,\"reason\":\"test reason\"}";
        mockMvc.perform(post("/reports/posts")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("举报成功"));
    }

    @Test
    void reportPost_unauthenticated_shouldReturnUnauthorized() throws Exception {
        String json = "{\"postId\":1,\"reason\":\"test reason\"}";
        mockMvc.perform(post("/reports/posts")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isUnauthorized());
    }
} 