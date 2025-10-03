package com.mentara.service.impl;

import com.mentara.dto.response.UserProfileResponse;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import com.mentara.entity.UserRole;
import com.mentara.repository.*;
import com.mentara.util.CryptoUtils;
import com.mentara.util.AvatarUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private ExpertRepository expertRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CheckinRepository checkinRepository;
    @Mock private CryptoUtils cryptoUtils;
    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void save_shouldCallRepository() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, userService.save(user));
    }

    @Test
    void findById_shouldReturnUser() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findById_shouldReturnEmpty() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(userService.findById(2L).isPresent());
    }

    @Test
    void findByUsername_shouldReturnUser() {
        User user = new User();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        assertTrue(userService.findByUsername("u").isPresent());
    }

    @Test
    void findByEmail_shouldReturnUser() {
        User user = new User();
        when(userRepository.findByEmail("e")).thenReturn(Optional.of(user));
        assertTrue(userService.findByEmail("e").isPresent());
    }

    @Test
    void existsByUsername_shouldReturnTrue() {
        when(userRepository.existsByUsername("u")).thenReturn(true);
        assertTrue(userService.existsByUsername("u"));
    }

    @Test
    void existsByUsernameIncludingDeleted_shouldReturnTrue() {
        when(userRepository.existsByUsernameIncludingDeleted("u")).thenReturn(true);
        assertTrue(userService.existsByUsernameIncludingDeleted("u"));
    }

    @Test
    void existsByEmail_shouldReturnTrue() {
        when(userRepository.existsByEmail("e")).thenReturn(true);
        assertTrue(userService.existsByEmail("e"));
    }

    @Test
    void updateUser_shouldCallRepository() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, userService.updateUser(user));
    }

    @Test
    void deleteUser_shouldSoftDelete() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        userService.deleteUser(1L);
        assertTrue(user.getIsDeleted());
        assertNotNull(user.getDeletedAt());
    }

    @Test
    void deleteUser_shouldThrowIfNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.deleteUser(2L));
    }

    @Test
    void updateLastLoginTime_shouldUpdateIfExists() {
        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, userService.updateLastLoginTime("u"));
        assertNotNull(user.getLastLoginAt());
    }

    @Test
    void updateLastLoginTime_shouldReturnNullIfNotFound() {
        when(userRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertNull(userService.updateLastLoginTime("u"));
    }

    @Test
    void findByStudentId_shouldHashAndQuery() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));
        assertTrue(userService.findByStudentId("sid").isPresent());
    }

    @Test
    void existsByStudentId_shouldReturnTrueIfExists() {
        UserServiceImpl spy = spy(userService);
        doReturn(Optional.of(new User())).when(spy).findByStudentId("sid");
        assertTrue(spy.existsByStudentId("sid"));
    }

    @Test
    void verifyPassword_shouldDelegateToEncoder() {
        when(passwordEncoder.matches("raw", "enc")).thenReturn(true);
        assertTrue(userService.verifyPassword("raw", "enc"));
    }

    @Test
    void createUser_shouldSaveUser() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("enc");
        User user = userService.createUser("sid", "e", "p", "nick");
        assertNotNull(user.getUsername());
        assertEquals("nick", user.getNickname());
        assertEquals("enc", user.getUserAuth().getPassword());
    }

    @Test
    void resetUserPassword_shouldUpdateIfMatch() {
        User user = new User();
        UserAuth auth = new UserAuth(); auth.setEmail("e"); user.setUserAuth(auth);
        when(passwordEncoder.encode(anyString())).thenReturn("enc");
        UserServiceImpl spy = spy(userService);
        doReturn(Optional.of(user)).when(spy).findByStudentId("sid");
        User result = spy.resetUserPassword("sid", "e", "new");
        assertEquals("enc", user.getUserAuth().getPassword());
        assertEquals(user, result);
    }

    @Test
    void resetUserPassword_shouldReturnNullIfNotMatch() {
        User user = new User();
        UserAuth auth = new UserAuth(); auth.setEmail("e1"); user.setUserAuth(auth);
        UserServiceImpl spy = spy(userService);
        doReturn(Optional.of(user)).when(spy).findByStudentId("sid");
        assertNull(spy.resetUserPassword("sid", "e2", "new"));
    }

    @Test
    void getUserProfileStats_shouldReturnProfile() {
        User user = new User(); user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.countByAuthorId(1L)).thenReturn(2);
        when(postLikeRepository.countByPostAuthorId(1L)).thenReturn(3);
        when(commentRepository.countByAuthorId(1L)).thenReturn(4);
        when(checkinRepository.findAverageRatingByUserId(1L)).thenReturn(5.0);
        UserProfileResponse resp = userService.getUserProfileStats(1L);
        assertEquals(2, resp.getPostsCount());
        assertEquals(3, resp.getTotalLikes());
        assertEquals(4, resp.getCommentsCount());
        assertEquals(5.0, resp.getAverageMoodRating());
    }

    @Test
    void getUserProfileStats_shouldThrowIfNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserProfileStats(2L));
    }

    @Test
    void disableUser_shouldSetDisabled() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.disableUser(1L);
        assertTrue(user.getIsDisabled());
    }

    @Test
    void enableUser_shouldSetEnabled() {
        User user = new User(); user.setIsDisabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.enableUser(1L);
        assertFalse(user.getIsDisabled());
    }

    @Test
    void incrementReportedCount_shouldIncrease() {
        User user = new User(); user.setReportedCount(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.incrementReportedCount(1L);
        assertEquals(2, user.getReportedCount());
    }

    @Test
    void decrementReportedCount_shouldNotBelowZero() {
        User user = new User(); user.setReportedCount(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.decrementReportedCount(1L, 2);
        assertEquals(0, user.getReportedCount());
    }

    @Test
    void searchUsers_shouldReturnPage() {
        User user = new User();
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.searchUsers(anyString(), any(Pageable.class))).thenReturn(page);
        Page<User> result = userService.searchUsers("k", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void softDeleteUser_shouldSetDeleted() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.softDeleteUser(1L);
        assertTrue(user.getIsDeleted());
    }

    @Test
    void restoreUser_shouldSetNotDeleted() {
        User user = new User(); user.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.restoreUser(1L);
        assertFalse(user.getIsDeleted());
    }

    @Test
    void isUsernameAvailable_shouldReturnTrue() {
        when(userRepository.existsByUsername("u")).thenReturn(false);
        assertTrue(userService.isUsernameAvailable("u"));
    }

    @Test
    void findByIds_shouldReturnList() {
        User user = new User();
        Set<Long> ids = new HashSet<>(); ids.add(1L);
        when(userRepository.findByIdIn(ids)).thenReturn(Collections.singletonList(user));
        List<User> result = userService.findByIds(ids);
        assertEquals(1, result.size());
    }

    @Test
    void restoreUserWithUsernameCheck_shouldSetNotDeleted() {
        User user = new User(); user.setIsDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.restoreUserWithUsernameCheck(1L);
        assertFalse(user.getIsDeleted());
    }

    @Test
    void getActiveUsersCount_shouldReturnCount() {
        when(userRepository.countActiveUsers()).thenReturn(5L);
        assertEquals(5L, userService.getActiveUsersCount());
    }

    @Test
    void getNewUsersThisMonth_shouldReturnCount() {
        when(userRepository.count()).thenReturn(6L);
        assertEquals(6L, userService.getNewUsersThisMonth());
    }

    @Test
    void updateUserRole_shouldUpdateRole() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.updateUserRole(1L, "ADMIN");
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void getAllExpertUsers_shouldReturnList() {
        User user = new User(); user.setRole(UserRole.EXPERT);
        when(userRepository.findByRoleAndIsDeletedFalse(UserRole.EXPERT)).thenReturn(Collections.singletonList(user));
        assertEquals(1, userService.getAllExpertUsers().size());
    }
} 