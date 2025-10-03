package com.mentara.controller;

import com.mentara.dto.response.AdminStatsResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.dto.response.ReportedPostResponse;
import com.mentara.dto.response.ReportedCommentResponse;
import com.mentara.dto.response.ChatRoomResponse;
import com.mentara.dto.response.PostResponse;
import com.mentara.dto.response.CommentResponse;
import com.mentara.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomUserService chatRoomUserService;

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        AdminStatsResponse stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取所有用户列表（用于用户管理，支持分页）
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileResponse> users = adminService.getAllUsersWithStats(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 搜索用户列表（用于用户管理，支持分页和关键词搜索）
     */
    @GetMapping("/users/search")
    public ResponseEntity<Page<UserProfileResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileResponse> users = adminService.searchUsersWithStats(keyword, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 获取用户详细信息
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserDetails(@PathVariable Long id) {
        UserProfileResponse userStats = adminService.getUserStatsById(id);
        if (userStats != null) {
            return ResponseEntity.ok(userStats);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 禁用用户
     */
    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok(java.util.Map.of("message", "用户已禁用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "禁用用户失败: " + e.getMessage()));
        }
    }

    /**
     * 启用用户
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok(java.util.Map.of("message", "用户已启用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "启用用户失败: " + e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        try {
            userService.updateUserRole(id, role);
            return ResponseEntity.ok(java.util.Map.of("message", "用户角色已更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "更新用户角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取被举报的帖子列表
     */
    @GetMapping("/reported-posts")
    public ResponseEntity<Page<ReportedPostResponse>> getReportedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportedPostResponse> reportedPosts = postService.getReportedPosts(pageable);
        return ResponseEntity.ok(reportedPosts);
    }
    
    // ================== 帖子审核相关API ==================
    
    /**
     * 获取待审核的帖子列表
     */
    @GetMapping("/pending-posts")
    public ResponseEntity<Page<PostResponse>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String state) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts;
        
        if (state != null && !state.trim().isEmpty()) {
            switch (state.toLowerCase()) {
                case "pending":
                    posts = postService.getPendingAuditPosts(pageable);
                    break;
                case "waiting":
                    posts = postService.getNeedAdminCheckPosts(pageable);
                    break;
                case "invalid":
                    posts = postService.getInvalidPosts(pageable);
                    break;
                case "deleted":
                    posts = postService.getDeletedPosts(pageable);
                    break;
                default:
                    posts = postService.getPendingAuditPosts(pageable);
                    break;
            }
        } else {
            posts = postService.getPendingAuditPosts(pageable);
        }
        
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 通过帖子审核
     */
    @PutMapping("/posts/{postId}/approve")
    public ResponseEntity<?> approvePost(@PathVariable Long postId) {
        try {
            postService.approvePost(postId);
            return ResponseEntity.ok(java.util.Map.of("message", "帖子审核已通过"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "通过审核失败: " + e.getMessage()));
        }
    }
    
    /**
     * 拒绝帖子审核
     */
    @PutMapping("/posts/{postId}/reject")
    public ResponseEntity<?> rejectPost(@PathVariable Long postId) {
        try {
            postService.rejectPost(postId);
            return ResponseEntity.ok(java.util.Map.of("message", "帖子审核已拒绝"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "拒绝审核失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除被举报的帖子（管理员操作）
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deleteReportedPost(@PathVariable Long postId) {
        try {
        postService.deletePostByAdmin(postId);
        return ResponseEntity.ok(java.util.Map.of("message", "帖子已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "删除帖子失败: " + e.getMessage()));
        }
    }
    
    /**
     * 忽略帖子举报（管理员操作）
     */
    @PutMapping("/posts/{postId}/ignore-reports")
    public ResponseEntity<?> ignorePostReports(@PathVariable Long postId) {
        postService.ignorePostReports(postId);
        return ResponseEntity.ok(java.util.Map.of("message", "举报已忽略"));
    }

    /**
     * 改变帖子状态（管理员操作）
     */
    @PutMapping("/posts/{postId}/change-status")
    public ResponseEntity<?> changePostStatus(@PathVariable Long postId, @RequestBody java.util.Map<String, String> request) {
        try {
            String status = request.get("status");
            postService.changePostStatus(postId, status);
            return ResponseEntity.ok(java.util.Map.of("message", "帖子状态已更改"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "更改帖子状态失败: " + e.getMessage()));
        }
    }

    /**
     * 通过帖子举报（管理员操作）
     */
    @PutMapping("/posts/{postId}/approve-report")
    public ResponseEntity<?> approvePostReport(@PathVariable Long postId) {
        try {
            postService.approvePostReport(postId);
            return ResponseEntity.ok(java.util.Map.of("message", "举报已通过"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "通过举报失败: " + e.getMessage()));
        }
    }

    /**
     * 获取被举报的评论列表
     */
    @GetMapping("/reported-comments")
    public ResponseEntity<Page<ReportedCommentResponse>> getReportedComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String state) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportedCommentResponse> reportedComments;
        
        if (state != null && !state.trim().isEmpty()) {
            switch (state.toLowerCase()) {
                case "waiting":
                    reportedComments = commentService.getWaitingCommentReports(pageable);
                    break;
                case "valid":
                    reportedComments = commentService.getValidCommentReports(pageable);
                    break;
                case "invalid":
                    reportedComments = commentService.getInvalidCommentReports(pageable);
                    break;
                default:
                    reportedComments = commentService.getReportedComments(pageable);
                    break;
            }
        } else {
            reportedComments = commentService.getReportedComments(pageable);
        }
        
        return ResponseEntity.ok(reportedComments);
    }
    
    /**
     * 删除被举报的评论（管理员操作）
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteReportedComment(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
        return ResponseEntity.ok(java.util.Map.of("message", "评论已删除"));
    }
    
    /**
     * 管理员删除任意评论（管理员操作）
     */
    @DeleteMapping("/comments/{commentId}/force")
    public ResponseEntity<?> forceDeleteComment(@PathVariable Long commentId) {
        commentService.forceDeleteComment(commentId);
        return ResponseEntity.ok(java.util.Map.of("message", "评论已删除"));
    }

    /**
     * 通过评论举报（管理员操作）
     */
    @PutMapping("/comments/{commentId}/approve-report")
    public ResponseEntity<?> approveCommentReport(@PathVariable Long commentId) {
        commentService.approveCommentReport(commentId);
        return ResponseEntity.ok(java.util.Map.of("message", "举报已通过"));
    }

    /**
     * 忽略评论举报（管理员操作）
     */
    @PutMapping("/comments/{commentId}/ignore-reports")
    public ResponseEntity<?> ignoreCommentReports(@PathVariable Long commentId) {
        commentService.ignoreCommentReports(commentId);
        return ResponseEntity.ok(java.util.Map.of("message", "举报已忽略"));
    }

    /**
     * 获取所有聊天室列表
     */
    @GetMapping("/chat-rooms")
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        List<ChatRoomResponse> chatRooms = chatRoomService.getAllRooms();
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 删除聊天室用户（管理员操作）
     */
    @DeleteMapping("/delete/chatRoomUserId/{chatRoomUserId}")
    @PreAuthorize("isAuthenticated()")
    public boolean deleteChatUser(
            @PathVariable Long chatRoomUserId
    ){
        try {
            chatRoomUserService.deleteUser(chatRoomUserId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除聊天室（管理员操作）
     */
    @DeleteMapping("/chat-rooms/{roomId}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Long roomId) {
        try {
            chatRoomService.deleteRoom(roomId);
            return ResponseEntity.ok(java.util.Map.of("message", "聊天室已删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "删除聊天室失败: " + e.getMessage()));
        }
    }
    
    // ================== 软删除相关API ==================
    
    /**
     * 获取已删除的帖子（管理员功能）
     */
    @GetMapping("/deleted-posts")
    public ResponseEntity<Page<PostResponse>> getDeletedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts;
        
        if (type != null && !type.trim().isEmpty()) {
            switch (type.toLowerCase()) {
                case "reported":
                    posts = postService.getReportedDeletedPosts(pageable);
                    break;
                case "audit":
                    posts = postService.getAuditDeletedPosts(pageable);
                    break;
                default:
                    posts = postService.getDeletedPosts(pageable);
                    break;
            }
        } else {
            posts = postService.getDeletedPosts(pageable);
        }
        
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 获取删除帖子统计信息
     */
    @GetMapping("/deleted-posts/stats")
    public ResponseEntity<?> getDeletedPostsStats() {
        Integer reportedCount = postService.getReportedDeletedPostsCount();
        Integer auditCount = postService.getAuditDeletedPostsCount();
        
        return ResponseEntity.ok(java.util.Map.of(
          "reportedDeletedCount", reportedCount,
            "auditDeletedCount", auditCount,
            "totalDeletedCount", reportedCount + auditCount
        ));
    }
    
    /**
     * 获取指定用户的已删除帖子
     */
    @GetMapping("/users/{userId}/deleted-posts")
    public ResponseEntity<Page<PostResponse>> getDeletedPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> deletedPosts = postService.getDeletedPostsByUser(userId, pageable);
        return ResponseEntity.ok(deletedPosts);
    }
    
    /**
     * 恢复已删除的帖子（从INVALID状态改为VALID）
     */
    @PutMapping("/posts/{postId}/restore")
    public ResponseEntity<?> restorePost(@PathVariable Long postId) {
        try {
            postService.restorePost(postId);
            return ResponseEntity.ok(java.util.Map.of("message", "帖子已恢复"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "恢复帖子失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有已删除的评论（管理员查看）
     */
    @GetMapping("/deleted-comments")
    public ResponseEntity<Page<CommentResponse>> getDeletedComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> deletedComments = commentService.getDeletedComments(pageable);
        return ResponseEntity.ok(deletedComments);
    }
    
    /**
     * 获取指定用户的已删除评论
     */
    @GetMapping("/users/{userId}/deleted-comments")
    public ResponseEntity<Page<CommentResponse>> getDeletedCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> deletedComments = commentService.getDeletedCommentsByUser(userId, pageable);
        return ResponseEntity.ok(deletedComments);
    }
    
    /**
     * 恢复已删除的评论
     */
    @PutMapping("/comments/{commentId}/restore")
    public ResponseEntity<?> restoreComment(@PathVariable Long commentId) {
        try {
            commentService.restoreComment(commentId);
            return ResponseEntity.ok(java.util.Map.of("message", "评论已恢复"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "恢复评论失败: " + e.getMessage()));
        }
    }
} 