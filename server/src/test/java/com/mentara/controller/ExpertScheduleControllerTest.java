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
@Sql(scripts = "/testdata/test-expertschedulecontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class ExpertScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void getAvailableSlots_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/1/slots"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void getDetailedSlots_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/1/detailed-slots"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void debugUserExpertInfo_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/user/1/debug"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void getExpertSchedule_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/1/schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void getAvailableSlotsByUserId_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/user/2/slots"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void getDetailedSlotsByUserId_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/user/1/detailed-slots"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void getMyExpertSchedule_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/expert-schedule/schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void updateSchedule_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/expert-schedule/1/update")
                .param("dayOffset", "0")
                .param("periodIndex", "0")
                .param("available", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void updateMySchedule_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/expert-schedule/update")
                .param("dayOffset", "0")
                .param("periodIndex", "0")
                .param("available", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "expert1", userDetailsServiceBeanName = "userDetailsService")
    void batchUpdateMySchedule_shouldReturnOk() throws Exception {
        String json = "[{\"dayOffset\":0,\"periodIndex\":0,\"available\":true}]";
        mockMvc.perform(put("/expert-schedule/batch-update")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void migrateScheduleData_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/expert-schedule/migrate-data"))
                .andExpect(status().isOk());
    }
} 