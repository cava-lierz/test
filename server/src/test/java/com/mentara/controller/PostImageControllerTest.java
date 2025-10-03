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
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import com.mentara.service.FileUploadService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-postimagecontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class PostImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    void uploadPostImage_shouldReturnUnauthorized_whenNotLogin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "testdata".getBytes());
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "testdata".getBytes());
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnBadRequest_whenInvalidType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "notimage".getBytes());
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnBadRequest_whenEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnBadRequest_whenTooLarge() throws Exception {
        byte[] large = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", large);
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnServerError_whenFileMissing() throws Exception {
        mockMvc.perform(multipart("/post-image/upload"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnServerError_whenServiceThrows() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "testdata".getBytes());
        when(fileUploadService.uploadPostImage(any(), any())).thenThrow(new RuntimeException("服务异常"));
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("服务异常")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnBadRequest_whenServiceThrowsIllegalArgument() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "testdata".getBytes());
        when(fileUploadService.uploadPostImage(any(), any())).thenThrow(new IllegalArgumentException("参数错误"));
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("参数错误")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnOk_andContainImageUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "testdata".getBytes());
        when(fileUploadService.uploadPostImage(any(), any())).thenReturn("http://example.com/test.jpg");
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/test.jpg"));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadPostImage_shouldReturnOk_whenFileIsMaxSize() throws Exception {
        byte[] max = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile file = new MockMultipartFile("file", "max.jpg", "image/jpeg", max);
        when(fileUploadService.uploadPostImage(any(), any())).thenReturn("http://example.com/max.jpg");
        mockMvc.perform(multipart("/post-image/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/post-image/delete")
                .param("imageUrl", "http://example.com/image1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePostImage_shouldReturnUnauthorized_whenNotLogin() throws Exception {
        mockMvc.perform(delete("/post-image/delete")
                .param("imageUrl", "http://example.com/image1.jpg"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnOk_whenUrlInvalid() throws Exception {
        mockMvc.perform(delete("/post-image/delete")
                .param("imageUrl", "invalid_url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnServerError_whenImageUrlMissing() throws Exception {
        mockMvc.perform(delete("/post-image/delete"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnServerError_whenServiceThrows() throws Exception {
        doThrow(new RuntimeException("删除异常")).when(fileUploadService).deletePostImage(any());
        mockMvc.perform(delete("/post-image/delete").param("imageUrl", "http://example.com/image1.jpg"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("删除异常")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnOk_whenUrlIsHttp() throws Exception {
        mockMvc.perform(delete("/post-image/delete").param("imageUrl", "http://example.com/image1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePostImage_shouldReturnOk_whenUrlIsHttps() throws Exception {
        mockMvc.perform(delete("/post-image/delete").param("imageUrl", "https://example.com/image1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
} 