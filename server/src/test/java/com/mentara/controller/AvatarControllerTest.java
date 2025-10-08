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
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import com.mentara.service.FileUploadService;
import com.mentara.service.UserService;
import com.mentara.entity.User;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-avatarcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class AvatarControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;
    @MockBean
    private UserService userService;
    @MockBean
    private com.mentara.service.OssService ossService;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnOkOrError() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
    when(fileUploadService.uploadAvatar(any(), any())).thenReturn("avatars/avatar.png");
    when(ossService.generatePresignedUrl("avatars/avatar.png", 3600L)).thenReturn("http://signed-url/avatars/avatar.png");
        when(userService.save(any())).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteAvatar_shouldReturnOkOrError() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        user.setAvatar(null);
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.save(any())).thenReturn(user);
        mockMvc.perform(delete("/avatar/delete"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "", "image/png", new byte[0]);
        when(fileUploadService.uploadAvatar(any(), any())).thenThrow(new IllegalArgumentException("文件为空"));
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnInternalServerError_whenUserNotExist() throws Exception {
        when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadAvatar_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteAvatar_shouldReturnOk_whenNoAvatar() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        user.setAvatar(null);
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userService.save(any())).thenReturn(user);
        mockMvc.perform(delete("/avatar/delete"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAvatar_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(delete("/avatar/delete"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnBadRequest_whenFileIsNotImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "not an image".getBytes());
        when(userService.findByUsername("user1")).thenReturn(Optional.of(new User()));
        when(fileUploadService.uploadAvatar(any(), any())).thenThrow(new IllegalArgumentException("文件类型不合法"));
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldDeleteOldAvatar_whenUserHasOldAvatar() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        user.setAvatar("old_avatar.png");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
    when(fileUploadService.uploadAvatar(any(), any())).thenReturn("avatars/new_avatar.png");
    when(ossService.generatePresignedUrl("avatars/new_avatar.png", 3600L)).thenReturn("http://signed-url/avatars/new_avatar.png");
        when(userService.save(any())).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("new_avatar.png"));
        verify(fileUploadService, times(1)).deleteAvatar("old_avatar.png");
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteAvatar_shouldReturnInternalServerError_whenOssFails() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        user.setAvatar("avatar.png");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("OSS error")).when(fileUploadService).deleteAvatar(any());
        when(userService.save(any())).thenReturn(user);
        mockMvc.perform(delete("/avatar/delete"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnInternalServerError_whenOssFails() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(fileUploadService.uploadAvatar(any(), any())).thenThrow(new RuntimeException("OSS error"));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadAvatar_shouldReturnInternalServerError_whenUserServiceSaveFails() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        when(fileUploadService.uploadAvatar(any(), any())).thenReturn("avatar.png");
        when(userService.save(any())).thenThrow(new RuntimeException("DB error"));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
        mockMvc.perform(multipart("/avatar/upload").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteAvatar_shouldReturnInternalServerError_whenUserServiceSaveFails() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("user1");
        user.setAvatar("avatar.png");
        when(userService.findByUsername("user1")).thenReturn(Optional.of(user));
        doNothing().when(fileUploadService).deleteAvatar(any());
        when(userService.save(any())).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(delete("/avatar/delete"))
                .andExpect(status().isInternalServerError());
    }

    // 可选：模拟OSS服务异常（需用@MockBean注入FileUploadService并抛异常）
    // @MockBean
    // private FileUploadService fileUploadService;
    //
    // @Test
    // @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    // void uploadAvatar_shouldReturnInternalServerError_whenOssFails() throws Exception {
    //     MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "dummydata".getBytes());
    //     when(fileUploadService.uploadAvatar(any(), any())).thenThrow(new RuntimeException("OSS error"));
    //     mockMvc.perform(multipart("/avatar/upload").file(file))
    //             .andExpect(status().isInternalServerError());
    // }
} 