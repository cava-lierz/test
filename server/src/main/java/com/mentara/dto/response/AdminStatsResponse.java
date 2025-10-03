package com.mentara.dto.response;

public class AdminStatsResponse {
    private int pendingReports;
    private int pendingPosts;
    private long activeUsers;
    private long newUsersThisMonth;
    private long totalUsers;
    private long totalPosts;
    private long totalComments;
    private long totalLikes;
    private double averageMoodScore;

    public AdminStatsResponse() {}

    public AdminStatsResponse(int pendingReports, int pendingPosts, long activeUsers, 
                             long newUsersThisMonth, long totalUsers, long totalPosts,
                             long totalComments, long totalLikes, double averageMoodScore) {
        this.pendingReports = pendingReports;
        this.pendingPosts = pendingPosts;
        this.activeUsers = activeUsers;
        this.newUsersThisMonth = newUsersThisMonth;
        this.totalUsers = totalUsers;
        this.totalPosts = totalPosts;
        this.totalComments = totalComments;
        this.totalLikes = totalLikes;
        this.averageMoodScore = averageMoodScore;
    }

    // Getters and Setters
    public int getPendingReports() {
        return pendingReports;
    }

    public void setPendingReports(int pendingReports) {
        this.pendingReports = pendingReports;
    }

    public int getPendingPosts() {
        return pendingPosts;
    }

    public void setPendingPosts(int pendingPosts) {
        this.pendingPosts = pendingPosts;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public double getAverageMoodScore() {
        return averageMoodScore;
    }

    public void setAverageMoodScore(double averageMoodScore) {
        this.averageMoodScore = averageMoodScore;
    }
} 