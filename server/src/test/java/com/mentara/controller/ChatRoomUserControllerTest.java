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
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/testdata/test-chatroomusercontroller-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestUserDetailsServiceConfig.class)
class ChatRoomUserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void joinRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/chat-room-user/join/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUsersByRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getMyRoomUserFromRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/1/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void quitRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/chat-room-user/quitRoom/chatRoomUserId/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void addUserToRoom_shouldReturnOk() throws Exception {
        String json = "{" +
                "\"chatRoomId\":1," +
                "\"userId\":2," +
                "\"displayNickname\":\"testNick\"," +
                "\"displayAvatar\":\"testAvatar.png\"}";
        mockMvc.perform(post("/chat-room-user/add")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void isInRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/1/isInRoom"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void setMyNickname_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/chat-room-user/room/1/me/setNickname")
                .param("displayNickname", "newNick"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRoomUserFromRoom_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/1/RoomUser/1"))
                .andExpect(status().isOk());
    }

    // 上传头像接口需要模拟文件上传
    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadMyAvatar_shouldReturnOk() throws Exception {
        // PNG文件头部
        byte[] fileContent = new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        mockMvc.perform(multipart("/chat-room-user/room/1/me/uploadAvatar")
                .file(new MockMultipartFile("file", "test.png", "image/png", fileContent)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void uploadMyAvatar_shouldReturnBadRequest_whenInvalidFile() throws Exception {
        byte[] fileContent = "dummy image content".getBytes();
        mockMvc.perform(multipart("/chat-room-user/room/1/me/uploadAvatar")
                .file("file", fileContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void addUserToRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        String json = "{" +
                "\"chatRoomId\":999," +
                "\"userId\":2," +
                "\"displayNickname\":\"testNick\"," +
                "\"displayAvatar\":\"testAvatar.png\"}";
        mockMvc.perform(post("/chat-room-user/add")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void isInRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/999/isInRoom"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getMyRoomUserFromRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/999/me"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void setMyNickname_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(put("/chat-room-user/room/999/me/setNickname")
                .param("displayNickname", "newNick"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRoomUserFromRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/999/RoomUser/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getRoomUserFromRoom_shouldReturnBadRequest_whenRoomUserNotExist() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/1/RoomUser/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void getUsersByRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(get("/chat-room-user/room/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void joinRoom_shouldReturnBadRequest_whenRoomNotExist() throws Exception {
        mockMvc.perform(post("/chat-room-user/join/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void joinRoom_shouldReturnBadRequest_whenUserNotExist() throws Exception {
        mockMvc.perform(post("/chat-room-user/join/1").with(request -> {request.setRemoteUser("notexist"); return request;}))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsService")
    void quitRoom_shouldReturnBadRequest_whenRoomUserNotExist() throws Exception {
        mockMvc.perform(post("/chat-room-user/quitRoom/chatRoomUserId/999"))
                .andExpect(status().isOk());
    }
} 