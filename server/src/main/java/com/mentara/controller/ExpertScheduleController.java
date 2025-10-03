package com.mentara.controller;

import com.mentara.service.ExpertScheduleService;
import com.mentara.service.UserService;
import com.mentara.service.impl.ExpertScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import com.mentara.entity.User;
import com.mentara.entity.Expert;
import com.mentara.repository.ExpertRepository;


@RestController
@RequestMapping("/expert-schedule")
@RequiredArgsConstructor
public class ExpertScheduleController {

    private final ExpertScheduleService expertScheduleService;
    private final UserService userService;
    private final ExpertRepository expertRepository;

    /**
     * 获取专家的可预约时段（公开接口，所有登录用户可访问）
     */
    @GetMapping("/{expertId}/slots")
    @PreAuthorize("isAuthenticated()")
    public boolean[][] getAvailableSlots(@PathVariable Long expertId) {
        return expertScheduleService.getAvailableSlots(expertId);
    }

    /**
     * 通过用户ID获取专家的可预约时段（公开接口，所有登录用户可访问）
     */
    @GetMapping("/user/{userId}/slots")
    @PreAuthorize("isAuthenticated()")
    public boolean[][] getAvailableSlotsByUserId(@PathVariable Long userId) {
        return expertScheduleService.getAvailableSlotsByUserId(userId);
    }

    /**
     * 获取专家的详细时间表状态（包含预约占用信息）
     */
    @GetMapping("/{expertId}/detailed-slots")
    @PreAuthorize("isAuthenticated()")
    public int[][] getDetailedSlots(@PathVariable Long expertId) {
        return expertScheduleService.getDetailedSlots(expertId);
    }

    /**
     * 通过用户ID获取专家的详细时间表状态（包含预约占用信息）
     */
    @GetMapping("/user/{userId}/detailed-slots")
    @PreAuthorize("isAuthenticated()")
    public int[][] getDetailedSlotsByUserId(@PathVariable Long userId) {
        try {
            return expertScheduleService.getDetailedSlotsByUserId(userId);
        } catch (Exception e) {
            // 如果获取详细状态失败，返回一个基础的可用状态矩阵
            // 这样前端不会完全破坏，虽然没有预约占用信息
            int[][] fallbackSlots = new int[14][8];
            for (int day = 0; day < 14; day++) {
                for (int period = 0; period < 8; period++) {
                    fallbackSlots[day][period] = 1; // 默认所有时段可用
                }
            }
            return fallbackSlots;
        }
    }

    /**
     * 测试端点：验证用户是否为专家以及关联的Expert记录
     */
    @GetMapping("/user/{userId}/debug")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> debugUserExpertInfo(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查用户是否存在
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                result.put("error", "用户不存在");
                return result;
            }
            
            User user = userOpt.get();
            result.put("userId", userId);
            result.put("username", user.getUsername());
            result.put("role", user.getRole().toString());
            result.put("isExpert", user.isExpert());
            
            // 检查是否有Expert记录
            Optional<Expert> expertOpt = expertRepository.findByUserId(userId);
            if (expertOpt.isPresent()) {
                Expert expert = expertOpt.get();
                result.put("expertId", expert.getId());
                result.put("expertName", expert.getName());
                result.put("expertSpecialty", expert.getSpecialty());
                result.put("expertStatus", expert.getStatus());
            } else {
                result.put("expertRecord", "不存在");
            }
            
            return result;
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 获取专家的排班信息（管理员或专家本人）
     */
    @GetMapping("/{expertId}/schedule")
    @PreAuthorize("hasRole('ADMIN') or @expertScheduleService.isExpertOwner(#expertId, authentication)")
    public Map<String, Object> getExpertSchedule(@PathVariable Long expertId) {
        return expertScheduleService.getExpertSchedule(expertId);
    }

    /**
     * 获取当前专家的排班信息
     */
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('EXPERT')")
    public Map<String, Object> getMyExpertSchedule(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        return expertScheduleService.getExpertScheduleByUserId(userId);
    }

    /**
     * 更新专家的排班信息（管理员或专家本人）
     */
    @PutMapping("/{expertId}/update")
    @PreAuthorize("hasRole('ADMIN') or @expertScheduleService.isExpertOwner(#expertId, authentication)")
    public boolean updateSchedule(@PathVariable Long expertId,
                                 @RequestParam int dayOffset,
                                 @RequestParam int periodIndex,
                                 @RequestParam boolean available) {
        return expertScheduleService.updateExpertAvailability(expertId, dayOffset, periodIndex, available);
    }

    /**
     * 更新当前专家用户的排班信息
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('EXPERT')")
    public boolean updateMySchedule(Authentication authentication,
                                   @RequestParam int dayOffset,
                                   @RequestParam int periodIndex,
                                   @RequestParam boolean available) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        return expertScheduleService.updateExpertAvailabilityByUserId(userId, dayOffset, periodIndex, available);
    }

    /**
     * 批量更新当前专家用户的排班信息
     */
    @PutMapping("/batch-update")
    @PreAuthorize("hasRole('EXPERT')")
    public Map<String, Object> batchUpdateMySchedule(Authentication authentication,
                                                   @RequestBody List<ScheduleUpdateRequest> updates) {
        String username = authentication.getName();
        Long userId = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                .getId();
        
        int successCount = expertScheduleService.batchUpdateExpertAvailabilityByUserId(userId, updates);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalRequests", updates.size());
        result.put("successCount", successCount);
        return result;
    }

    /**
     * 排班更新请求DTO
     */
    public static class ScheduleUpdateRequest {
        private int dayOffset;
        private int periodIndex;
        private boolean available;
        
        // Getters and setters
        public int getDayOffset() { return dayOffset; }
        public void setDayOffset(int dayOffset) { this.dayOffset = dayOffset; }
        
        public int getPeriodIndex() { return periodIndex; }
        public void setPeriodIndex(int periodIndex) { this.periodIndex = periodIndex; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    /**
     * 数据迁移端点：将旧格式数据迁移到新的状态格式
     * 只在系统升级时使用一次
     */
    @PostMapping("/migrate-data")
    @PreAuthorize("hasRole('ADMIN')")
    public String migrateScheduleData() {
        try {
            ((ExpertScheduleServiceImpl) expertScheduleService).migrateScheduleDataFormat();
            return "数据迁移完成";
        } catch (Exception e) {
            return "数据迁移失败: " + e.getMessage();
        }
    }
} 