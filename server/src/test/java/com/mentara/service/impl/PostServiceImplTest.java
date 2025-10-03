package com.mentara.service.impl;

import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.PostResponse;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.enums.PostState;
import com.mentara.repository.*;
import com.mentara.converter.PostConverter;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.service.*;
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

class PostServiceImplTest {
    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private UserService userService;
    @Mock private PostConverter postConverter;
    @Mock private TagService tagService;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private CommentRepository commentRepository;
    @Mock private CommentLikeRepository commentLikeRepository;
    @Mock private FileUploadService fileUploadService;
    @Mock private MoodScoreService moodScoreService;
    @Mock private QdrantService qdrantService;
    @Mock private PostReportAuditService postReportAuditService;
    @Mock private PostAuditService postAuditService;
    @InjectMocks private PostServiceImpl postService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void findById_shouldReturnPostResponse_whenValid() {
        Post post = new Post(); post.setId(1L); post.setIsDeleted(false); post.setState(PostState.VALID);
        User user = new User(); user.setId(2L);
        PostResponse resp = new PostResponse();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(true);
        when(postConverter.toResponse(post, true)).thenReturn(resp);
        PostResponse result = postService.findById(1L, 2L);
        assertEquals(resp, result);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> postService.findById(1L, null));
    }

    @Test
    void findById_shouldThrow_whenDeleted() {
        Post post = new Post(); post.setId(1L); post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThrows(ResourceNotFoundException.class, () -> postService.findById(1L, null));
    }

    @Test
    void findById_shouldThrow_whenStateNotValid() {
        Post post = new Post(); post.setId(1L); post.setIsDeleted(false); post.setState(PostState.PENDING);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        assertThrows(ResourceNotFoundException.class, () -> postService.findById(1L, null));
    }

    @Test
    void findAllPosts_shouldReturnPage() {
        Post post = new Post(); post.setId(1L);
        Page<Post> postPage = new PageImpl<>(Collections.singletonList(post));
        PostResponse resp = new PostResponse();
        when(postRepository.findValidPosts(any(Pageable.class))).thenReturn(postPage);
        when(postConverter.toResponse(any(Post.class), anyBoolean())).thenReturn(resp);
        Page<PostResponse> result = postService.findAllPosts(Pageable.unpaged(), 2L);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void createPostForUser_shouldReturnResponse() {
        PostRequest req = new PostRequest();
        User user = new User(); user.setId(2L);
        Post post = new Post(); post.setId(1L);
        PostResponse resp = new PostResponse();
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postConverter.toResponse(post, false)).thenReturn(resp);
        PostResponse result = postService.createPostForUser(req, 2L);
        assertEquals(resp, result);
    }

    @Test
    void likePost_shouldCallRepository() {
        Post post = new Post(); post.setId(1L);
        User user = new User(); user.setId(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(false);
        postService.likePost(1L, 2L);
        verify(postLikeRepository, times(1)).save(any());
    }

    @Test
    void likePost_shouldNotDuplicateLike() {
        Post post = new Post(); post.setId(1L);
        User user = new User(); user.setId(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(true);
        postService.likePost(1L, 2L);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void deletePost_shouldSoftDelete() {
        Post post = new Post(); post.setId(1L); post.setIsDeleted(false);
        User user = new User(); user.setId(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        postService.deletePost(1L, 2L);
        assertTrue(post.getIsDeleted());
    }

    @Test
    void reportPost_shouldSaveReport() {
        Post post = new Post(); post.setId(1L);
        User user = new User(); user.setId(2L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userService.findById(2L)).thenReturn(Optional.of(user));
        postService.reportPost(1L, 2L, "reason");
        verify(reportRepository, times(1)).save(any());
    }

    @Test
    void restorePost_shouldSetNotDeleted() {
        Post post = new Post(); post.setId(1L); post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        postService.restorePost(1L);
        assertFalse(post.getIsDeleted());
    }
} 