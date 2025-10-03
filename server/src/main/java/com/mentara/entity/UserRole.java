package com.mentara.entity;

/**
 * 用户角色枚举
 * ADMIN: 管理员
 * USER: 普通用户
 * EXPERT: 专家
 */
public enum UserRole {
    ADMIN("管理员"),
    USER("普通用户"),
    EXPERT("专家");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 