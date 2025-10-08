package com.mentara.controller;

import com.mentara.dto.request.FollowUserRequest;
import com.mentara.service.UserFollowService;
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
@RequestMapping("/user-follows")
@PreAuthorize("hasRole('USER') or hasRole('EXPERT') or hasRole('ADMIN')")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.mentara.service.OssService ossService;

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public ResponseEntity<?> followUser(
            @Valid @RequestBody FollowUserRequest request,
            @CurrentUser UserPrincipal currentUser) {
        try {
            userFollowService.followUser(currentUser.getId(), request.getFollowedUserId());
            return ResponseEntity.ok(Map.of("message", "关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/unfollow/{followedUserId}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long followedUserId,
            @CurrentUser UserPrincipal currentUser) {
        try {
            userFollowService.unfollowUser(currentUser.getId(), followedUserId);
            return ResponseEntity.ok(Map.of("message", "取消关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 检查是否已关注用户
     */
    @GetMapping("/check/{followedUserId}")
    public ResponseEntity<?> checkFollowStatus(
            @PathVariable Long followedUserId,
            @CurrentUser UserPrincipal currentUser) {
        boolean isFollowing = userFollowService.isUserFollowing(currentUser.getId(), followedUserId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    /**
     * 获取用户关注的所有用户列表
     */
    @GetMapping("/followed-users")
    public ResponseEntity<?> getFollowedUsers(@CurrentUser UserPrincipal currentUser) {
        List<Long> followedUserIds = userFollowService.getFollowingUserIds(currentUser.getId());
        
        // 获取用户详细信息
        List<Map<String, Object>> followedUsers = followedUserIds.stream()
            .map(userId -> {
                var userOpt = userService.findById(userId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    userMap.put("email", user.getUserAuth() != null ? user.getUserAuth().getEmail() : "");
                    String avatar = user.getAvatar();
                    String presigned = ossService.generatePresignedUrl(avatar, 3600L);
                    userMap.put("avatar", presigned);
                    userMap.put("bio", user.getBio());
                    return userMap;
                }
                return null;
            })
            .filter(user -> user != null)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("followedUsers", followedUsers));
    }

    /**
     * 获取关注用户的所有用户列表
     */
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(@CurrentUser UserPrincipal currentUser) {
        List<Long> followerUserIds = userFollowService.getFollowerUserIds(currentUser.getId());
        
        // 获取用户详细信息
        List<Map<String, Object>> followers = followerUserIds.stream()
            .map(userId -> {
                var userOpt = userService.findById(userId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    userMap.put("email", user.getUserAuth() != null ? user.getUserAuth().getEmail() : "");
                    String avatar = user.getAvatar();
                    String presigned = ossService.generatePresignedUrl(avatar, 3600L);
                    userMap.put("avatar", presigned);
                    userMap.put("bio", user.getBio());
                    return userMap;
                }
                return null;
            })
            .filter(user -> user != null)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("followers", followers));
    }

    /**
     * 获取关注统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getFollowStats(@CurrentUser UserPrincipal currentUser) {
        Long followingCount = userFollowService.countFollowing(currentUser.getId());
        Long followersCount = userFollowService.countFollowers(currentUser.getId());
        
        return ResponseEntity.ok(Map.of(
            "followingCount", followingCount,
            "followersCount", followersCount
        ));
    }
} 