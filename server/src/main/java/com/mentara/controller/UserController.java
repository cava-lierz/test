package com.mentara.controller;

import com.mentara.dto.request.UpdateUserRequest;
import com.mentara.dto.request.UpdatePrivacyRequest;
import com.mentara.dto.response.MessageResponse;
import com.mentara.dto.response.UserProfileResponse;
import com.mentara.entity.User;
import com.mentara.security.UserPrincipal;
import com.mentara.service.PostService;
import com.mentara.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> user = userService.findById(userPrincipal.getId());
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUser(@Valid @RequestBody UpdateUserRequest updateRequest,
                                              Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userService.findById(userPrincipal.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (updateRequest.getNickname() != null) {
                user.setNickname(updateRequest.getNickname());
            }
            if (updateRequest.getAvatar() != null) {
                user.setAvatar(updateRequest.getAvatar());
            }
            if (updateRequest.getBio() != null) {
                user.setBio(updateRequest.getBio());
            }
            if (updateRequest.getGender() != null) {
                user.setGender(updateRequest.getGender());
            }
            if (updateRequest.getAge() != null) {
                user.setAge(updateRequest.getAge());
            }
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            User userEntity = user.get();
            
            // 如果用户不公开个人信息，且不是当前用户自己，则返回有限信息
            if (!userEntity.getIsProfilePublic() && 
                (authentication == null || !userEntity.getId().equals(((UserPrincipal) authentication.getPrincipal()).getId()))) {
                // 返回不公开信息
                return ResponseEntity.ok(new MessageResponse("该用户不公开个人信息"));
            }
            
            return ResponseEntity.ok(userEntity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/profile/stats")
    public ResponseEntity<UserProfileResponse> getCurrentUserStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserProfileResponse userStats = userService.getUserProfileStats(userPrincipal.getId());
        return ResponseEntity.ok(userStats);
    }
    
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserProfileResponse> getUserStatsById(@PathVariable Long id) {
        UserProfileResponse userStats = userService.getUserProfileStats(id);
        return ResponseEntity.ok(userStats);
    }

    @PutMapping("/profile/privacy")
    public ResponseEntity<?> updatePrivacySettings(@Valid @RequestBody UpdatePrivacyRequest privacyRequest,
                                                  Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userService.findById(userPrincipal.getId());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (privacyRequest.getIsProfilePublic() != null) {
                user.setIsProfilePublic(privacyRequest.getIsProfilePublic());
            }
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
        }
    }
}