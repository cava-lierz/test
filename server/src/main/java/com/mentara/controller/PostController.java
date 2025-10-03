package com.mentara.controller;

import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.MessageResponse;
import com.mentara.dto.response.PostResponse;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import com.mentara.service.PostService;
import com.mentara.enums.MoodType;
import com.mentara.service.QdrantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;

@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private QdrantService qdrantService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tags,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser == null ? null : currentUser.getId();
        
        // 如果指定了标签筛选
        if (tags != null && !tags.trim().isEmpty()) {
            List<Long> tagIds = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
            return ResponseEntity.ok(postService.findPostsByTags(tagIds, pageable, currentUserId));
        }
        
        // 如果指定了筛选条件
        if (filter != null && !filter.trim().isEmpty()) {
            return ResponseEntity.ok(postService.findPostsByFilter(filter, pageable, currentUserId));
        }
        
        // 默认返回全部帖子
        return ResponseEntity.ok(postService.findAllPosts(pageable, currentUserId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser == null ? null : currentUser.getId();

        return ResponseEntity.ok(postService.searchPosts(keyword, pageable, currentUserId));
    }

    @GetMapping("/filter/{filterType}")
    public ResponseEntity<Page<PostResponse>> getPostsByFilter(
            @PathVariable String filterType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser == null ? null : currentUser.getId();
        return ResponseEntity.ok(postService.findPostsByFilter(filterType, pageable, currentUserId));
    }

    @GetMapping("/mood/{moodType}")
    public ResponseEntity<Page<PostResponse>> getPostsByMood(
            @PathVariable String moodType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser == null ? null : currentUser.getId();
        try {
            MoodType mood = MoodType.valueOf(moodType.toUpperCase());
            return ResponseEntity.ok(postService.findPostsByMood(mood, pageable, currentUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId, @CurrentUser UserPrincipal currentUser) {
        Long currentUserId = currentUser == null ? null : currentUser.getId();
        return ResponseEntity.ok(postService.findById(postId, currentUserId));
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.findPostsByUser(currentUser.getId(), pageable, currentUser.getId()));
    }
    

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostResponse>> getPostsByUserId(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @CurrentUser UserPrincipal currentUser) {
        
        //隐私设计，一张表记录用户的隐私状态(公开，仅好友可见，私密)
        //一张表记录好友关系()
        /*
        CREATE TABLE user_relationships (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,           -- 主键，自增
        follower_id BIGINT NOT NULL,                    -- 关注者ID（主动关注的人）
        following_id BIGINT NOT NULL,                   -- 被关注者ID（被关注的人）
        status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING', -- 关系状态
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
        UNIQUE KEY unique_relationship (follower_id, following_id),      -- 唯一约束
        FOREIGN KEY (follower_id) REFERENCES users(id), -- 外键约束
        FOREIGN KEY (following_id) REFERENCES users(id) -- 外键约束
        );
         */
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.findPostsByUser(userId, pageable, currentUser.getId()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequest postRequest,
                                       @CurrentUser UserPrincipal currentUser) {

        PostResponse postResponse = postService.createPostForUser(postRequest, currentUser.getId());
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Long userId = currentUser.getId();
        boolean isLiked = postService.isPostLikedByUser(id, userId);
        if (isLiked) {
            postService.unlikePost(id, userId);
        } else {
            postService.likePost(id, userId);
        }
        return ResponseEntity.ok(new MessageResponse("操作成功"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        postService.deletePost(id, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("操作成功"));
    }

    // ================== 管理员功能 ==================
    
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PostResponse>> getPendingAuditPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> pendingPosts = postService.getPendingAuditPosts(pageable);
        return ResponseEntity.ok(pendingPosts);
    }
    
    @GetMapping("/admin/waiting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PostResponse>> getNeedAdminCheckPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> waitingPosts = postService.getNeedAdminCheckPosts(pageable);
        return ResponseEntity.ok(waitingPosts);
    }
    
    @GetMapping("/admin/invalid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PostResponse>> getInvalidPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> invalidPosts = postService.getInvalidPosts(pageable);
        return ResponseEntity.ok(invalidPosts);
    }
    
    @GetMapping("/admin/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PostResponse>> getDeletedPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> deletedPosts = postService.getDeletedPosts(pageable);
        return ResponseEntity.ok(deletedPosts);
    }
    
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approvePost(@PathVariable Long id) {
        postService.approvePost(id);
        return ResponseEntity.ok(new MessageResponse("帖子已通过审核"));
    }
    
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectPost(@PathVariable Long id) {
        postService.rejectPost(id);
        return ResponseEntity.ok(new MessageResponse("帖子已拒绝"));
    }
    
    @PostMapping("/admin/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePostByAdmin(@PathVariable Long id) {
        postService.deletePostByAdmin(id);
        return ResponseEntity.ok(new MessageResponse("帖子已删除"));
    }
}