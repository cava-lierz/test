package com.mentara.controller;

import com.mentara.entity.User;
import com.mentara.service.FileUploadService;
import com.mentara.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/avatar")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AvatarController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;
    @Autowired
    private com.mentara.service.OssService ossService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 删除旧头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                fileUploadService.deleteAvatar(user.getAvatar());
            }

            // 上传新头像，返回存储的对象key（例如: avatars/xxx.jpg）并保存到DB
            String objectKey = fileUploadService.uploadAvatar(file, user.getId());

            // 更新用户头像字段为object key
            user.setAvatar(objectKey);
            userService.save(user);

            // 生成临时可访问URL返回给前端（默认1小时）
            String presignedUrl = null;
            try {
                presignedUrl = ossService.generatePresignedUrl(objectKey, 3600L);
            } catch (Exception ignored) {}

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("avatarUrl", presignedUrl != null ? presignedUrl : objectKey);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "头像上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAvatar() {
        try {
            // 获取当前用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 删除头像文件
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                fileUploadService.deleteAvatar(user.getAvatar());
            }

            // 清除用户头像字段
            user.setAvatar(null);
            userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "头像删除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "头像删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 