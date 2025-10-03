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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import com.mentara.service.AuthService;
import com.mentara.dto.request.LoginRequest;
import com.mentara.dto.request.SignupRequest;
import com.mentara.dto.request.ResetPasswordRequest;
import com.mentara.dto.request.RefreshTokenRequest;
import com.mentara.dto.response.JwtResponse;
import com.mentara.dto.response.RefreshTokenResponse;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-authcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void signin_shouldReturnOkOrBadRequest() throws Exception {
        String json = "{\"username\":\"user1\",\"password\":\"testpass\"}";
        mockMvc.perform(post("/auth/signin")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void signup_shouldReturnOkOrBadRequest() throws Exception {
        String json = "{\"email\":\"newuser@example.com\",\"password\":\"newpass\",\"username\":\"newuser\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_shouldReturnOkOrBadRequest() throws Exception {
        String json = "{\"studentId\":\"user1\",\"email\":\"user1@example.com\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testJwt_shouldReturnOk() throws Exception {
        when(authService.generateTestJWT()).thenReturn("mockedToken");
        mockMvc.perform(post("/auth/test-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testToken").value("mockedToken"));
    }

    @Test
    void refresh_shouldReturnBadRequest() throws Exception {
        String json = "{\"refreshToken\":\"invalid\"}";
        when(authService.refreshToken(any())).thenThrow(new RuntimeException("token无效"));
        mockMvc.perform(post("/auth/refresh")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("token无效")));
    }

    @Test
    void signin_shouldReturnBadRequest_whenPasswordWrong() throws Exception {
        String json = "{\"username\":\"user1\",\"password\":\"wrong\"}";
        when(authService.authenticateUser(any())).thenThrow(new RuntimeException("密码错误"));
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("密码错误")));
    }

    @Test
    void signin_shouldReturnBadRequest_whenUsernameMissing() throws Exception {
        String json = "{\"password\":\"testpass\"}";
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturnBadRequest_whenUserExists() throws Exception {
        String json = "{\"email\":\"user1@example.com\",\"password\":\"testpass\",\"username\":\"user1\"}";
        doThrow(new RuntimeException("用户已存在")).when(authService).registerUser(any());
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户已存在")));
    }

    @Test
    void signup_shouldReturnBadRequest_whenEmailMissing() throws Exception {
        String json = "{\"password\":\"testpass\",\"username\":\"user1\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenPasswordNotMatch() throws Exception {
        String json = "{\"studentId\":\"user1\",\"email\":\"user1@example.com\",\"newPassword\":\"newpass\",\"confirmPassword\":\"diff\"}";
        doThrow(new RuntimeException("两次密码不一致")).when(authService).resetPassword(any());
        mockMvc.perform(post("/auth/reset-password")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("两次密码不一致")));
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenUserNotExist() throws Exception {
        String json = "{\"studentId\":\"notexist\",\"email\":\"notexist@example.com\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}";
        doThrow(new RuntimeException("用户不存在")).when(authService).resetPassword(any());
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户不存在")));
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenEmailMissing() throws Exception {
        String json = "{\"studentId\":\"user1\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testJwt_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        when(authService.generateTestJWT()).thenThrow(new RuntimeException("生成失败"));
        mockMvc.perform(post("/auth/test-jwt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("生成失败")));
    }

    @Test
    void refresh_shouldReturnBadRequest_whenServiceThrows() throws Exception {
        String json = "{\"refreshToken\":\"invalid\"}";
        when(authService.refreshToken(any())).thenThrow(new RuntimeException("token无效"));
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("token无效")));
    }

    @Test
    void refresh_shouldReturnBadRequest_whenTokenMissing() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signin_shouldReturnBadRequest_whenPasswordBlank() throws Exception {
        String json = "{\"username\":\"user1\",\"password\":\"\"}";
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturnBadRequest_whenUsernameTooShort() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"testpass\",\"username\":\"ab\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturnBadRequest_whenEmailFormatWrong() throws Exception {
        String json = "{\"email\":\"notanemail\",\"password\":\"testpass\",\"username\":\"user1\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_shouldReturnBadRequest_whenPasswordTooShort() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"123\",\"username\":\"user1\"}";
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenStudentIdBlank() throws Exception {
        String json = "{\"studentId\":\"\",\"email\":\"user1@example.com\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenEmailFormatWrong() throws Exception {
        String json = "{\"studentId\":\"user1\",\"email\":\"notanemail\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenNewPasswordTooShort() throws Exception {
        String json = "{\"studentId\":\"user1\",\"email\":\"user1@example.com\",\"newPassword\":\"123\",\"confirmPassword\":\"123\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenConfirmPasswordBlank() throws Exception {
        String json = "{\"studentId\":\"user1\",\"email\":\"user1@example.com\",\"newPassword\":\"newpass\",\"confirmPassword\":\"\"}";
        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturnBadRequest_whenTokenBlank() throws Exception {
        String json = "{\"refreshToken\":\"\"}";
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
} 