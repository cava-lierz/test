package com.mentara.controller;

import com.mentara.dto.request.BlockUserRequest;
import com.mentara.service.UserBlockService;
import com.mentara.service.UserService;
import com.mentara.security.CurrentUser;
import com.mentara.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-blocks")
@PreAuthorize("hasRole('USER') or hasRole('EXPERT')")
public class UserBlockController {

    @Autowired
    private UserBlockService userBlockService;

    @Autowired
    private UserService userService;

    /**
     * 拉黑用户
     */
    @PostMapping("/block")
    public ResponseEntity<?> blockUser(
            @Valid @RequestBody BlockUserRequest request,
            @CurrentUser UserPrincipal currentUser) {
        try {
            userBlockService.blockUser(currentUser.getId(), request.getBlockedUserId(), request.getReason());
            return ResponseEntity.ok(Map.of("message", "拉黑成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取消拉黑用户
     */
    @DeleteMapping("/unblock/{blockedUserId}")
    public ResponseEntity<?> unblockUser(
            @PathVariable Long blockedUserId,
            @CurrentUser UserPrincipal currentUser) {
        try {
            userBlockService.unblockUser(currentUser.getId(), blockedUserId);
            return ResponseEntity.ok(Map.of("message", "取消拉黑成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 检查是否已拉黑用户
     */
    @GetMapping("/check/{blockedUserId}")
    public ResponseEntity<?> checkBlockStatus(
            @PathVariable Long blockedUserId,
            @CurrentUser UserPrincipal currentUser) {
        boolean isBlocked = userBlockService.isUserBlocked(currentUser.getId(), blockedUserId);
        return ResponseEntity.ok(Map.of("isBlocked", isBlocked));
    }

    /**
     * 获取用户拉黑的所有用户列表
     */
    @GetMapping("/blocked-users")
    public ResponseEntity<?> getBlockedUsers(@CurrentUser UserPrincipal currentUser) {
        List<Long> blockedUserIds = userBlockService.getBlockedUserIds(currentUser.getId());
        
        // 获取用户详细信息
        List<Map<String, Object>> blockedUsers = blockedUserIds.stream()
            .map(userId -> {
                var userOpt = userService.findById(userId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    userMap.put("email", user.getUserAuth() != null ? user.getUserAuth().getEmail() : "");
                    userMap.put("avatar", user.getAvatar());
                    userMap.put("bio", user.getBio());
                    return userMap;
                }
                return null;
            })
            .filter(user -> user != null)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("blockedUsers", blockedUsers));
    }

    /**
     * 获取被用户拉黑的所有用户ID列表
     */
    @GetMapping("/blockers")
    public ResponseEntity<?> getBlockers(@CurrentUser UserPrincipal currentUser) {
        List<Long> blockerUserIds = userBlockService.getBlockerUserIds(currentUser.getId());
        return ResponseEntity.ok(Map.of("blockerUserIds", blockerUserIds));
    }

    /**
     * 获取拉黑统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getBlockStats(@CurrentUser UserPrincipal currentUser) {
        Long blockedCount = userBlockService.countBlockedUsers(currentUser.getId());
        Long blockerCount = userBlockService.countBlockers(currentUser.getId());
        
        return ResponseEntity.ok(Map.of(
            "blockedCount", blockedCount,
            "blockerCount", blockerCount
        ));
    }
} 