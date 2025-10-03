package com.mentara.service;

import com.mentara.dto.request.DeepSeekRequest;
import com.mentara.dto.response.DeepSeekResponse;
import com.mentara.dto.response.LlmJsonResponse;

/**
 * DeepSeek API服务接口
 */
public interface DeepSeekService {
    
    /**
     * 发送聊天请求到DeepSeek API
     * 
     * @param request 请求对象
     * @return 响应对象
     */
    DeepSeekResponse sendChatRequest(DeepSeekRequest request);
    
    /**
     * 根据消息列表生成响应
     * 
     * @param messages 消息列表
     * @param model 模型名称
     * @return 响应内容
     */
    String generateResponse(String messages, String model);
    
    /**
     * 解析JSON格式的LLM响应
     * @param jsonResponse JSON格式的响应字符串
     * @return LlmJsonResponse对象
     */
    LlmJsonResponse parseJsonResponse(String jsonResponse);
} 