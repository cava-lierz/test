package com.mentara.controller;

import com.mentara.config.TestUserDetailsServiceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.mentara.entity.User;
import com.mentara.security.UserPrincipal;
import com.mentara.dto.request.UpdateUserRequest;
import com.mentara.dto.request.UpdatePrivacyRequest;
import com.mentara.dto.response.UserProfileResponse;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-usercontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.mentara.service.UserService userService;
    @MockBean
    private com.mentara.service.PostService postService;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCurrentUserProfile_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateCurrentUserProfile_shouldReturnOk() throws Exception {
        String json = "{\"nickname\":\"新昵称\"}";
        mockMvc.perform(put("/users/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCurrentUserProfile_shouldReturnNotFound() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateCurrentUserProfile_shouldReturnBadRequestIfUserNotFound() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(Optional.empty());
        String json = "{\"nickname\":\"新昵称\"}";
        mockMvc.perform(put("/users/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateCurrentUserProfile_shouldUpdateAllFields() throws Exception {
        User user = new User(); user.setId(1L);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userService.updateUser(Mockito.any(User.class))).thenReturn(user);
        String json = "{\"nickname\":\"a\",\"avatar\":\"b\",\"bio\":\"c\",\"gender\":\"d\",\"age\":18}";
        mockMvc.perform(put("/users/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updateCurrentUserProfile_shouldUpdatePartialFields() throws Exception {
        User user = new User(); user.setId(1L);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userService.updateUser(Mockito.any(User.class))).thenReturn(user);
        String json = "{\"nickname\":\"a\"}";
        mockMvc.perform(put("/users/profile")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserById_shouldReturnOkOrNotFound() throws Exception {
        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCurrentUserStats_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/users/profile/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUserStatsById_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/users/2/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updatePrivacySettings_shouldReturnOk() throws Exception {
        String json = "{\"isProfilePublic\":false}";
        mockMvc.perform(put("/users/profile/privacy")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updatePrivacySettings_shouldReturnBadRequestIfUserNotFound() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(Optional.empty());
        String json = "{\"isProfilePublic\":true}";
        mockMvc.perform(put("/users/profile/privacy")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void updatePrivacySettings_shouldUpdateField() throws Exception {
        User user = new User(); user.setId(1L);
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userService.updateUser(Mockito.any(User.class))).thenReturn(user);
        String json = "{\"isProfilePublic\":false}";
        mockMvc.perform(put("/users/profile/privacy")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getUserById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/users/99999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void updateUser_shouldReturnBadRequestOnInvalidParam() throws Exception {
        String json = "{\"nickname\":\"\"}";
        mockMvc.perform(put("/users/1").contentType("application/json").content(json)).andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/1")).andExpect(status().isUnauthorized());
    }
} 