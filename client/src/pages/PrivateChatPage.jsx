import React, {
  useState,
  useEffect,
  useRef,
  useMemo,
  useCallback,
} from "react";
import ReactDOM from "react-dom";
import { VariableSizeList as List } from "react-window";
import SidebarToggleButton from "../components/SidebarToggleButton";
import { getCurrentUserId } from "../services/api";
import privateChatService from "../services/privateChatService";
import WebSocketService from "../services/webSocketService";
import { AUTH_CONFIG } from "../utils/constants";
import { buildAvatarUrl } from "../utils/avatarUtils";
import {
  getMyChatRoomUser,
  setMyChatRoomUser,
  setChatRoomUser,
  findChatRoomUser,
  upsertChatRoomUser,
} from "../utils/chatLocalStorage";

const wsUrl = "http://localhost:8080/api/ws";
const PAGE_SIZE = 20;

// 竖着三个点的菜单组件，精简为只保留删除私聊
function PrivateChatSettingsMenu({ onQuitRoom }) {
  const [showMenu, setShowMenu] = useState(false);
  const [menuPos, setMenuPos] = useState({ top: 0, left: 0 });
  const btnRef = useRef();
  const menuRef = useRef();

  const handleMenuToggle = () => {
    setShowMenu((v) => !v);
  };

  useEffect(() => {
    if (showMenu && btnRef.current) {
      const rect = btnRef.current.getBoundingClientRect();
      setMenuPos({
        top: rect.bottom + window.scrollY + 4,
        left: rect.right - 140 + window.scrollX,
      });
    }
  }, [showMenu]);

  useEffect(() => {
    if (!showMenu) return;
    const onClick = (e) => {
      if (
        btnRef.current &&
        !btnRef.current.contains(e.target) &&
        menuRef.current &&
        !menuRef.current.contains(e.target)
      ) {
        setShowMenu(false);
      }
    };
    window.addEventListener("mousedown", onClick);
    return () => window.removeEventListener("mousedown", onClick);
  }, [showMenu]);

  return (
    <>
      <button
        className="post-menu-button"
        ref={btnRef}
        onClick={handleMenuToggle}
        style={{
          background: "none",
          border: "none",
          cursor: "pointer",
          fontSize: 24,
          padding: 4,
        }}
        aria-label="更多操作"
      >
        &#8942;
      </button>
      {showMenu &&
        ReactDOM.createPortal(
          <div
            className="post-menu-dropdown"
            ref={menuRef}
            style={{
              position: "absolute",
              top: menuPos.top,
              left: menuPos.left,
              background: "#fff",
              boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
              borderRadius: 6,
              minWidth: 120,
              padding: 4,
              zIndex: 3000,
            }}
          >
            <button
              className="post-menu-item"
              style={{
                width: "100%",
                minWidth: "120px",
                padding: "8px 12px",
                border: "none",
                background: "none",
                textAlign: "left",
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: 8,
                color: "#e74c3c",
              }}
              onClick={() => {
                setShowMenu(false);
                onQuitRoom();
              }}
            >
              <span role="img" aria-label="删除私聊">
                🗑️
              </span>{" "}
              删除私聊
            </button>
          </div>,
          document.body
        )}
    </>
  );
}

