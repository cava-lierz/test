package com.mentara.controller;

import com.mentara.dto.request.ReportPostRequest;
import com.mentara.service.PostService;
import com.mentara.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private PostService postService;

    @PostMapping("/posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reportPost(
            @Valid @RequestBody ReportPostRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        postService.reportPost(request.getPostId(), currentUser.getId(), request.getReason());
        // 返回标准JSON格式
        return ResponseEntity.ok().body(Map.of("message", "举报成功"));
    }
} 