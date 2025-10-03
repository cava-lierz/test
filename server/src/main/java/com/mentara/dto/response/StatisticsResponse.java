 package com.mentara.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private Integer newUsers;        // 新增用户数
    private Integer totalPosts;      // 总帖子数
    private Integer reportCount;     // 举报数
    private Integer activeUsers;     // 活跃用户数
    private LocalDateTime startDate; // 统计开始时间
    private LocalDateTime endDate;   // 统计结束时间
}