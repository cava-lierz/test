package com.mentara.service.impl;

import com.mentara.dto.response.AdminStatsResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.entity.User;
import com.mentara.entity.UserRole;
import com.mentara.repository.*;
import com.mentara.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CheckinRepository checkinRepository;
    @Mock private UserService userService;
    @InjectMocks private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void getAdminStats_shouldReturnCorrectStats() {
        when(userService.countUsers()).thenReturn(10L);
        when(userService.getActiveUsersCount()).thenReturn(5L);
        when(userService.getNewUsersThisMonth()).thenReturn(2L);
        when(postRepository.count()).thenReturn(20L);
        when(commentRepository.count()).thenReturn(30L);
        when(postLikeRepository.count()).thenReturn(40L);
        when(checkinRepository.findGlobalAverageRating()).thenReturn(4.5);
        when(postRepository.countByReportCountGreaterThan(0)).thenReturn(1);
        AdminStatsResponse resp = adminService.getAdminStats();
        assertEquals(10L, resp.getTotalUsers());
        assertEquals(5L, resp.getActiveUsers());
        assertEquals(2L, resp.getNewUsersThisMonth());
        assertEquals(20L, resp.getTotalPosts());
        assertEquals(30L, resp.getTotalComments());
        assertEquals(40L, resp.getTotalLikes());
        assertEquals(4.5, resp.getAverageMoodScore());
        assertEquals(1, resp.getPendingReports());
        assertEquals(0, resp.getPendingPosts());
    }

    @Test
    void getAdminStats_shouldUseDefaultMoodScoreIfNull() {
        when(userService.countUsers()).thenReturn(1L);
        when(userService.getActiveUsersCount()).thenReturn(1L);
        when(userService.getNewUsersThisMonth()).thenReturn(1L);
        when(postRepository.count()).thenReturn(1L);
        when(commentRepository.count()).thenReturn(1L);
        when(postLikeRepository.count()).thenReturn(1L);
        when(checkinRepository.findGlobalAverageRating()).thenReturn(null);
        when(postRepository.countByReportCountGreaterThan(0)).thenReturn(0);
        AdminStatsResponse resp = adminService.getAdminStats();
        assertEquals(3.8, resp.getAverageMoodScore());
    }

    @Test
    void getAllUsersWithStats_shouldReturnMappedPage() {
        User user = new User();
        user.setId(1L); user.setUsername("u"); user.setRole(UserRole.USER);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        Page<UserProfileResponse> result = adminService.getAllUsersWithStats(Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchUsersWithStats_shouldReturnMappedPage() {
        User user = new User();
        user.setId(2L); user.setUsername("u2"); user.setRole(UserRole.USER);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userService.searchUsers(anyString(), any(Pageable.class))).thenReturn(userPage);
        Page<UserProfileResponse> result = adminService.searchUsersWithStats("key", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getUserStatsById_shouldReturnProfile_whenUserExists() {
        User user = new User();
        user.setId(3L); user.setUsername("u3"); user.setRole(UserRole.USER);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        UserProfileResponse resp = adminService.getUserStatsById(3L);
        assertNotNull(resp);
        assertEquals(3L, resp.getId());
    }

    @Test
    void getUserStatsById_shouldReturnNull_whenUserNotExists() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(adminService.getUserStatsById(99L));
    }

    @Test
    void getNewUsersThisMonth_shouldDelegateToUserService() {
        when(userService.getNewUsersThisMonth()).thenReturn(7L);
        assertEquals(7L, adminService.getNewUsersThisMonth());
    }

    @Test
    void getActiveUsersCount_shouldDelegateToUserService() {
        when(userService.getActiveUsersCount()).thenReturn(8L);
        assertEquals(8L, adminService.getActiveUsersCount());
    }

    @Test
    void getTotalUsersCount_shouldDelegateToUserService() {
        when(userService.countUsers()).thenReturn(9L);
        assertEquals(9L, adminService.getTotalUsersCount());
    }

    @Test
    void convertToUserProfileResponse_shouldHandleException() {
        User user = new User();
        user.setId(4L); user.setUsername("u4"); user.setRole(UserRole.USER);
        // mock repository抛异常
        when(postRepository.countByAuthorId(anyLong())).thenThrow(new RuntimeException("fail"));
        UserProfileResponse resp = invokeConvertToUserProfileResponse(user);
        assertNotNull(resp);
        assertEquals(0, resp.getPostsCount());
        assertEquals(0, resp.getTotalLikes());
        assertEquals(0, resp.getCommentsCount());
        assertEquals(0.0, resp.getAverageMoodRating());
        assertEquals(0, resp.getReportedPostsCount());
    }

    // 反射调用private方法以100%覆盖
    private UserProfileResponse invokeConvertToUserProfileResponse(User user) {
        try {
            java.lang.reflect.Method m = AdminServiceImpl.class.getDeclaredMethod("convertToUserProfileResponse", User.class);
            m.setAccessible(true);
            return (UserProfileResponse) m.invoke(adminService, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 