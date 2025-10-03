package com.mentara.service;

import com.mentara.entity.ExpertSchedule;

import java.util.Map;

public interface ExpertScheduleService {
    ExpertSchedule getOrCreateSchedule(Long expertId);
    void refreshScheduleIfNeeded(Long expertId);
    boolean bookSlot(Long expertId, int dayOffset, int periodIndex);
    boolean[][] getAvailableSlots(Long expertId);
    /**
     * 通过用户ID获取专家的可预约时段
     */
    boolean[][] getAvailableSlotsByUserId(Long userId);
    
    /**
     * 获取包含预约状态的完整时间表
     * 返回三种状态：0=专家不可用, 1=可预约, 2=已被预约
     */
    int[][] getDetailedSlots(Long expertId);
    
    /**
     * 通过用户ID获取包含预约状态的完整时间表
     */
    int[][] getDetailedSlotsByUserId(Long userId);
    
    /**
     * 释放某专家某天某时段的预约（取消预约时调用）
     */
    void releaseSlot(Long expertId, int dayOffset, int periodIndex);
    /**
     * 获取专家的排班信息
     */
    Map<String, Object> getExpertSchedule(Long expertId);
    /**
     * 通过用户ID获取专家的排班信息
     */
    Map<String, Object> getExpertScheduleByUserId(Long userId);
    /**
     * 更新专家的排班偏好 (在状态0和2之间切换)
     */
    boolean updateExpertAvailability(Long expertId, int dayOffset, int periodIndex, boolean available);
    /**
     * 通过用户ID更新专家的排班偏好
     */
    boolean updateExpertAvailabilityByUserId(Long userId, int dayOffset, int periodIndex, boolean available);
    
    /**
     * 批量更新专家的排班偏好
     */
    int batchUpdateExpertAvailabilityByUserId(Long userId, java.util.List<com.mentara.controller.ExpertScheduleController.ScheduleUpdateRequest> updates);
    
    /**
     * 预约时段 (将状态从0改为1)
     */
    boolean bookSlotDirectly(Long expertId, int dayOffset, int periodIndex);
    
    /**
     * 取消预约 (将状态从1改为0) 
     */
    boolean cancelSlotDirectly(Long expertId, int dayOffset, int periodIndex);
} 