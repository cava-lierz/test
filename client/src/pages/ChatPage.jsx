import React, {
  useState,
  useEffect,
  useRef,
  useMemo,
  useCallback,
} from "react";
import ReactDOM from "react-dom";
import { VariableSizeList as List } from "react-window";
import { getCurrentUserId } from "../services/api";
import chatRoomService from "../services/chatRoomService";
import WebSocketService from "../services/webSocketService";
import { CHAT_ROOM_TYPE, AUTH_CONFIG } from "../utils/constants";
import { buildAvatarUrl } from "../utils/avatarUtils";
import SidebarToggleButton from "../components/SidebarToggleButton";
import {
  getMyChatRoomUser,
  setMyChatRoomUser,
  setChatRoomUser,
  findChatRoomUser,
  upsertChatRoomUser,
} from "../utils/chatLocalStorage";

const wsUrl = "http://localhost:8080/api/ws";
const PAGE_SIZE = 20;

function ChatSettingsMenu({ onUploadAvatar, onSetNickname, onQuitRoom }) {
  // 与原ChatPage一致
  const [showMenu, setShowMenu] = useState(false);
  const [menuPos, setMenuPos] = useState({ top: 0, left: 0 });
  const btnRef = useRef();
  const menuRef = useRef();
  const handleMenuToggle = () => setShowMenu((v) => !v);
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
              }}
              onClick={() => {
                setShowMenu(false);
                onUploadAvatar();
              }}
            >
              <span role="img" aria-label="上传头像">
                🖼️
              </span>{" "}
              上传头像
            </button>
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
              }}
              onClick={() => {
                setShowMenu(false);
                onSetNickname();
              }}
            >
              <span role="img" aria-label="设置昵称">
                ✏️
              </span>{" "}
              设置昵称
            </button>
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
              <span role="img" aria-label="退出房间">
                🚪
              </span>{" "}
              退出房间
            </button>
          </div>,
          document.body
        )}
    </>
  );
}

