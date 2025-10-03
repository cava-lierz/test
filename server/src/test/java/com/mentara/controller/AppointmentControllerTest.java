package com.mentara.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.mentara.repository.AppointmentRepository;
import com.mentara.service.ExpertScheduleService;
import com.mentara.dto.AppointmentDTO;
import com.mentara.dto.AppointmentRequestDTO;
import com.mentara.entity.User;
import com.mentara.entity.UserRole;
import com.mentara.service.AppointmentService;
import com.mentara.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-appointmentcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AppointmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;
    @MockBean
    private UserService userService;

    // ========== createAppointment ==========
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void createAppointment_shouldReturnOk() throws Exception {
        AppointmentDTO dto = new AppointmentDTO();
        User user = new User(); user.setId(2L); user.setUsername("user1"); user.setRole(UserRole.USER);
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.createAppointment(anyLong(), any(AppointmentRequestDTO.class))).thenReturn(dto);
        String json = "{\"expertUserId\":3,\"appointmentTime\":\"2024-01-01T10:00:00\", \"description\":\"test\", \"contactInfo\":\"test@example.com\", \"duration\":55}";
        mockMvc.perform(post("/appointments")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void createAppointment_shouldReturnBadRequestIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        String json = "{\"expertUserId\":3,\"appointmentTime\":\"2024-01-01T10:00:00\", \"description\":\"test\", \"contactInfo\":\"test@example.com\", \"duration\":55}";
        mockMvc.perform(post("/appointments")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }
    // ========== getMyAppointments ==========
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getMyAppointments_shouldReturnOk() throws Exception {
        User user = new User(); user.setId(2L); user.setUsername("user1");
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.getUserAppointments(eq(user), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/appointments/my"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getMyAppointments_shouldThrowIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/appointments/my"))
                .andExpect(status().isBadRequest());
    }
    // ========== getExpertAppointments ==========
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getExpertAppointments_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.getExpertAppointments(eq(1L), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/appointments/expert/1"))
                .andExpect(status().isOk());
    }
    // ========== getMyExpertAppointments ==========
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void getMyExpertAppointments_shouldReturnOk() throws Exception {
        User user = new User(); user.setId(3L); user.setUsername("expert1"); user.setRole(UserRole.EXPERT);
        Mockito.when(userService.findByUsername("expert1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.getExpertAppointmentsByUserId(eq(3L), any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/appointments/expert"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void getMyExpertAppointments_shouldThrowIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("expert1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/appointments/expert"))
                .andExpect(status().isBadRequest());
    }
    // ========== getAppointmentById ==========
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getAppointmentById_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.getAppointmentById(1L)).thenReturn(new AppointmentDTO());
        mockMvc.perform(get("/appointments/1"))
                .andExpect(status().isOk());
    }
    // ========== confirmAppointment ==========
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void confirmAppointment_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.confirmAppointment(eq(1L), anyString())).thenReturn(new AppointmentDTO());
        String json = "{\"reply\":\"confirmed\"}";
        mockMvc.perform(put("/appointments/1/confirm")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    // ========== rejectAppointment ==========
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void rejectAppointment_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.rejectAppointment(eq(1L), anyString())).thenReturn(new AppointmentDTO());
        String json = "{\"reply\":\"rejected\"}";
        mockMvc.perform(put("/appointments/1/reject")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    // ========== cancelAppointment ==========
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void cancelAppointment_shouldReturnOk() throws Exception {
        User user = new User(); user.setId(2L); user.setUsername("user1");
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.cancelAppointment(eq(1L), eq(2L))).thenReturn(new AppointmentDTO());
        mockMvc.perform(put("/appointments/1/cancel"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void cancelAppointment_shouldThrowIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        mockMvc.perform(put("/appointments/1/cancel"))
                .andExpect(status().isBadRequest());
    }
    // ========== completeAppointment ==========
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void completeAppointment_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.completeAppointment(1L)).thenReturn(new AppointmentDTO());
        mockMvc.perform(put("/appointments/1/complete"))
                .andExpect(status().isOk());
    }
    // ========== rateAppointment ==========
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void rateAppointment_shouldReturnOk() throws Exception {
        User user = new User(); user.setId(2L); user.setUsername("user1");
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.rateAppointment(eq(1L), eq(2L), anyString(), any())).thenReturn(new AppointmentDTO());
        String json = "{\"userRating\":\"good\",\"rating\":5}";
        mockMvc.perform(put("/appointments/1/rate")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void rateAppointment_shouldThrowIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        String json = "{\"userRating\":\"good\",\"rating\":5}";
        mockMvc.perform(put("/appointments/1/rate")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }
    // ========== getPendingAppointmentCount ==========
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPendingAppointmentCount_shouldReturnOk() throws Exception {
        Mockito.when(appointmentService.getPendingAppointmentCount(1L)).thenReturn(5L);
        mockMvc.perform(get("/appointments/expert/1/pending-count"))
                .andExpect(status().isOk());
    }
    // ========== getMyPendingAppointmentCount ==========
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void getMyPendingAppointmentCount_shouldReturnOk() throws Exception {
        User user = new User(); user.setId(3L); user.setUsername("expert1");
        Mockito.when(userService.findByUsername("expert1")).thenReturn(Optional.of(user));
        Mockito.when(appointmentService.getPendingAppointmentCountByUserId(3L)).thenReturn(2L);
        mockMvc.perform(get("/appointments/expert/pending-count"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "expert1", roles = {"EXPERT"})
    void getMyPendingAppointmentCount_shouldThrowIfUserNotFound() throws Exception {
        Mockito.when(userService.findByUsername("expert1")).thenReturn(Optional.empty());
        mockMvc.perform(get("/appointments/expert/pending-count"))
                .andExpect(status().isBadRequest());
    }
} 