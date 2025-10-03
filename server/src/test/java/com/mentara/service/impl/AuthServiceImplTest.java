package com.mentara.service.impl;

import com.mentara.dto.request.LoginRequest;
import com.mentara.dto.response.JwtResponse;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import com.mentara.entity.UserRole;
import com.mentara.security.jwt.JwtUtils;
import com.mentara.service.LlmSessionInitService;
import com.mentara.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private LlmSessionInitService llmSessionInitService;
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUser_shouldReturnJwtResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testid");
        request.setPassword("pass");
        User user = new User();
        user.setId(1L);
        user.setUsername("testid");
        user.setNickname("nick");
        user.setRole(UserRole.USER);
        UserAuth userAuth = new UserAuth();
        userAuth.setPassword("encoded");
        userAuth.setEmail("test@test.com");
        user.setUserAuth(userAuth);
        when(userService.findByStudentId("testid")).thenReturn(Optional.of(user));
        when(userService.verifyPassword("pass", "encoded")).thenReturn(true);
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwt");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh");
        JwtResponse response = authService.authenticateUser(request);
        assertNotNull(response);
        assertEquals("jwt", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(1L, response.getId());
        assertEquals("testid", response.getUsername());
        assertEquals("nick", response.getNickname());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }
} 