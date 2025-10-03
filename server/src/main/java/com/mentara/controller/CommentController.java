package com.mentara.controller;

import com.mentara.dto.request.CommentRequest;
import com.mentara.dto.request.ReportCommentRequest;
import com.mentara.dto.response.CommentResponse;
import com.mentara.dto.response.MessageResponse;
import com.mentara.dto.response.ReportedCommentResponse;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import com.mentara.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
        @Valid @RequestBody CommentRequest commentRequest,
        @CurrentUser UserPrincipal currentUser) {
        CommentResponse commentResponse = commentService.createComment(commentRequest, currentUser.getId());
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsOfPost(
        @PathVariable Long postId, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Page<CommentResponse> commentResponses = commentService.getCommentsOfPost(postId, pageable, currentUserId);
        return ResponseEntity.ok(commentResponses);
    }

    @GetMapping("/posts/{postId}/comments/last-page")
    public ResponseEntity<Page<CommentResponse>> getLastPageCommentsOfPost(
        @PathVariable Long postId, 
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        int page = commentService.calculateLastPageOfPost(postId, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> commentResponses = commentService.getCommentsOfPost(postId, pageable, currentUserId);
        return ResponseEntity.ok(commentResponses);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId, @CurrentUser UserPrincipal currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        CommentResponse commentResponse = commentService.getComment(commentId, currentUserId);
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping("/comments/{commentId}/replies-to-top-comment")
    public ResponseEntity<Page<CommentResponse>> getRepliesOfTopComment(
        @PathVariable Long commentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Page<CommentResponse> replies = commentService.getRepliesOfTopComment(commentId, pageable, currentUserId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/comments/{commentId}/goto/{parentId}")
    public ResponseEntity<Page<CommentResponse>> getPageOfComment(
        @PathVariable Long commentId,
        @PathVariable Long parentId,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        int page = commentService.calculatePageOfReplyInTopComment(parentId, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> replies = commentService.getRepliesOfTopComment(commentId, pageable, currentUserId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/comments/{commentId}/replies-to-top-comment/last-page")
    public ResponseEntity<Page<CommentResponse>> getLastPageRepliesOfTopComment(
        @PathVariable Long commentId,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        int page = commentService.calculateLastPageOfTopComment(commentId, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> replies = commentService.getRepliesOfTopComment(commentId, pageable, currentUserId);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<Page<CommentResponse>> getRepliesOfComment(
        @PathVariable Long commentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        Page<CommentResponse> replies = commentService.getRepliesOfComment(commentId, pageable, currentUserId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/comments/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLikeComment(
        @PathVariable Long commentId, @CurrentUser UserPrincipal currentUser) {
        boolean isLiked = commentService.isCommentLikedByUser(commentId, currentUser.getId());

        if (isLiked) {
            commentService.unlikeComment(commentId, currentUser.getId());
        } else {
            commentService.likeComment(commentId, currentUser.getId());
        }
        return ResponseEntity.ok(new MessageResponse("操作成功"));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, @CurrentUser UserPrincipal currentUser) {
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("操作成功"));
    }

    @PostMapping("/comments/{commentId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCommentRequest request,
            @CurrentUser UserPrincipal currentUser) {
        commentService.reportComment(commentId, currentUser.getId(), request.getReason());
        return ResponseEntity.ok().body(java.util.Map.of("message", "举报成功"));
    }


} 