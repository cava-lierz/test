import React, { useState, useRef, useEffect } from "react";

const SidebarToggleButton = ({ open, onClick }) => (
  <button
    className="sidebar-toggle-btn"
    onClick={onClick}
    title={open ? "收起侧栏" : "展开侧栏"}
    aria-label={open ? "收起侧栏" : "展开侧栏"}
  >
    {open ? (
      <span style={{ fontSize: 20 }}>&lt;</span>
    ) : (
      <span style={{ fontSize: 20 }}>&gt;</span>
    )}
  </button>
);

export default function AIChatPage() {
  // AI助手配置
  const aiAssistants = [
    {
      id: "general",
      name: "心理健康助手",
      avatar: "🤖",
      description: "随时为你提供心理支持和倾听",
      welcomeMessage:
        "你好！我是Mentara的AI助手，很高兴为你服务。你可以和我分享你的心情、困扰或者任何想要倾诉的事情。我会尽力帮助你！",
      responses: [
        "我理解你的感受，这确实是一个挑战。记住，每个人都会遇到困难，重要的是如何面对它们。",
        "听起来你经历了很多，我为你感到心疼。请记住，寻求帮助是勇敢的表现。",
        "你的感受是完全可以理解的。有时候，仅仅是把想法说出来就能让人感觉好一些。",
        "我注意到你提到了一些积极的方面，这很好！即使在困难时期，也要保持希望。",
        "感谢你愿意和我分享这些。记住，你并不孤单，有很多人关心你。",
        "这听起来确实很困难。你有没有考虑过和信任的朋友或家人谈谈这件事？",
        "我听到你在努力应对这个情况，这很了不起。继续保持这种积极的态度。",
        "你的感受很重要，不要忽视它们。有时候，承认自己的情绪是处理它们的第一步。",
      ],
    },
    {
      id: "counselor",
      name: "专业咨询师",
      avatar: "👨‍⚕️",
      description: "提供专业的心理咨询建议",
      welcomeMessage:
        "你好！我是一名专业的心理咨询师AI助手。我可以为你提供专业的心理建议和指导，帮助你更好地理解和管理自己的情绪。",
      responses: [
        "从专业角度来看，你描述的情况很常见。让我们一起来分析一下可能的原因和解决方案。",
        "我建议你可以尝试一些认知行为疗法(CBT)的技巧来应对这种情况。",
        "你的情绪反应是正常的，但我们可以学习更健康的应对方式。",
        "从心理学角度，这种模式可能与你过去的经历有关。理解这一点很重要。",
        "我注意到你使用了一些消极的自我对话，我们可以一起练习更积极的思维方式。",
        "根据你的描述，我建议你可以考虑寻求专业的面谈咨询。",
        "让我们一起来制定一个情绪管理计划，这对你的长期心理健康很有帮助。",
        "你的自我觉察能力很强，这是心理成长的重要基础。",
      ],
    },
    {
      id: "friend",
      name: "知心朋友",
      avatar: "👥",
      description: "像朋友一样倾听和陪伴",
      welcomeMessage:
        "嗨！我是你的AI朋友，我会像真正的朋友一样倾听你，陪伴你。无论你想说什么，我都会在这里支持你！",
      responses: [
        "哇，这确实不容易！我完全理解你的感受，换作是我也会这样想的。",
        "你知道吗？我觉得你已经很勇敢了，敢于面对这些困难。",
        "哈哈，我也有过类似的经历！要不要听听我是怎么处理的？",
        "朋友，你值得更好的！别对自己太苛刻了。",
        "我真的很为你感到骄傲，你比想象中更坚强。",
        "有时候哭出来也没关系，我就在这里陪着你。",
        "你知道吗？你刚才说的那句话特别有道理，我觉得你很聪明！",
        "无论发生什么，我都会一直支持你的，你永远不是一个人。",
      ],
    },
    {
      id: "coach",
      name: "生活教练",
      avatar: "💪",
      description: "帮助你设定目标并实现自我提升",
      welcomeMessage:
        "你好！我是你的AI生活教练。我会帮助你设定目标、制定计划，并鼓励你实现自我提升。让我们一起创造更好的生活！",
      responses: [
        "很好！让我们一起来分析一下你的目标，并制定具体的行动计划。",
        "我注意到你提到了一些障碍，这些都是可以克服的。让我们找到解决方案。",
        "你的进步很明显！继续保持这种积极的态度。",
        "设定小目标很重要，让我们把大目标分解成可执行的小步骤。",
        "记住，失败是成功的一部分。每次挫折都是学习的机会。",
        "我建议你可以尝试一些新的方法来突破当前的瓶颈。",
        "你的坚持让我很感动！这种毅力是成功的关键。",
        "让我们一起来庆祝你的每一个小成就，它们都很重要！",
      ],
    },
  ];

  const [selectedAssistant, setSelectedAssistant] = useState(aiAssistants[0]);
  const [messages, setMessages] = useState([
    {
      id: 1,
      type: "ai",
      content: aiAssistants[0].welcomeMessage,
      time: new Date().toLocaleTimeString(),
    },
  ]);
  const [inputMessage, setInputMessage] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
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

  // 切换AI助手
  const handleAssistantChange = (assistant) => {
    setSelectedAssistant(assistant);
    setMessages([
      {
        id: Date.now(),
        type: "ai",
        content: assistant.welcomeMessage,
        time: new Date().toLocaleTimeString(),
      },
    ]);
    if (window.innerWidth <= 768) {
      setSidebarOpen(false);
    }
  };

  const handleSendMessage = async () => {
    if (!inputMessage.trim()) return;

    const userMessage = {
      id: Date.now(),
      type: "user",
      content: inputMessage,
      time: new Date().toLocaleTimeString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage("");
    setIsTyping(true);

    // 模拟AI回复
    setTimeout(() => {
      const randomResponse =
        selectedAssistant.responses[
          Math.floor(Math.random() * selectedAssistant.responses.length)
        ];

      const aiMessage = {
        id: Date.now() + 1,
        type: "ai",
        content: randomResponse,
        time: new Date().toLocaleTimeString(),
      };

      setMessages((prev) => [...prev, aiMessage]);
      setIsTyping(false);
    }, 1500);
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
              <h3 className="sidebar-header-title">选择AI助手</h3>
              <SidebarToggleButton open={true} onClick={handleSidebarToggle} />
            </div>
            <div className="assistant-list">
              {aiAssistants.map((assistant) => (
                <div
                  key={assistant.id}
                  className={`assistant-item ${
                    selectedAssistant.id === assistant.id ? "active" : ""
                  }`}
                  onClick={() => handleAssistantChange(assistant)}
                >
                  <div className="assistant-avatar">{assistant.avatar}</div>
                  <div className="assistant-info">
                    <h4 className="assistant-name">{assistant.name}</h4>
                    <p className="assistant-description">
                      {assistant.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 折叠状态的内容 */}
          <div className="sidebar-collapsed-content">
            <div className="sidebar-collapsed-bar">
              <SidebarToggleButton open={false} onClick={handleSidebarToggle} />
              <div className="collapsed-avatar-list">
                {aiAssistants.map((assistant) => (
                  <button
                    key={assistant.id}
                    className={`collapsed-avatar-btn${
                      selectedAssistant.id === assistant.id ? " active" : ""
                    }`}
                    onClick={() => {
                      handleAssistantChange(assistant);
                    }}
                    title={assistant.name}
                  >
                    <span className="collapsed-avatar-icon">
                      {assistant.avatar}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* 聊天头部 */}
        <div className="chat-header">
          <div className="chat-header-content">
            <div className="chat-avatar">{selectedAssistant.avatar}</div>
            <div>
              <h1 className="chat-title">{selectedAssistant.name}</h1>
              <p className="chat-subtitle">{selectedAssistant.description}</p>
            </div>
          </div>
        </div>

        {/* 聊天主体区域 */}
        <div className="chat-main">
          {/* 消息列表 */}
          <div className="chat-messages">
            <div className="messages-grid">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`message-container ${message.type}`}
                >
                  <div className={`message-bubble ${message.type}`}>
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
              placeholder="输入你的想法..."
              className="chat-textarea"
              rows="1"
            />
            <button
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() || isTyping}
              className="chat-send-button"
            >
              ➤
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
