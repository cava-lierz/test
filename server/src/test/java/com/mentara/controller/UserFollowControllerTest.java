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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-userfollowcontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class UserFollowControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.mentara.service.UserFollowService userFollowService;
    @MockBean
    private com.mentara.service.UserService userService;

    // TODO: 添加具体接口测试
    @Test
    void contextLoads() {
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("关注成功"));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void unfollowUser_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/user-follows/unfollow/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("取消关注成功"));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void checkFollowStatus_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user-follows/check/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowing").value(false));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getFollowedUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/user-follows/followed-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followedUsers").isArray());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getFollowers_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/user-follows/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followers").isArray());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getFollowStats_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/user-follows/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followingCount").exists())
                .andExpect(jsonPath("$.followersCount").exists());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnBadRequest_whenFollowSelf() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("不能关注自己")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnBadRequest_whenAlreadyFollowed() throws Exception {
        // 先关注一次
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isOk());
        // 再次关注
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("已经关注该用户")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void unfollowUser_shouldReturnBadRequest_whenNotFollowed() throws Exception {
        mockMvc.perform(delete("/user-follows/unfollow/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("未关注该用户")));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnBadRequest_whenUserNotExist() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":999}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void unfollowUser_shouldReturnBadRequest_whenUserNotExist() throws Exception {
        mockMvc.perform(delete("/user-follows/unfollow/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void checkFollowStatus_shouldReturnOk_whenUserNotExist() throws Exception {
        mockMvc.perform(get("/user-follows/check/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowing").value(false));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnBadRequest_whenParamInvalid() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnBadRequest_whenFollowDeletedUser() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void followUser_shouldReturnOk_whenFollowDisabledUser() throws Exception {
        mockMvc.perform(post("/user-follows/follow")
                .contentType("application/json")
                .content("{\"followedUserId\":4}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void unfollowUser_shouldReturnNotFound_whenUnfollowDeletedUser() throws Exception {
        mockMvc.perform(delete("/user-follows/unfollow/3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void checkFollowStatus_shouldReturnOk_whenCheckDeletedUser() throws Exception {
        mockMvc.perform(get("/user-follows/check/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowing").value(false));
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getFollowedUsers_shouldReturnOk_withUserDetailsVariants() throws Exception {
        // 用户ID 2: 正常用户，3: userAuth为null，4: userService.findById返回empty
        Mockito.when(userFollowService.getFollowingUserIds(1L)).thenReturn(List.of(2L, 3L, 4L));
        // user2: 正常
        User user2 = new User(); user2.setId(2L); user2.setUsername("user2"); user2.setNickname("昵称2");
        UserAuth auth2 = new UserAuth(); auth2.setEmail("user2@example.com"); user2.setUserAuth(auth2);
        Mockito.when(userService.findById(2L)).thenReturn(Optional.of(user2));
        // user3: userAuth为null
        User user3 = new User(); user3.setId(3L); user3.setUsername("user3");
        Mockito.when(userService.findById(3L)).thenReturn(Optional.of(user3));
        // user4: 不存在
        Mockito.when(userService.findById(4L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/user-follows/followed-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followedUsers").isArray())
                .andExpect(jsonPath("$.followedUsers[0].id").value(2))
                .andExpect(jsonPath("$.followedUsers[0].name").value("昵称2"))
                .andExpect(jsonPath("$.followedUsers[0].email").value("user2@example.com"))
                .andExpect(jsonPath("$.followedUsers[1].id").value(3))
                .andExpect(jsonPath("$.followedUsers[1].email").value(""));
    }
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getFollowers_shouldReturnOk_withUserDetailsVariants() throws Exception {
        Mockito.when(userFollowService.getFollowerUserIds(1L)).thenReturn(List.of(2L, 3L));
        // user2: 正常
        User user2 = new User(); user2.setId(2L); user2.setUsername("user2"); user2.setNickname("昵称2");
        UserAuth auth2 = new UserAuth(); auth2.setEmail("user2@example.com"); user2.setUserAuth(auth2);
        Mockito.when(userService.findById(2L)).thenReturn(Optional.of(user2));
        // user3: userAuth为null
        User user3 = new User(); user3.setId(3L); user3.setUsername("user3");
        Mockito.when(userService.findById(3L)).thenReturn(Optional.of(user3));
        mockMvc.perform(get("/user-follows/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followers").isArray())
                .andExpect(jsonPath("$.followers[0].id").value(2))
                .andExpect(jsonPath("$.followers[0].name").value("昵称2"))
                .andExpect(jsonPath("$.followers[0].email").value("user2@example.com"))
                .andExpect(jsonPath("$.followers[1].id").value(3))
                .andExpect(jsonPath("$.followers[1].email").value(""));
    }
} 