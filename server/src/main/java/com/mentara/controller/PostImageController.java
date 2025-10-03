package com.mentara.controller;


import com.mentara.service.FileUploadService;
import com.mentara.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/post-image")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PostImageController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadPostImage(
        @RequestParam("file") MultipartFile file,
        @CurrentUser UserPrincipal currentUser) {
        try {
            // 获取当前用户ID
            Long userId = currentUser == null ? null : currentUser.getId();

            // 上传帖子图片
            String imageUrl = fileUploadService.uploadPostImage(file, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片上传成功");
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "图片上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePostImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            // 删除图片文件
            fileUploadService.deletePostImage(imageUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片删除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "图片删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 