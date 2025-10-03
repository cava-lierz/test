package com.mentara.dto;

import lombok.Data;

/**
 * 专家用户数据传输对象
 * 包含用户基本信息和专家详细信息
 */
@Data
public class ExpertUserDTO {
    
    /** 用户ID（用于预约时使用） */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像 */
    private String avatar;
    
    /** 专家记录ID */
    private Long expertId;
    
    /** 专家姓名 */
    private String expertName;
    
    /** 专业领域 */
    private String specialty;
    
    /** 联系方式 */
    private String contact;
    
    /** 在线状态 */
    private String status;
    
    /** 是否有专家详细信息 */
    private boolean hasExpertDetails;
} 