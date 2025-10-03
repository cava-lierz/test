package com.mentara.controller;

import com.mentara.dto.AppointmentDTO;
import com.mentara.dto.AppointmentRequestDTO;
import com.mentara.service.AppointmentService;
import com.mentara.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mentara.entity.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    /**
     * 创建预约
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> createAppointment(
            @RequestBody AppointmentRequestDTO requestDTO,
            Authentication authentication) {
        try {
            // 添加详细的认证调试信息
            System.out.println("=== AppointmentController 创建预约调试 ===");
            System.out.println("Authentication: " + authentication);
            System.out.println("Authentication.getName(): " + authentication.getName());
            System.out.println("Authentication.getPrincipal(): " + authentication.getPrincipal());
            System.out.println("Authentication.isAuthenticated(): " + authentication.isAuthenticated());
            System.out.println("RequestDTO: " + requestDTO);
            
            String username = authentication.getName();
            System.out.println("尝试查找用户名: " + username);
            
            Optional<User> userOpt = userService.findByUsername(username);
            System.out.println("用户查找结果: " + userOpt.isPresent());
            
            if (!userOpt.isPresent()) {
                System.out.println("未找到用户: " + username);
                return ResponseEntity.badRequest().build();
            }
            
            User user = userOpt.get();
            Long userId = user.getId();
            System.out.println("用户ID: " + userId);
            
            AppointmentDTO appointment = appointmentService.createAppointment(userId, requestDTO);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            System.err.println("创建预约时发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取当前用户的预约列表
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AppointmentDTO>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentDTO> appointments = appointmentService.getUserAppointments(user, pageable);
        return ResponseEntity.ok(appointments);
    }

    /**
     * 获取专家的预约列表（管理员或专家本人）
     */
    @GetMapping("/expert/{expertId}")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isExpertOwner(#expertId, authentication)")
    public ResponseEntity<Page<AppointmentDTO>> getExpertAppointments(
            @PathVariable Long expertId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentDTO> appointments = appointmentService.getExpertAppointments(expertId, pageable);
        return ResponseEntity.ok(appointments);
    }

    /**
     * 获取当前专家用户的预约列表
     */
    @GetMapping("/expert")
    @PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Page<AppointmentDTO>> getMyExpertAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentDTO> appointments = appointmentService.getExpertAppointmentsByUserId(userId, pageable);
        return ResponseEntity.ok(appointments);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 确认预约（专家操作）
     */
    @PutMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isAppointmentExpertOwner(#appointmentId, authentication)")
    public ResponseEntity<AppointmentDTO> confirmAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {
        String expertReply = request.get("reply");
        AppointmentDTO appointment = appointmentService.confirmAppointment(appointmentId, expertReply);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 拒绝预约（专家操作）
     */
    @PutMapping("/{appointmentId}/reject")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isAppointmentExpertOwner(#appointmentId, authentication)")
    public ResponseEntity<AppointmentDTO> rejectAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> request) {
        String expertReply = request.get("reply");
        AppointmentDTO appointment = appointmentService.rejectAppointment(appointmentId, expertReply);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        AppointmentDTO appointment = appointmentService.cancelAppointment(appointmentId, userId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 完成预约（专家操作）
     */
    @PutMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isAppointmentExpertOwner(#appointmentId, authentication)")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long appointmentId) {
        AppointmentDTO appointment = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 评价预约
     */
    @PutMapping("/{appointmentId}/rate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentDTO> rateAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        
        String userRating = (String) request.get("userRating");
        Integer rating = (Integer) request.get("rating");
        AppointmentDTO appointment = appointmentService.rateAppointment(appointmentId, userId, userRating, rating);
        return ResponseEntity.ok(appointment);
    }

    /**
     * 获取待处理预约数量
     */
    @GetMapping("/expert/{expertId}/pending-count")
    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isExpertOwner(#expertId, authentication)")
    public ResponseEntity<Map<String, Long>> getPendingAppointmentCount(@PathVariable Long expertId) {
        long count = appointmentService.getPendingAppointmentCount(expertId);
        Map<String, Long> response = new HashMap<>();
        response.put("pendingCount", count);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前专家用户的待处理预约数量
     */
    @GetMapping("/expert/pending-count")
    @PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Map<String, Long>> getMyPendingAppointmentCount(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        long count = appointmentService.getPendingAppointmentCountByUserId(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("pendingCount", count);
        return ResponseEntity.ok(response);
    }
} 