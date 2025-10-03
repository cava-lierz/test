package com.mentara.dto.response;

import java.time.LocalDateTime;

public class UserStatsResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private String status; // active, suspended
    private LocalDateTime joinDate;
    private int postCount;
    private int reportCount;
    private LocalDateTime lastActive;
    private int joinDays; // 加入天数

    public UserStatsResponse() {}

    public UserStatsResponse(Long id, String username, String nickname, String email, 
                           String role, String status, LocalDateTime joinDate, 
                           int postCount, int reportCount, LocalDateTime lastActive, int joinDays) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.joinDate = joinDate;
        this.postCount = postCount;
        this.reportCount = reportCount;
        this.lastActive = lastActive;
        this.joinDays = joinDays;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public int getJoinDays() {
        return joinDays;
    }

    public void setJoinDays(int joinDays) {
        this.joinDays = joinDays;
    }
} 