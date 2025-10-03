package com.mentara.enums;

/**
 * Post状态枚举
 */
public enum PostState {
    PENDING("待审核"),      // 刚创建，等待AI审核
    WAITING("等待人工审核"),  // AI审核后需要人工审核
    VALID("已通过"),        // 审核通过，正常显示
    INVALID("已拒绝");      // 审核不通过，不显示
    
    private final String displayName;
    
    PostState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 