const ChatPage = () => {
  // 结构与 testChatPage 完全同步
  const [rooms, setRooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [roomUsers, setRoomUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);
  const wsRef = useRef(null);
  const userId = getCurrentUserId();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newRoomName, setNewRoomName] = useState("");
  const [newRoomDesc, setNewRoomDesc] = useState("");
  const [newRoomType, setNewRoomType] = useState(CHAT_ROOM_TYPE.REALNAME);
  const [creating, setCreating] = useState(false);
  const [myChatRoomUser, setMyChatRoomUserState] = useState(null);
  const [displayUsers, setDisplayUsers] = useState({});
  const [joinedRoomIds, setJoinedRoomIds] = useState(new Set());
  // 分页相关
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  // 头像/昵称相关state
  const [showAvatarModal, setShowAvatarModal] = useState(false);
  const [showNicknameModal, setShowNicknameModal] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const [settingNickname, setSettingNickname] = useState(false);
  const [newNickname, setNewNickname] = useState("");
  const [selectedAvatarFile, setSelectedAvatarFile] = useState(null);
  const messagesContainerRef = useRef(null);
  const listRef = useRef(null);
  const [showNewMsgTip, setShowNewMsgTip] = useState(false);
  // 新增：每条消息的动态高度
  const [itemHeights, setItemHeights] = useState({});

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
    () => ({
      id: 0,
      name: "未选择聊天室",
      description: "",
      isPlaceholder: true,
    }),
    []
  );

  // 初始化聊天室列表
  useEffect(() => {
    const fetchRooms = async () => {
      setLoading(true);
      try {
        const data = await chatRoomService.getRooms();
        setRooms(data);
        if (data && data.length > 0) {
          setSelectedRoom(data[0]);
        } else {
          setSelectedRoom(EMPTY_ROOM);
        }
      } catch (e) {
        setError("加载聊天室失败");
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
      const isInRoom = await chatRoomService.isInRoom(selectedRoom.id);
      if (isInRoom) {
        try {
          // 1. 拉取房间成员
          //const users = await chatRoomService.getRoomUsers(selectedRoom.id);
          //setChatRoomUser(userId, selectedRoom.id, users || []);
          //if (isMounted) setRoomUsers(users || []);
          // 2. 查找自己的本地数据
          let myUser = getMyChatRoomUser(userId, selectedRoom.id);
          if (!myUser) {
            myUser = await chatRoomService.getCurrentUserByRoomId(
              selectedRoom.id
            );
            setMyChatRoomUser(userId, selectedRoom.id, myUser);
          }
          if (isMounted) setMyChatRoomUserState(myUser);
          // 3. 分页拉取最新消息
          const remoteMsgs = await chatRoomService.getMessagesPaged(
            selectedRoom.id,
            { limit: PAGE_SIZE }
          );
          setMessages(remoteMsgs);
          setHasMore(remoteMsgs.length === PAGE_SIZE);
          // 首次加载后滚动到底部
          setTimeout(() => {
            if (listRef.current && remoteMsgs.length > 0) {
              listRef.current.scrollToItem(remoteMsgs.length - 1, "end");
              // 兜底
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
        setError("请先加入聊天室！");
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
    // 记录当前第一个可见消息的id
    let firstVisibleIndex = 0;
    if (list && list._outerRef) {
      const scrollOffset = list.state
        ? list.state.scrollOffset
        : list._outerRef.scrollTop;
      firstVisibleIndex = Math.floor(scrollOffset / getItemSize());
    }
    // const firstMsgId = messages[firstVisibleIndex]?.id; // 暂时注释掉未使用的变量
    setLoadingMore(true);
    try {
      const oldestMsg = messages[0];
      const olderMsgs = await chatRoomService.getMessagesPaged(
        selectedRoom.id,
        {
          before: oldestMsg.sentAt,
          limit: PAGE_SIZE,
        }
      );
      setMessages((prev) => [...olderMsgs, ...prev]);
      setHasMore(olderMsgs.length === PAGE_SIZE);
      setTimeout(() => {
        // 找到新messages中firstMsgId的index
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
        // 判断用户是否在底部（react-window 方式）
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
              // 兜底
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
                const result = await chatRoomService.getRoomUserByRoomUserId(
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
    }, 300);
    return () => {
      clearTimeout(timeoutId);
    };
  }, [messages, selectedRoom, userId]);

  // 懒加载判断用户是否已加入每个房间
  useEffect(() => {
    if (!rooms || rooms.length === 0) return;
    rooms.forEach((room) => {
      if (!joinedRoomIds.has(room.id)) {
        chatRoomService.isInRoom(room.id).then((isIn) => {
          if (isIn) {
            setJoinedRoomIds((prev) => new Set(prev).add(room.id));
          }
        });
      }
    });
  }, [rooms, joinedRoomIds]);

  const isUserInRoom = (roomId) => joinedRoomIds.has(roomId);

  // 发送消息
  const handleSendMessage = async () => {
    if (!inputMessage.trim() || !selectedRoom) return;
    try {
      await chatRoomService.sendMessage(selectedRoom.id, inputMessage);
      setInputMessage("");
      // 主动滚动到底部
      setTimeout(() => {
        if (listRef.current) {
          listRef.current.scrollToItem(messages.length, "end");
          if (listRef.current._outerRef) {
            listRef.current._outerRef.scrollTop =
              listRef.current._outerRef.scrollHeight;
          }
        }
      }, 0);
    } catch (e) {
      setError("发送消息失败");
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // 侧边栏切换
  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };
  const handleSidebarClose = () => {
    setSidebarOpen(false);
  };

  // 切换聊天室
  const handleRoomChange = (room) => {
    if (selectedRoom && room.id === selectedRoom.id) {
      setSelectedRoom(EMPTY_ROOM);
      setMyChatRoomUserState(null);
      setRoomUsers([]);
      setMessages([]);
      setDisplayUsers({});
      setError(null);
      if (window.innerWidth <= 768) {
        setSidebarOpen(false);
      }
      return;
    }
    setSelectedRoom(room);
    setMyChatRoomUserState(null);
    setRoomUsers([]);
    setMessages([]);
    setDisplayUsers({});
    setError(null);
    if (window.innerWidth <= 768) {
      setSidebarOpen(false);
    }
  };

  // 创建聊天室
  const handleCreateRoom = async (e) => {
    e.preventDefault();
    if (!newRoomName.trim()) {
      setError("聊天室名称不能为空");
      return;
    }
    setCreating(true);
    try {
      const room = await chatRoomService.createRoom({
        name: newRoomName,
        description: newRoomDesc,
        type: newRoomType,
      });
      setShowCreateModal(false);
      setNewRoomName("");
      setNewRoomDesc("");
      setNewRoomType(CHAT_ROOM_TYPE.REALNAME);
      const data = await chatRoomService.getRooms();
      setRooms(data);
      if (data && data.length > 0) {
        setSelectedRoom(data[room.id]);
      }
    } catch (e) {
      setError("创建聊天室失败");
    } finally {
      setCreating(false);
    }
  };

  // 加入房间
  const handleJoinRoom = async (roomId) => {
    try {
      await chatRoomService.joinRoom(roomId);
      setJoinedRoomIds((prev) => new Set(prev).add(roomId));
      if (selectedRoom && selectedRoom.id === roomId) {
        setError(null);
        const users = await chatRoomService.getRoomUsers(roomId);
        setRoomUsers(users || []);
        setChatRoomUser(userId, roomId, users || []);
        const myUser = await chatRoomService.getCurrentUserByRoomId(roomId);
        setMyChatRoomUser(userId, roomId, myUser);
        setMyChatRoomUserState(myUser);
        // 分页拉取最新消息
        const remoteMsgs = await chatRoomService.getMessagesPaged(roomId, {
          limit: PAGE_SIZE,
        });
        setMessages(remoteMsgs);
        setHasMore(remoteMsgs.length === PAGE_SIZE);
      }
    } catch (e) {
      setError("加入聊天室失败");
    }
  };

  // 头像上传、昵称设置、退出房间逻辑与原来一致
  const handleAvatarFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (!file.type.startsWith("image/")) {
        setError("请选择图片文件");
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        setError("图片大小不能超过 5MB");
        return;
      }
      setSelectedAvatarFile(file);
    }
  };
  const handleUploadAvatar = async () => {
    if (!selectedAvatarFile || !selectedRoom) return;
    setUploadingAvatar(true);
    try {
      const formData = new FormData();
      formData.append("file", selectedAvatarFile);
      const result = await chatRoomService.uploadAvatar(
        selectedRoom.id,
        formData
      );
      setMyChatRoomUserState(result);
      setMyChatRoomUser(userId, selectedRoom.id, result);
      setDisplayUsers((prev) => ({
        ...prev,
        [String(result.chatRoomUserId)]: result,
      }));
      setShowAvatarModal(false);
      setSelectedAvatarFile(null);
      setError(null);
    } catch (e) {
      setError("头像上传失败");
    } finally {
      setUploadingAvatar(false);
    }
  };
  const handleSetNickname = async () => {
    if (!newNickname.trim() || !selectedRoom) return;
    setSettingNickname(true);
    try {
      const result = await chatRoomService.setNickname(
        selectedRoom.id,
        newNickname.trim()
      );
      setMyChatRoomUserState(result);
      setMyChatRoomUser(userId, selectedRoom.id, result);
      setDisplayUsers((prev) => ({
        ...prev,
        [String(result.chatRoomUserId)]: result,
      }));
      setShowNicknameModal(false);
      setNewNickname("");
      setError(null);
    } catch (e) {
      setError("昵称设置失败");
    } finally {
      setSettingNickname(false);
    }
  };
  const handleQuitRoom = async () => {
    if (!myChatRoomUser || !selectedRoom) return;
    if (!(window.showConfirm && window.showConfirm("确定要退出当前房间吗？")))
      return;
    try {
      await chatRoomService.quitRoom(myChatRoomUser.chatRoomUserId);
      setMyChatRoomUserState(null);
      setJoinedRoomIds((prev) => {
        const newSet = new Set(prev);
        newSet.delete(selectedRoom.id);
        return newSet;
      });
      setSelectedRoom(null);
      setMessages([]);
      setRoomUsers([]);
      setError(null);
      const data = await chatRoomService.getRooms();
      setRooms(data);
    } catch (e) {
      setError("退出房间失败");
    }
  };

  // 估算每条消息高度（动态）
  const getItemSize = useCallback(
    (index) => itemHeights[index] || 120, // 120为兜底高度
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

  // UI渲染部分与testChatPage一致
  return (
    <>
      {/* 创建聊天室弹窗 */}
      {showCreateModal && (
        <div className="modal-backdrop chat-create-modal">
          <div className="modal-container">
            <h2>创建聊天室</h2>
            <form onSubmit={handleCreateRoom}>
              <div className="form-group">
                <label>名称</label>
                <input
                  value={newRoomName}
                  onChange={(e) => setNewRoomName(e.target.value)}
                  maxLength={30}
                  required
                />
              </div>
              <div className="form-group">
                <label>简介</label>
                <textarea
                  value={newRoomDesc}
                  onChange={(e) => setNewRoomDesc(e.target.value)}
                  maxLength={100}
                />
              </div>
              <div className="form-group">
                <label>类型</label>
                <select
                  value={newRoomType}
                  onChange={(e) => setNewRoomType(e.target.value)}
                >
                  <option value={CHAT_ROOM_TYPE.REALNAME}>实名聊天室</option>
                  <option value={CHAT_ROOM_TYPE.ANONYMOUS}>匿名聊天室</option>
                </select>
              </div>
              <div className="modal-actions">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  disabled={creating}
                >
                  取消
                </button>
                <button type="submit" disabled={creating}>
                  {creating ? "创建中..." : "创建"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
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
        {window.innerWidth <= 768 && sidebarOpen && (
          <div className="sidebar-backdrop" onClick={handleSidebarClose}></div>
        )}
        <div className={`chat-sidebar ${sidebarOpen ? "open" : "collapsed"}`}>
          <div className="sidebar-expanded-content">
            <div className="sidebar-header">
              <h3 className="sidebar-header-title">聊天室</h3>
              <button
                className="create-room-btn"
                onClick={() => setShowCreateModal(true)}
                style={{ marginRight: 8 }}
              >
                ＋新建
              </button>
              <SidebarToggleButton open={true} onClick={handleSidebarToggle} />
            </div>
            <div className="assistant-list">
              {rooms.map((room) => {
                const joined =
                  selectedRoom && room.id === selectedRoom.id ? true : false;
                return (
                  <div
                    key={room.id}
                    className={`assistant-item ${joined ? "active" : ""}`}
                    onClick={() => handleRoomChange(room)}
                  >
                    <div className="assistant-avatar">💬</div>
                    <div className="assistant-info">
                      <h4 className="assistant-name">
                        {room.name || `聊天室${room.id}`}
                      </h4>
                      <p className="assistant-description">
                        {room.description || "群聊"}
                      </p>
                    </div>
                    {!isUserInRoom(room.id) && (
                      <button
                        className="join-room-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleJoinRoom(room.id);
                        }}
                      >
                        加入
                      </button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
          <div className="sidebar-collapsed-content">
            <div className="sidebar-collapsed-bar">
              <SidebarToggleButton open={false} onClick={handleSidebarToggle} />
              <div className="collapsed-avatar-list">
                {rooms.map((room) => (
                  <button
                    key={room.id}
                    className={`collapsed-avatar-btn${
                      selectedRoom?.id === room.id ? " active" : ""
                    }`}
                    onClick={() => handleRoomChange(room)}
                    title={room.name || `聊天室${room.id}`}
                  >
                    <span className="collapsed-avatar-icon">💬</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="chat-header">
          <div className="chat-header-content">
            <div className="chat-avatar">💬</div>
            <div className="chat-header-info">
              <h1 className="chat-title">
                {selectedRoom?.id === 0
                  ? "未选择聊天室"
                  : selectedRoom?.name || "请选择聊天室"}
              </h1>
              <p className="chat-subtitle">
                {selectedRoom?.id === 0
                  ? "请在左侧选择一个聊天室"
                  : selectedRoom?.description || ""}
              </p>
            </div>
            <div className="chat-actions">
              <span style={{ fontSize: 14, color: "#888" }}>
                {selectedRoom?.id === 0 ? null : (
                  <>在线成员：{roomUsers.length}</>
                )}
              </span>
              {selectedRoom &&
                selectedRoom.id !== 0 &&
                isUserInRoom(selectedRoom.id) && (
                  <ChatSettingsMenu
                    onUploadAvatar={() => setShowAvatarModal(true)}
                    onSetNickname={() => setShowNicknameModal(true)}
                    onQuitRoom={handleQuitRoom}
                  />
                )}
            </div>
          </div>
        </div>
        {error && (
          <div className="error-message">
            {error}
            <button onClick={() => setError(null)}>×</button>
          </div>
        )}
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
              <p>暂无可用的聊天室</p>
            </div>
          )}
          {selectedRoom?.id === 0 && !loading && (
            <div className="loading-indicator">
              <div style={{ fontSize: 48, marginBottom: 16 }}>💬</div>
              <p>请选择左侧的聊天室以开始聊天</p>
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
              <div ref={messagesEndRef} />
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
                        listRef.current.scrollToItem(
                          messages.length - 1,
                          "end"
                        );
                        // 兜底
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
        <div className="chat-input-container">
          <div className="chat-input-wrapper">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={
                selectedRoom?.id === 0 ? "请先选择聊天室..." : "输入消息..."
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
      {showAvatarModal && (
        <div className="modal-backdrop">
          <div className="modal-container">
            <h2>上传头像</h2>
            <div className="form-group">
              <label>选择图片文件</label>
              <div className="file-upload-container">
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarFileSelect}
                  id="avatar-file-input"
                  className="file-input-hidden"
                />
                <label
                  htmlFor="avatar-file-input"
                  className="file-upload-button"
                >
                  <div className="file-upload-icon">📁</div>
                  <div className="file-upload-text">
                    <span className="file-upload-title">点击选择图片</span>
                    <span className="file-upload-subtitle">
                      支持 JPG、PNG、GIF 格式，最大 5MB
                    </span>
                  </div>
                </label>
              </div>
              {selectedAvatarFile && (
                <>
                  <p className="file-selected-text">
                    已选择: {selectedAvatarFile.name}
                  </p>
                  <div className="avatar-preview">
                    <div className="avatar-preview-container">
                      <img
                        src={URL.createObjectURL(selectedAvatarFile)}
                        alt="预览"
                        className="avatar-preview-image"
                      />
                    </div>
                  </div>
                </>
              )}
            </div>
            <div className="modal-actions">
              <button
                type="button"
                onClick={() => {
                  setShowAvatarModal(false);
                  setSelectedAvatarFile(null);
                }}
                disabled={uploadingAvatar}
              >
                取消
              </button>
              <button
                type="button"
                onClick={handleUploadAvatar}
                disabled={!selectedAvatarFile || uploadingAvatar}
              >
                {uploadingAvatar ? "上传中..." : "上传"}
              </button>
            </div>
          </div>
        </div>
      )}
      {showNicknameModal && (
        <div className="modal-backdrop">
          <div className="modal-container">
            <h2>设置昵称</h2>
            <div className="form-group">
              <label>昵称</label>
              <input
                type="text"
                value={newNickname}
                onChange={(e) => setNewNickname(e.target.value)}
                placeholder="请输入新的昵称"
                maxLength={20}
                style={{ marginBottom: "10px" }}
              />
              <p style={{ fontSize: "12px", color: "#888" }}>
                当前昵称: {myChatRoomUser?.displayNickname || "未设置"}
              </p>
            </div>
            <div className="modal-actions">
              <button
                type="button"
                onClick={() => {
                  setShowNicknameModal(false);
                  setNewNickname("");
                }}
                disabled={settingNickname}
              >
                取消
              </button>
              <button
                type="button"
                onClick={handleSetNickname}
                disabled={!newNickname.trim() || settingNickname}
              >
                {settingNickname ? "设置中..." : "设置"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
export default ChatPage;

