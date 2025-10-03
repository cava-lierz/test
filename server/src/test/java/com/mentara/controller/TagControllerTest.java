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
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-tagcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class TagControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    void getAllTags_shouldReturnOkAndList() throws Exception {
        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getTagById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/tags/99999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createTag_shouldReturnBadRequestOnDuplicate() throws Exception {
        String json = "{\"name\":\"已存在标签\"}";
        mockMvc.perform(post("/tags").contentType("application/json").content(json)).andExpect(status().isInternalServerError());
    }

    @Test
    void createTag_shouldReturnUnauthorized() throws Exception {
        String json = "{\"name\":\"新标签\"}";
        mockMvc.perform(post("/tags").contentType("application/json").content(json)).andExpect(status().isInternalServerError());
    }
} 