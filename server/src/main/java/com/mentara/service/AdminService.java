package com.mentara.service;

import com.mentara.dto.response.AdminStatsResponse;
import com.mentara.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    
    /**
     * 获取管理员统计数据
     */
    AdminStatsResponse getAdminStats();
    
    /**
     * 获取所有用户及其统计信息（分页）
     */
    Page<UserProfileResponse> getAllUsersWithStats(Pageable pageable);
    
    /**
     * 搜索用户及其统计信息（分页）
     */
    Page<UserProfileResponse> searchUsersWithStats(String keyword, Pageable pageable);
    
    /**
     * 根据用户ID获取用户统计信息
     */
    UserProfileResponse getUserStatsById(Long userId);
    
    /**
     * 获取本月新增用户数
     */
    long getNewUsersThisMonth();
    
    /**
     * 获取活跃用户数（最近30天有活动）
     */
    long getActiveUsersCount();
    
    /**
     * 获取用户总数
     */
    long getTotalUsersCount();
} 