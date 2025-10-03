package com.mentara.dto.response;

import com.mentara.entity.Report;
import com.mentara.enums.MoodType;
import com.mentara.enums.PostState;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportedPostResponse {
    // 帖子基本信息（精简版）
    private Long id;
    private String title;
    private String content;
    private MoodType mood;
    private LocalDateTime createdAt;
    
    // 作者信息（精简版）
    private String authorName;
    private Long authorId;
    
    // 举报核心信息
    private Integer reportCount;
    private PostState state; // 举报处理状态
    private LocalDateTime lastReportTime; // 最近一次举报时间
    
    // 举报原因汇总
    private List<String> reportReasons; // 所有举报原因的列表
} 