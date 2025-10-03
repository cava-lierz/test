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
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-commentcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void createComment_shouldReturnOk() throws Exception {
        String json = "{\"postId\":1,\"content\":\"test comment\"}";
        mockMvc.perform(post("/comments")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getCommentsOfPost_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/posts/1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void likeAndUnlikeComment_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/comments/1/like"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/comments/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteComment_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void reportComment_shouldReturnOk() throws Exception {
        String json = "{\"reason\":\"spam\"}";
        mockMvc.perform(post("/comments/1/report")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getLastPageCommentsOfPost_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/posts/1/comments/last-page"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getComment_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRepliesOfTopComment_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/comments/1/replies-to-top-comment"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPageOfComment_shouldReturnOk_whenHasTopComment() throws Exception {
        // id=2是有top_comment_id的子评论
        mockMvc.perform(get("/comments/3/goto/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getPageOfComment_shouldReturnBadRequest_whenParentNotExist() throws Exception {
        mockMvc.perform(get("/comments/2/goto/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getLastPageRepliesOfTopComment_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/comments/1/replies-to-top-comment/last-page"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRepliesOfComment_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/comments/1/replies"))
                .andExpect(status().isOk());
    }

    // 异常分支
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getComment_shouldReturnBadRequest_whenNotExist() throws Exception {
        mockMvc.perform(get("/comments/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void deleteComment_shouldReturnBadRequest_whenNotExist() throws Exception {
        mockMvc.perform(delete("/comments/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void likeComment_shouldReturnBadRequest_whenNotExist() throws Exception {
        mockMvc.perform(post("/comments/999/like"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "userDetailsService")
    void reportComment_shouldReturnBadRequest_whenNotExist() throws Exception {
        String json = "{\"reason\":\"spam\"}";
        mockMvc.perform(post("/comments/999/report")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void getComments_shouldReturnBadRequestOnInvalidPage() throws Exception {
        mockMvc.perform(get("/comments?postId=1&page=-1")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void addComment_shouldReturnBadRequestOnBlankContent() throws Exception {
        String json = "{\"postId\":1,\"content\":\"\"}";
        mockMvc.perform(post("/comments").contentType("application/json").content(json)).andExpect(status().isBadRequest());
    }

    @Test
    void addComment_shouldReturnBadRequest() throws Exception {
        String json = "{\"postId\":1,\"content\":\"test\"}";
        mockMvc.perform(post("/comments").contentType("application/json").content(json)).andExpect(status().isBadRequest());
    }
} 