package com.mentara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抑郁检查响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepressCheckResponse {
    
    /**
     * 抑郁倾向标签
     * 0: 无抑郁倾向
     * 1: 轻度抑郁倾向
     * 2: 中度抑郁倾向
     * 3: 重度抑郁倾向
     */
    private Integer label;
    
    /**
     * 抑郁倾向得分 (0.0 - 1.0)
     */
    private Double score;
} 