const PrivateChatPage = () => {
  const [rooms, setRooms] = useState([]); // 私聊房间列表
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [error, setError] = useState(null);
  const wsRef = useRef(null);
  const userId = getCurrentUserId();
  const [myChatRoomUser, setMyChatRoomUserState] = useState(null);
  const [displayUsers, setDisplayUsers] = useState({});
  const [roomOtherUsers, setRoomOtherUsers] = useState({});
  const messagesContainerRef = useRef(null);
  const listRef = useRef(null);
  const [showNewMsgTip, setShowNewMsgTip] = useState(false);
  const [itemHeights, setItemHeights] = useState({});
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  // 响应式侧边栏
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth <= 768 && sidebarOpen) {
        setSidebarOpen(false);
      }
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, [sidebarOpen]);

  // 空房间占位对象
  const EMPTY_ROOM = useMemo(
    () => ({ id: 0, name: "未选择私聊", description: "", isPlaceholder: true }),
    []
  );

  // 初始化私聊房间列表
  useEffect(() => {
    const fetchRooms = async () => {
      setLoading(true);
      try {
        const data = await privateChatService.getPrivateRooms();
        setRooms(data);
        // 获取每个房间的对方用户信息
        if (data && data.length > 0) {
          const otherUsersData = {};
          for (const room of data) {
            try {
              const otherUserData = await privateChatService.getOtherUser(
                room.id
              );
              if (otherUserData) {
                otherUsersData[room.id] = otherUserData;
              }
            } catch (error) {}
          }
          setRoomOtherUsers(otherUsersData);
        }
        if (data && data.length > 0) {
          setSelectedRoom(data[0]);
        } else {
          setSelectedRoom(EMPTY_ROOM);
        }
      } catch (e) {
        setError("加载私聊房间失败");
        setRooms([]);
        setSelectedRoom(EMPTY_ROOM);
      } finally {
        setLoading(false);
      }
    };
    fetchRooms();
  }, [EMPTY_ROOM]);

  // 进入房间时，分页拉取最新消息
  useEffect(() => {
    if (!selectedRoom || selectedRoom.id === 0) return;
    let isMounted = true;
    const fetchData = async () => {
      setLoading(true);
      const isInRoom = await privateChatService.isInRoom(selectedRoom.id);
      if (isInRoom) {
        try {
          // 拉取房间成员
          const users = await privateChatService.getRoomUsers(selectedRoom.id);
          setChatRoomUser(userId, selectedRoom.id, users || []);
          // 查找自己的本地数据
          let myUser = getMyChatRoomUser(userId, selectedRoom.id);
          if (!myUser) {
            myUser = await privateChatService.getCurrentUserByRoomId(
              selectedRoom.id
            );
            setMyChatRoomUser(userId, selectedRoom.id, myUser);
          }
          if (isMounted) setMyChatRoomUserState(myUser);
          // 分页拉取最新消息
          const remoteMsgs = await privateChatService.getMessagesPaged(
            selectedRoom.id,
            { limit: PAGE_SIZE }
          );
          setMessages(remoteMsgs);
          setHasMore(remoteMsgs.length === PAGE_SIZE);
          setTimeout(() => {
            if (listRef.current && remoteMsgs.length > 0) {
              listRef.current.scrollToItem(remoteMsgs.length - 1, "end");
              if (listRef.current._outerRef) {
                listRef.current._outerRef.scrollTop =
                  listRef.current._outerRef.scrollHeight;
              }
            }
          }, 0);
        } catch (e) {
          setError("加载消息或成员失败");
        } finally {
          setLoading(false);
        }
      } else {
        if (isMounted) setMyChatRoomUserState(null);
        setLoading(false);
        setError("请先加入私聊！");
      }
    };
    fetchData();
    return () => {
      isMounted = false;
    };
  }, [selectedRoom, userId]);

  // 加载更多历史消息
  const handleLoadMore = async () => {
    if (loadingMore || !hasMore || messages.length === 0) return;
    const list = listRef.current;
    let firstVisibleIndex = 0;
    if (list && list._outerRef) {
      const scrollOffset = list.state
        ? list.state.scrollOffset
        : list._outerRef.scrollTop;
      firstVisibleIndex = Math.floor(scrollOffset / getItemSize());
    }
    setLoadingMore(true);
    try {
      const oldestMsg = messages[0];
      const olderMsgs = await privateChatService.getMessagesPaged(
        selectedRoom.id,
        {
          before: oldestMsg.sentAt,
          limit: PAGE_SIZE,
        }
      );
      setMessages((prev) => [...olderMsgs, ...prev]);
      setHasMore(olderMsgs.length === PAGE_SIZE);
      setTimeout(() => {
        const newIndex = olderMsgs.length + firstVisibleIndex;
        if (listRef.current && newIndex !== -1) {
          listRef.current.scrollToItem(newIndex, "start");
        }
      }, 0);
    } catch (e) {
      setError("加载历史消息失败");
    } finally {
      setLoadingMore(false);
    }
  };

  // WebSocket连接与订阅，消息本地去重
  useEffect(() => {
    if (!selectedRoom || selectedRoom.id === 0) return;
    if (wsRef.current) {
      wsRef.current.disconnect();
    }
    const jwt = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    const ws = new WebSocketService({ serverUrl: wsUrl });
    ws.connect(jwt);
    wsRef.current = ws;
    const dest = `/topic/chat/${selectedRoom.id}`;
    ws.subscribe(dest, (msg) => {
      setMessages((prev) => {
        if (!msg.id) return prev;
        if (prev.some((m) => m.id === msg.id)) return prev;
        let isAtBottom = false;
        if (listRef.current && listRef.current._outerRef) {
          const outer = listRef.current._outerRef;
          isAtBottom =
            outer.scrollHeight - outer.scrollTop - outer.clientHeight < 10;
        }
        if (isAtBottom) {
          setTimeout(() => {
            if (listRef.current) {
              listRef.current.scrollToItem(prev.length, "end");
              if (listRef.current._outerRef) {
                listRef.current._outerRef.scrollTop =
                  listRef.current._outerRef.scrollHeight;
              }
            }
          }, 0);
          setShowNewMsgTip(false);
        } else {
          setShowNewMsgTip(true);
        }
        return [...prev, msg];
      });
    });
    return () => {
      ws.disconnect();
    };
  }, [selectedRoom, userId]);

  // 渲染消息时，批量拉取和缓存所有消息的用户信息
  useEffect(() => {
    if (!messages || messages.length === 0 || !selectedRoom) return;
    const timeoutId = setTimeout(() => {
      let isMounted = true;
      const fetchUsers = async () => {
        const newDisplayUsers = {};
        const pendingRequests = new Set();
        for (const msg of messages) {
          const chatRoomUserId = String(msg.chatRoomUserId);
          const version = msg.version;
          let user = findChatRoomUser(userId, selectedRoom.id, chatRoomUserId);
          if (!user || (version && version > user.version)) {
            if (!pendingRequests.has(chatRoomUserId)) {
              pendingRequests.add(chatRoomUserId);
              try {
                const result = await privateChatService.getRoomUserByRoomUserId(
                  selectedRoom.id,
                  chatRoomUserId
                );
                if (result) {
                  user = result;
                  upsertChatRoomUser(userId, selectedRoom.id, user);
                }
              } catch (error) {}
            }
          }
          if (user && user.version === 0) {
            user = {
              displayNickname: "已删除用户",
              displayAvatar: "https://i.pravatar.cc/150?u=deleted",
              chatRoomUserId: chatRoomUserId,
            };
          }
          if (!user) {
            user = {
              displayNickname: "未知用户",
              displayAvatar: "https://i.pravatar.cc/150?u=unknown",
              chatRoomUserId,
            };
          }
          newDisplayUsers[chatRoomUserId] = user;
        }
        if (isMounted) {
          setDisplayUsers(newDisplayUsers);
        }
      };
      fetchUsers();
      return () => {
        isMounted = false;
      };
    }, 100);
    return () => clearTimeout(timeoutId);
  }, [messages, selectedRoom, userId]);

  // 发送消息
  const handleSendMessage = async () => {
    if (!inputMessage.trim() || !selectedRoom || selectedRoom.id === 0) return;
    try {
      await privateChatService.sendMessage(
        selectedRoom.id,
        inputMessage.trim()
      );
      setInputMessage("");
      setTimeout(() => {
        if (listRef.current) {
          listRef.current.scrollToItem(messages.length, "end");
          if (listRef.current._outerRef) {
            listRef.current._outerRef.scrollTop =
              listRef.current._outerRef.scrollHeight;
          }
        }
      }, 0);
    } catch (error) {
      setError("发送消息失败");
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // 切换房间
  const handleRoomChange = (room) => {
    setSelectedRoom(room);
    setMessages([]);
    setError(null);
  };

  // 删除私聊
  const handleQuitRoom = async () => {
    if (!selectedRoom || selectedRoom.id === 0) return;
    if (
      !window.showConfirm &&
      window.showConfirm("确定要删除这个私聊吗？此操作不可撤销。")
    ) {
      return;
    }
    try {
      if (myChatRoomUser) {
        await privateChatService.quitRoom(myChatRoomUser.chatRoomUserId);
      }
      setRooms((prev) => prev.filter((room) => room.id !== selectedRoom.id));
      const remainingRooms = rooms.filter(
        (room) => room.id !== selectedRoom.id
      );
      if (remainingRooms.length > 0) {
        setSelectedRoom(remainingRooms[0]);
      } else {
        setSelectedRoom(EMPTY_ROOM);
      }
      window.showToast && window.showToast("私聊已删除", "success");
    } catch (error) {
      setError("删除私聊失败");
    }
  };

  // 估算每条消息高度（动态）
  const getItemSize = useCallback(
    (index) => itemHeights[index] || 120,
    [itemHeights]
  );

  // 新增：消息渲染组件，动态测量高度
  const MessageItem = ({ index, style, data }) => {
    const { messages, myChatRoomUser, displayUsers, buildAvatarUrl } = data;
    const msg = messages[index];
    const isMe =
      myChatRoomUser &&
      String(msg.chatRoomUserId) === String(myChatRoomUser.chatRoomUserId);
    const displayUser = isMe
      ? myChatRoomUser
      : displayUsers[String(msg.chatRoomUserId)];
    const itemRef = React.useRef();

    React.useEffect(() => {
      if (itemRef.current) {
        const height = itemRef.current.offsetHeight;
        setItemHeights((prev) => {
          if (prev[index] === height) return prev;
          const next = { ...prev, [index]: height };
          if (listRef.current) {
            listRef.current.resetAfterIndex(index, true);
          }
          return next;
        });
      }
    }, [messages, index]);

    return (
      <div
        ref={itemRef}
        key={msg.id || index}
        className={`message-container ${isMe ? "user" : "ai"}`}
        style={{ ...style, width: "100%" }}
      >
        <div className="message-avatar">
          <span className="avatar-circle">
            {displayUser?.displayAvatar ? (
              <img
                src={buildAvatarUrl(
                  displayUser.displayAvatar,
                  "https://i.pravatar.cc/150?u=default"
                )}
                alt="avatar"
                onError={(e) => {
                  e.target.src = "https://i.pravatar.cc/150?u=default";
                }}
              />
            ) : (
              (displayUser?.displayNickname ||
                String(msg.chatRoomUserId) ||
                "U")[0]
            )}
          </span>
        </div>
        <div
          className="message-content-block"
          style={{ float: isMe ? "right" : "left" }}
        >
          <div className="message-nickname">
            {displayUser?.displayNickname || String(msg.chatRoomUserId)}
          </div>
          <div className="message-bubble">
            <span className="message-content">{msg.content}</span>
          </div>
          <span className="message-time">
            {msg.sentAt ? new Date(msg.sentAt).toLocaleString() : ""}
          </span>
        </div>
      </div>
    );
  };

  // 上滑自动加载更多和新消息提示隐藏
  const onListScroll = ({ scrollOffset }) => {
    if (scrollOffset === 0 && hasMore && !loadingMore) {
      handleLoadMore();
    }
    if (listRef.current) {
      const outer = listRef.current._outerRef;
      if (
        outer &&
        outer.scrollHeight - outer.scrollTop - outer.clientHeight < 10
      ) {
        setShowNewMsgTip(false);
      }
    }
  };

  return (
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
        <div
          className="sidebar-backdrop"
          onClick={() => setSidebarOpen(false)}
        ></div>
      )}
      {/* 侧边栏 */}
      <div className={`chat-sidebar ${sidebarOpen ? "open" : "collapsed"}`}>
        {/* 展开状态的内容 */}
        <div className="sidebar-expanded-content">
          <div className="sidebar-header">
            <h3 className="sidebar-header-title">私聊</h3>
            <SidebarToggleButton
              open={true}
              onClick={() => setSidebarOpen(false)}
            />
          </div>
          <div className="assistant-list">
            {rooms.map((room) => {
              const otherUserInfo = roomOtherUsers[room.id];
              const joined = selectedRoom && room.id === selectedRoom.id;
              return (
                <div
                  key={room.id}
                  className={`assistant-item ${joined ? "active" : ""}`}
                  onClick={() => handleRoomChange(room)}
                >
                  <div className="assistant-avatar">
                    {otherUserInfo ? (
                      <img
                        src={buildAvatarUrl(otherUserInfo.avatar)}
                        alt={otherUserInfo.nickname || otherUserInfo.username}
                        className="avatar-image"
                        onError={(e) => {
                          e.target.src = "https://i.pravatar.cc/150?u=default";
                        }}
                      />
                    ) : (
                      <div className="avatar-placeholder">👤</div>
                    )}
                  </div>
                  <div className="assistant-info">
                    <h4 className="assistant-name">
                      {otherUserInfo
                        ? otherUserInfo.nickname || otherUserInfo.username
                        : room.name || `私聊${room.id}`}
                    </h4>
                    <p className="assistant-description">
                      {room.lastMessage || "暂无消息"}
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
            <SidebarToggleButton
              open={false}
              onClick={() => setSidebarOpen(true)}
            />
            <div className="collapsed-avatar-list">
              {rooms.map((room) => {
                const otherUserInfo = roomOtherUsers[room.id];
                return (
                  <button
                    key={room.id}
                    className={`collapsed-avatar-btn${
                      selectedRoom?.id === room.id ? " active" : ""
                    }`}
                    onClick={() => handleRoomChange(room)}
                    title={room.name || `私聊${room.id}`}
                  >
                    {otherUserInfo ? (
                      <img
                        src={buildAvatarUrl(otherUserInfo.avatar)}
                        alt={otherUserInfo.nickname || otherUserInfo.username}
                        className="avatar-image"
                        style={{
                          width: 32,
                          height: 32,
                          borderRadius: "50%",
                          objectFit: "cover",
                        }}
                        onError={(e) => {
                          e.target.src = "https://i.pravatar.cc/150?u=default";
                        }}
                      />
                    ) : (
                      <span className="collapsed-avatar-icon">👤</span>
                    )}
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
          <div className="chat-avatar">
            {selectedRoom?.id !== 0 && roomOtherUsers[selectedRoom?.id] ? (
              <img
                src={buildAvatarUrl(roomOtherUsers[selectedRoom.id].avatar)}
                alt={
                  roomOtherUsers[selectedRoom.id].nickname ||
                  roomOtherUsers[selectedRoom.id].username
                }
                className="avatar-image"
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: "50%",
                  objectFit: "cover",
                }}
                onError={(e) => {
                  e.target.src = "https://i.pravatar.cc/150?u=default";
                }}
              />
            ) : (
              <span style={{ fontSize: 32 }}>💬</span>
            )}
          </div>
          <div className="chat-header-info">
            <h1 className="chat-title">
              {selectedRoom?.id === 0
                ? "未选择私聊"
                : (() => {
                    const otherUser = roomOtherUsers[selectedRoom?.id];
                    return otherUser
                      ? otherUser.nickname || otherUser.username
                      : selectedRoom?.name || "请选择私聊";
                  })()}
            </h1>
            <p className="chat-subtitle">
              {selectedRoom?.id === 0 ? "请在左侧选择一个私聊" : ""}
            </p>
          </div>
          <div className="chat-actions">
            {selectedRoom && selectedRoom.id !== 0 && (
              <PrivateChatSettingsMenu onQuitRoom={handleQuitRoom} />
            )}
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
        {loading && (
          <div className="loading-indicator">
            <div className="loading-spinner"></div>
            <p>正在加载...</p>
          </div>
        )}
        {!loading && rooms.length === 0 && (
          <div className="loading-indicator">
            <div style={{ fontSize: 48, marginBottom: 16 }}>💬</div>
            <p>暂无可用的私聊</p>
          </div>
        )}
        {selectedRoom?.id === 0 && !loading && (
          <div className="loading-indicator">
            <div style={{ fontSize: 48, marginBottom: 16 }}>💬</div>
            <p>请选择左侧的私聊以开始聊天</p>
          </div>
        )}
        {selectedRoom && selectedRoom.id !== 0 && (
          <div
            className="chat-messages"
            ref={messagesContainerRef}
            style={{
              position: "relative",
              height: "100%",
              minHeight: 300,
              overflow: "hidden",
            }}
          >
            <List
              ref={listRef}
              height={
                messagesContainerRef.current
                  ? messagesContainerRef.current.clientHeight || 400
                  : 400
              }
              itemCount={messages.length}
              itemSize={getItemSize}
              width={"100%"}
              itemData={{
                messages,
                myChatRoomUser,
                displayUsers,
                buildAvatarUrl,
              }}
              overscanCount={6}
              style={{ width: "100%", minHeight: 200 }}
              onScroll={onListScroll}
            >
              {MessageItem}
            </List>
            {showNewMsgTip && (
              <div
                className="new-msg-tip"
                style={{
                  position: "absolute",
                  bottom: 24,
                  left: 0,
                  right: 0,
                  display: "flex",
                  justifyContent: "center",
                  zIndex: 10,
                }}
              >
                <button
                  style={{
                    background: "#409eff",
                    color: "#fff",
                    border: "none",
                    borderRadius: 16,
                    padding: "4px 16px",
                    cursor: "pointer",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
                  }}
                  onClick={() => {
                    if (listRef.current) {
                      listRef.current.scrollToItem(messages.length - 1, "end");
                      if (listRef.current._outerRef) {
                        listRef.current._outerRef.scrollTop =
                          listRef.current._outerRef.scrollHeight;
                      }
                    }
                    setShowNewMsgTip(false);
                  }}
                >
                  有新消息，点击查看
                </button>
              </div>
            )}
          </div>
        )}
      </div>
      {/* 输入区域 */}
      <div className="chat-input-container">
        <div className="chat-input-wrapper">
          <textarea
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={
              selectedRoom?.id === 0 ? "请先选择私聊..." : "输入消息..."
            }
            className="chat-textarea"
            rows="1"
            disabled={loading || !selectedRoom || selectedRoom.id === 0}
          />
          <button
            onClick={handleSendMessage}
            disabled={
              !inputMessage.trim() ||
              loading ||
              !selectedRoom ||
              selectedRoom.id === 0
            }
            className="chat-send-button"
          >
            ➤
          </button>
        </div>
      </div>
    </div>
  );
};

export default PrivateChatPage;
