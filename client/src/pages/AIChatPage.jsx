import React, { useState, useRef, useEffect } from "react";
import { aiAPI } from "../services/api";
import SidebarToggleButton from "../components/SidebarToggleButton";

const assistantTemplate = {
  expert: {
    name: "心理咨询师",
    avatar: "👨‍⚕️",
    bio: "专业的心理咨询师，可以为你提供专业的心理咨询服务",
  },
  friend: {
    name: "好朋友",
    avatar: "👥",
    bio: "像真正的朋友一样倾听你，陪伴你，支持你",
  },
  lover: {
    name: "贴心伴侣",
    avatar: "💕",
    bio: "温柔体贴的恋人，给你温暖和关爱",
  },
  coach: {
    name: "正念教练",
    avatar: "👫",
    bio: "给你最专业的正念指导，帮你找到内心的平静",
  },
  general: {
    name: "通用",
    avatar: "🤖",
    bio: "Mentara的AI助手，很高兴为你服务",
  },
};

const AIChatPage = () => {
  // 根据model获取助手信息的函数
  const getAssistantInfo = (model) => {
    return assistantTemplate[model] || assistantTemplate.general;
  };

  // 处理后端返回的日期数组格式
  const formatDate = (dateArray) => {
    if (Array.isArray(dateArray)) {
      // 后端返回的格式: [year, month, day, hour, minute, second, nanoseconds]
      const [year, month, day, hour, minute, second] = dateArray;
      return new Date(
        year,
        month - 1,
        day,
        hour,
        minute,
        second
      ).toLocaleString();
    } else if (dateArray) {
      // 如果是标准日期字符串，直接转换
      return new Date(dateArray).toLocaleString();
    }
    return new Date().toLocaleString();
  };

  const [sessions, setSessions] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [currentSessionId, setCurrentSessionId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth <= 768 && sidebarOpen) {
        setSidebarOpen(false);
      }
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [sidebarOpen]);

  // 初始化：获取会话列表
  useEffect(() => {
    const initializeChat = async () => {
      try {
        setLoading(true);

        // 获取会话列表
        const sessionsData = await aiAPI.getSessions();
        setSessions(sessionsData);

        // 如果有会话，选择第一个作为默认会话
        if (sessionsData && sessionsData.length > 0) {
          const firstSession = sessionsData[0];
          const assistantInfo = getAssistantInfo(firstSession.chatType);

          // 合并session数据和助手模板信息
          const enrichedSession = {
            ...firstSession,
            name: assistantInfo.name,
            avatar: assistantInfo.avatar,
            bio: assistantInfo.bio,
          };

          setSelectedSession(enrichedSession);
          setCurrentSessionId(firstSession.id);

          // 加载现有会话的消息
          const messagesData = await aiAPI.getMessages(firstSession.id);
          if (messagesData && messagesData.length > 0) {
            const formattedMessages = messagesData.map((msg, index) => ({
              id: index + 1,
              role: msg.role,
              content: msg.content,
              time: formatDate(msg.createdAt),
            }));
            setMessages(formattedMessages);
          } else {
            // 如果没有消息，设置默认欢迎消息
            setMessages([
              {
                id: Date.now(),
                role: "assistant",
                content: `你好！我是${assistantInfo.name}，很高兴为你服务。`,
                time: new Date().toLocaleString(),
              },
            ]);
          }
        }
      } catch (err) {
        console.error("初始化聊天失败:", err);
        setError("加载聊天会话失败");
      } finally {
        setLoading(false);
      }
    };

    initializeChat();
  }, []);

  // 切换会话
  const handleSessionChange = async (session) => {
    const assistantInfo = getAssistantInfo(session.chatType);

    // 合并session数据和助手模板信息
    const enrichedSession = {
      ...session,
      name: assistantInfo.name,
      avatar: assistantInfo.avatar,
      bio: assistantInfo.bio,
    };

    setSelectedSession(enrichedSession);
    setCurrentSessionId(session.id);

    try {
      // 加载会话的消息
      const messagesData = await aiAPI.getMessages(session.id);
      console.log(messagesData);
      if (messagesData && messagesData.length > 0) {
        const formattedMessages = messagesData.map((msg, index) => ({
          id: index + 1,
          role: msg.role,
          content: msg.content,
          time: formatDate(msg.createdAt),
        }));
        setMessages(formattedMessages);
      } else {
        // 如果没有消息，设置默认欢迎消息
        setMessages([
          {
            id: Date.now(),
            role: "assistant",
            content: `你好！我是${assistantInfo.name}，很高兴为你服务。`,
            time: new Date().toLocaleString(),
          },
        ]);
      }

      if (window.innerWidth <= 768) {
        setSidebarOpen(false);
      }
    } catch (err) {
      console.error("切换会话失败:", err);
      setError("切换会话失败");
    }
  };

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isTyping || !selectedSession) return;

    const userMessage = {
      id: Date.now(),
      role: "user",
      content: inputMessage,
      time: new Date().toLocaleString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage("");
    setIsTyping(true);
    setError(null);

    try {
      // 发送消息到后端
      const response = await aiAPI.sendMessage(currentSessionId, inputMessage);

      if (response && response.content) {
        const aiMessage = {
          id: Date.now() + 1,
          role: "assistant",
          content: response.content,
          time: new Date().toLocaleString(),
        };

        setMessages((prev) => [...prev, aiMessage]);
      } else {
        throw new Error("AI回复格式错误");
      }
    } catch (err) {
      console.error("发送消息失败:", err);
      setError("发送消息失败，请重试");

      // 添加错误消息
      const errorMessage = {
        id: Date.now() + 1,
        role: "assistant",
        content: "抱歉，我暂时无法回复，请稍后再试。",
        time: new Date().toLocaleString(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // 处理侧边栏切换
  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  // 处理侧边栏关闭
  const handleSidebarClose = () => {
    setSidebarOpen(false);
  };

  // 清空聊天记录
  const handleClearChat = async () => {
    try {
      if (currentSessionId) {
        await aiAPI.clearSession(currentSessionId);
      }
      setMessages([
        {
          id: Date.now(),
          role: "assistant",
          content: `你好！我是${
            selectedSession?.name || "AI助手"
          }，很高兴为你服务。`,
          time: new Date().toLocaleString(),
        },
      ]);
    } catch (err) {
      console.error("清空聊天失败:", err);
      setError("清空聊天失败");
    }
  };

  // 清空记忆
  const handleClearMemory = async () => {
    try {
      if (currentSessionId) {
        await aiAPI.clearMemory(currentSessionId);
      }
      setMessages([
        {
          id: Date.now(),
          role: "assistant",
          content: `你好！我是${
            selectedSession?.name || "AI助手"
          }，很高兴为你服务。`,
          time: new Date().toLocaleString(),
        },
      ]);
    } catch (err) {
      console.error("清空记忆失败:", err);
      setError("清空记忆失败");
    }
  };

  return (
    <>
      <div
        className={`chat-container${
          sidebarOpen ? " sidebar-open" : " sidebar-collapsed"
        }`}
        style={{
          paddingLeft: window.innerWidth > 768 ? (sidebarOpen ? 300 : 48) : 48,
          paddingRight: 0,
          transition: "padding-left 0.3s ease",
        }}
      >
        {/* 窄屏sidebar展开时的遮罩 */}
        {window.innerWidth <= 768 && sidebarOpen && (
          <div className="sidebar-backdrop" onClick={handleSidebarClose}></div>
        )}
        {/* 侧边栏 */}
        <div className={`chat-sidebar ${sidebarOpen ? "open" : "collapsed"}`}>
          {/* 展开状态的内容 */}
          <div className="sidebar-expanded-content">
            <div className="sidebar-header">
              <h3 className="sidebar-header-title">选择会话</h3>
              <SidebarToggleButton open={true} onClick={handleSidebarToggle} />
            </div>
            <div className="assistant-list">
              {sessions.map((session) => {
                const assistantInfo = getAssistantInfo(session.chatType);
                return (
                  <div
                    key={session.id}
                    className={`assistant-item ${
                      selectedSession?.id === session.id ? "active" : ""
                    }`}
                    onClick={() => handleSessionChange(session)}
                  >
                    <div className="assistant-avatar">
                      {assistantInfo.avatar}
                    </div>
                    <div className="assistant-info">
                      <h4 className="assistant-name">{assistantInfo.name}</h4>
                      <p className="assistant-description">
                        {assistantInfo.bio}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* 折叠状态的内容 */}
          <div className="sidebar-collapsed-content">
            <div className="sidebar-collapsed-bar">
              <SidebarToggleButton open={false} onClick={handleSidebarToggle} />
              <div className="collapsed-avatar-list">
                {sessions.map((session) => {
                  const assistantInfo = getAssistantInfo(session.chatType);
                  return (
                    <button
                      key={session.id}
                      className={`collapsed-avatar-btn${
                        selectedSession?.id === session.id ? " active" : ""
                      }`}
                      onClick={() => {
                        handleSessionChange(session);
                      }}
                      title={assistantInfo.name}
                    >
                      <span className="collapsed-avatar-icon">
                        {assistantInfo.avatar}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        </div>

        {/* 聊天头部 */}
        <div className="chat-header">
          <div className="chat-header-content">
            <div className="chat-avatar">{selectedSession?.avatar || "🤖"}</div>
            <div className="chat-header-info">
              <h1 className="chat-title">
                {selectedSession?.name || "AI助手"}
              </h1>
              <p className="chat-subtitle">
                {selectedSession?.bio || "正在加载..."}
              </p>
            </div>
            <div className="chat-actions">
              <button
                className="clear-chat-btn"
                onClick={handleClearChat}
                title="清空聊天记录"
                disabled={!selectedSession}
              >
                清空
              </button>
              <button
                className="clear-memory-btn"
                onClick={handleClearMemory}
                title="清空记忆"
                disabled={!selectedSession}
              >
                清空记忆
              </button>
            </div>
          </div>
        </div>

        {/* 错误提示 */}
        {error && (
          <div className="error-message">
            {error}
            <button onClick={() => setError(null)}>×</button>
          </div>
        )}

        {/* 聊天主体区域 */}
        <div className="chat-main">
          {/* 加载状态 */}
          {loading && (
            <div className="loading-indicator">
              <div className="loading-spinner"></div>
              <p>正在加载会话...</p>
            </div>
          )}

          {/* 没有会话时的状态 */}
          {!loading && sessions.length === 0 && (
            <div className="loading-indicator">
              <div style={{ fontSize: 48, marginBottom: 16 }}>🤖</div>
              <p>暂无可用的会话</p>
            </div>
          )}

          {/* 消息列表 */}
          <div className="chat-messages">
            <div className="messages-grid">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`message-container ${
                    message.role === "user" ? "user" : "ai"
                  }`}
                >
                  <div
                    className={`message-bubble ${
                      message.role === "user" ? "user" : "ai"
                    }`}
                  >
                    <p className="message-content">{message.content}</p>
                    <span className="message-time">{message.time}</span>
                  </div>
                </div>
              ))}

              {/* 正在输入指示器 */}
              {isTyping && (
                <div className="typing-indicator">
                  <div className="message-bubble ai">
                    <div className="typing-dots">
                      <div className="typing-dot"></div>
                      <div className="typing-dot"></div>
                      <div className="typing-dot"></div>
                    </div>
                  </div>
                </div>
              )}
            </div>
            <div ref={messagesEndRef} />
          </div>
        </div>

        {/* 输入区域 */}
        <div className="chat-input-container">
          <div className="chat-input-wrapper">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={
                selectedSession ? "输入你的想法..." : "请先选择会话..."
              }
              className="chat-textarea"
              rows="1"
              disabled={isTyping || loading || !selectedSession}
            />
            <button
              onClick={handleSendMessage}
              disabled={
                !inputMessage.trim() || isTyping || loading || !selectedSession
              }
              className="chat-send-button"
            >
              ➤
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default AIChatPage;
