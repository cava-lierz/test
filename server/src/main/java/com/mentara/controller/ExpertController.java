package com.mentara.controller;

import com.mentara.dto.ExpertUserDTO;
import com.mentara.entity.Expert;
import com.mentara.service.ExpertService;
import com.mentara.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertService expertService;
    private final UserService userService;

    /**
     * 获取所有专家列表（用户查看）
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expert>> getAllExperts() {
        List<Expert> experts = expertService.getAllExperts();
        return ResponseEntity.ok(experts);
    }

    /**
     * 获取专家用户列表（推荐使用，包含用户信息和专家详细信息）
     */
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExpertUserDTO>> getExpertUsers() {
        List<ExpertUserDTO> expertUsers = userService.getAllExpertUsers();
        return ResponseEntity.ok(expertUsers);
    }

    /**
     * 根据专业领域获取专家列表
     */
    @GetMapping("/specialty/{specialty}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expert>> getExpertsBySpecialty(@PathVariable String specialty) {
        // 暂时返回所有专家，后续可以添加按专业筛选的逻辑
        List<Expert> experts = expertService.getAllExperts();
        return ResponseEntity.ok(experts);
    }

    /**
     * 获取我的专家档案
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expert> getMyExpertProfile(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        Expert expert = expertService.getExpertByUserId(userId);
        return ResponseEntity.ok(expert);
    }

    /**
     * 更新专家信息
     */
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expert> updateExpertProfile(@RequestBody Expert expertData, 
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        
        // 获取当前专家信息
        Expert expert = expertService.getExpertByUserId(userId);
        
        // 更新专家信息
        expert.setName(expertData.getName());
        expert.setSpecialty(expertData.getSpecialty());
        expert.setContact(expertData.getContact());
        
        // 保存更新
        Expert updatedExpert = expertService.updateExpert(expert);
        return ResponseEntity.ok(expert);
    }

    /**
     * 更新我的专家档案信息
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expert> updateMyExpertProfile(@RequestBody Expert expertData, 
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        
        // 获取当前专家信息
        Expert expert = expertService.getExpertByUserId(userId);
        
        // 更新专家信息
        expert.setName(expertData.getName());
        expert.setSpecialty(expertData.getSpecialty());
        expert.setContact(expertData.getContact());
        
        // 保存更新
        Expert updatedExpert = expertService.updateExpert(expert);
        return ResponseEntity.ok(updatedExpert);
    }
} 