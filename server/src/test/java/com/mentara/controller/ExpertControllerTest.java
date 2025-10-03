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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-expertcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class ExpertControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllExperts_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/experts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getExpertUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/experts/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getExpertsBySpecialty_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/experts/specialty/心理"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void getMyExpertProfile_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/experts/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void updateExpertProfile_shouldReturnOk() throws Exception {
        String json = "{" +
                "\"name\":\"新专家\"," +
                "\"specialty\":\"新领域\"," +
                "\"contact\":\"newcontact\"}";
        mockMvc.perform(put("/experts/update")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void updateMyExpertProfile_shouldReturnOk() throws Exception {
        String json = "{" +
                "\"name\":\"新专家2\"," +
                "\"specialty\":\"新领域2\"," +
                "\"contact\":\"newcontact2\"}";
        mockMvc.perform(put("/experts/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getMyExpertProfile_shouldReturnBadRequest_whenNotExpert() throws Exception {
        mockMvc.perform(get("/experts/profile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateExpertProfile_shouldReturnBadRequest_whenNotExpert() throws Exception {
        String json = "{" +
                "\"name\":\"新专家\"," +
                "\"specialty\":\"新领域\"," +
                "\"contact\":\"newcontact\"}";
        mockMvc.perform(put("/experts/update")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateMyExpertProfile_shouldReturnBadRequest_whenNotExpert() throws Exception {
        String json = "{" +
                "\"name\":\"新专家2\"," +
                "\"specialty\":\"新领域2\"," +
                "\"contact\":\"newcontact2\"}";
        mockMvc.perform(put("/experts/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }
} 