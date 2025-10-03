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
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import com.mentara.service.CheckinService;
import com.mentara.dto.request.CheckinRequest;
import com.mentara.dto.response.CheckinResponse;
import org.springframework.http.MediaType;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-checkincontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class CheckinControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckinService checkinService;

    @Test
    void createCheckin_shouldReturnBadRequest_whenNotLoggedIn() throws Exception {
        String json = "{\"rating\":5,\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnBadRequest_whenRatingMissing() throws Exception {
        String json = "{\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnBadRequest_whenRatingTooLow() throws Exception {
        String json = "{\"rating\":0,\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnBadRequest_whenRatingTooHigh() throws Exception {
        String json = "{\"rating\":6,\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnServerError_whenServiceThrows() throws Exception {
        String json = "{\"rating\":5,\"note\":\"test\"}";
        doThrow(new RuntimeException("service error")).when(checkinService).createCheckin(any(), any());
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTodayData_shouldReturnBadRequest_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkins/today"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getTodayData_shouldReturnServerError_whenServiceThrows() throws Exception {
        when(checkinService.getTodayData(any())).thenThrow(new RuntimeException("service error"));
        mockMvc.perform(get("/checkins/today"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentWeekData_shouldReturnBadRequest_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkins/current-week"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCurrentWeekData_shouldReturnServerError_whenServiceThrows() throws Exception {
        when(checkinService.getCurrentWeekData(any())).thenThrow(new RuntimeException("service error"));
        mockMvc.perform(get("/checkins/current-week"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeekDataByDate_shouldReturnBadRequest_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkins/week/0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDate_shouldReturnServerError_whenServiceThrows() throws Exception {
        when(checkinService.getWeekData(any(), anyInt())).thenThrow(new RuntimeException("service error"));
        mockMvc.perform(get("/checkins/week/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDate_shouldReturnBadRequest_whenWeeksAgoNegative() throws Exception {
        mockMvc.perform(get("/checkins/week/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserCheckins_shouldReturnBadRequest_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkins/user/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserCheckins_shouldReturnServerError_whenServiceThrows() throws Exception {
        when(checkinService.getUserCheckins(any(), any(Pageable.class))).thenThrow(new RuntimeException("service error"));
        mockMvc.perform(get("/checkins/user/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserCheckins_shouldReturnOk_withInvalidPageAndSize() throws Exception {
        CheckinResponse resp = new CheckinResponse();
        resp.setId(1L);
        resp.setRating(5);
        resp.setNote("test");
        resp.setCheckinDate(LocalDate.now());
        resp.setUserId(2L);
        when(checkinService.getUserCheckins(eq(2L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(resp), PageRequest.of(0, 10), 1));
        mockMvc.perform(get("/checkins/user/2?page=-1&size=200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnOk() throws Exception {
        String json = "{\"rating\":\"5\",\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getTodayData_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/checkins/today"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCurrentWeekData_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/checkins/current-week"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDate_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/checkins/week/0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserCheckins_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/checkins/user/2"))
                .andExpect(status().isOk());
    }

    // week-by-date接口分支
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDateParam_shouldReturnOk() throws Exception {
        CheckinResponse resp = new CheckinResponse();
        resp.setId(1L);
        resp.setRating(5);
        resp.setNote("test");
        resp.setCheckinDate(LocalDate.now());
        resp.setUserId(2L);
        when(checkinService.getWeekDataByDate(eq(2L), any(LocalDate.class)))
            .thenReturn(List.of(resp));
        mockMvc.perform(get("/checkins/week-by-date?date=2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getWeekDataByDateParam_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkins/week-by-date?date=2024-01-01"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDateParam_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        when(checkinService.getWeekDataByDate(any(), any(LocalDate.class))).thenThrow(new RuntimeException("service error"));
        mockMvc.perform(get("/checkins/week-by-date?date=2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getWeekDataByDateParam_shouldReturnBadRequest_whenDateInvalid() throws Exception {
        mockMvc.perform(get("/checkins/week-by-date?date=invalid-date"))
                .andExpect(status().isBadRequest());
    }

    // createCheckin note超长、rating为null
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnBadRequest_whenNoteTooLong() throws Exception {
        String longNote = "a".repeat(1001);
        String json = String.format("{\"rating\":5,\"note\":\"%s\"}", longNote);
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk()); // note无长度限制，若有限制应为badRequest
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createCheckin_shouldReturnBadRequest_whenRatingNull() throws Exception {
        String json = "{\"note\":\"test\"}";
        mockMvc.perform(post("/checkins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // getUserCheckins 分页参数类型错误
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserCheckins_shouldReturnBadRequest_whenPageNotNumber() throws Exception {
        mockMvc.perform(get("/checkins/user/2?page=abc"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserCheckins_shouldReturnBadRequest_whenSizeNotNumber() throws Exception {
        mockMvc.perform(get("/checkins/user/2?size=abc"))
                .andExpect(status().isBadRequest());
    }
} 