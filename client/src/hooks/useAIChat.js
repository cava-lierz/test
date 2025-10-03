import { useState, useCallback, useRef } from "react";
import aiAssistant from "../services/aiAgent.js";

export const useAIChat = () => {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [memoryInfo, setMemoryInfo] = useState("");

  // 使用ref来保持AI助手实例
  const aiAssistantRef = useRef(aiAssistant);

  // 发送消息
  const sendMessage = useCallback(async (userMessage) => {
    if (!userMessage.trim()) return;

    setIsLoading(true);
    setError(null);

    try {
      // 添加用户消息到UI
      const userMsg = {
        id: Date.now(),
        role: "user",
        content: userMessage,
        timestamp: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, userMsg]);

      // 发送消息给AI助手
      const response = await aiAssistantRef.current.sendMessage(userMessage);

      // 添加AI回复到UI
      const aiMsg = {
        id: Date.now() + 1,
        role: "assistant",
        content: response.content,
        timestamp: new Date().toISOString(),
        functionCalled: response.functionCalled,
        functionResult: response.functionResult,
      };

      setMessages((prev) => [...prev, aiMsg]);

      // 更新记忆信息
      setMemoryInfo(aiAssistantRef.current.getMemoryInfo());

      return response;
    } catch (err) {
      setError(err.message);

      // 添加错误消息到UI
      const errorMsg = {
        id: Date.now() + 1,
        role: "error",
        content: `错误: ${err.message}`,
        timestamp: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 清空对话历史
  const clearHistory = useCallback(() => {
    aiAssistantRef.current.clearHistory();
    setMessages([]);
    setMemoryInfo(aiAssistantRef.current.getMemoryInfo());
    setError(null);
  }, []);

  // 获取对话历史
  const getHistory = useCallback(() => {
    return aiAssistantRef.current.getHistory();
  }, []);

  // 设置系统提示
  const setSystemPrompt = useCallback((prompt) => {
    aiAssistantRef.current.setSystemPrompt(prompt);
  }, []);

  // 获取可用函数列表
  const getAvailableFunctions = useCallback(() => {
    return aiAssistantRef.current.getAvailableFunctions();
  }, []);

  // 初始化记忆信息
  const initializeMemoryInfo = useCallback(() => {
    setMemoryInfo(aiAssistantRef.current.getMemoryInfo());
  }, []);

  return {
    // 状态
    messages,
    isLoading,
    error,
    memoryInfo,

    // 方法
    sendMessage,
    clearHistory,
    getHistory,
    setSystemPrompt,
    getAvailableFunctions,
    initializeMemoryInfo,
  };
};
