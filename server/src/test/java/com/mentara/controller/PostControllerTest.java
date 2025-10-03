package com.mentara.controller;

import com.mentara.config.TestUserDetailsServiceConfig;
import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.MessageResponse;
import com.mentara.dto.response.PostResponse;
import com.mentara.enums.MoodType;
import com.mentara.security.UserPrincipal;
import com.mentara.service.PostService;
import com.mentara.service.QdrantService;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-postcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;
    @MockBean
    private QdrantService qdrantService;

    // ========== getAllPosts ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllPosts_shouldReturnOk_whenCurrentUserNull() throws Exception {
        Mockito.when(postService.findAllPosts(any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllPosts_shouldReturnOk_whenTagsProvided() throws Exception {
        Mockito.when(postService.findPostsByTags(anyList(), any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts?tags=1,2"))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllPosts_shouldReturnBadRequest_whenTagsIllegal() throws Exception {
        mockMvc.perform(get("/posts?tags=abc"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllPosts_shouldReturnOk_whenFilterProvided() throws Exception {
        Mockito.when(postService.findPostsByFilter(anyString(), any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts?filter=hot"))
                .andExpect(status().isOk());
    }
    // ========== searchPosts ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void searchPosts_shouldReturnOk_whenCurrentUserNull() throws Exception {
        Mockito.when(postService.searchPosts(anyString(), any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/search?keyword=test"))
                .andExpect(status().isOk());
    }
    // ========== getPostsByFilter ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByFilter_shouldReturnOk_whenCurrentUserNull() throws Exception {
        Mockito.when(postService.findPostsByFilter(anyString(), any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/filter/hot"))
                .andExpect(status().isOk());
    }
    // ========== getPostsByMood ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByMood_shouldReturnOk_whenMoodValid() throws Exception {
        Mockito.when(postService.findPostsByMood(any(MoodType.class), any(), isNull())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/mood/HAPPY"))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByMood_shouldReturnBadRequest_whenMoodInvalid() throws Exception {
        mockMvc.perform(get("/posts/mood/illegal"))
                .andExpect(status().isBadRequest());
    }
    // ========== getPostById ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostById_shouldReturnOk_whenCurrentUserNull() throws Exception {
        Mockito.when(postService.findById(anyLong(), isNull())).thenReturn(new PostResponse());
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk());
    }
    // ========== getMyPosts ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getMyPosts_shouldReturnOk() throws Exception {
        Mockito.when(postService.findPostsByUser(anyLong(), any(), anyLong())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/user/me"))
                .andExpect(status().isOk());
    }
    // ========== getPostsByUserId ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByUserId_shouldReturnOk() throws Exception {
        Mockito.when(postService.findPostsByUser(anyLong(), any(), anyLong())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/user/1"))
                .andExpect(status().isOk());
    }
    // ========== createPost ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createPost_shouldReturnOk() throws Exception {
        Mockito.when(postService.createPostForUser(any(PostRequest.class), anyLong())).thenReturn(new PostResponse());
        String json = "{\"title\":\"新帖\",\"content\":\"内容\",\"tagIds\":[1],\"imageUrls\":[],\"isAnnouncement\":false}";
        mockMvc.perform(post("/posts")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }
    // ========== toggleLike ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void toggleLike_shouldReturnOk_whenLiked() throws Exception {
        Mockito.when(postService.isPostLikedByUser(anyLong(), anyLong())).thenReturn(true);
        mockMvc.perform(post("/posts/1/like"))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void toggleLike_shouldReturnOk_whenNotLiked() throws Exception {
        Mockito.when(postService.isPostLikedByUser(anyLong(), anyLong())).thenReturn(false);
        mockMvc.perform(post("/posts/1/like"))
                .andExpect(status().isOk());
    }
    // ========== deletePost ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deletePost_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isOk());
    }
    // ========== 管理员功能 ==========
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void adminEndpoints_shouldReturnOk() throws Exception {
        Mockito.when(postService.getPendingAuditPosts(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        Mockito.when(postService.getNeedAdminCheckPosts(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        Mockito.when(postService.getInvalidPosts(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        Mockito.when(postService.getDeletedPosts(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/posts/admin/pending"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/posts/admin/waiting"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/posts/admin/invalid"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/posts/admin/deleted"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/posts/admin/1/approve"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/posts/admin/1/reject"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/posts/admin/1/delete"))
                .andExpect(status().isOk());
    }
    // ========== 异常与边界 ==========
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getAllPosts_shouldReturnBadRequest_whenPageParamInvalid() throws Exception {
        mockMvc.perform(get("/posts?page=-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/posts?size=0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/posts?size=101"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByMood_shouldReturnBadRequest_whenPageParamInvalid() throws Exception {
        mockMvc.perform(get("/posts/mood/HAPPY?page=-1"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPostsByFilter_shouldReturnBadRequest_whenPageParamInvalid() throws Exception {
        mockMvc.perform(get("/posts/filter/hot?page=-1"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void searchPosts_shouldReturnBadRequest_whenPageParamInvalid() throws Exception {
        mockMvc.perform(get("/posts/search?keyword=test&page=-1"))
                .andExpect(status().isBadRequest());
    }
} 