package com.mentara.service;

import com.mentara.entity.Expert;
import java.util.List;

public interface ExpertService {
    /**
     * 获取所有心理专家
     */
    List<Expert> getAllExperts();

    /**
     * 根据ID获取专家
     */
    Expert getExpertById(Long id);

    /**
     * 根据用户ID获取专家
     */
    Expert getExpertByUserId(Long userId);

    /**
     * 添加心理专家
     */
    Expert addExpert(Expert expert);

    /**
     * 更新心理专家信息
     */
    Expert updateExpert(Expert expert);

    /**
     * 删除心理专家
     */
    void deleteExpert(Long id);
} 