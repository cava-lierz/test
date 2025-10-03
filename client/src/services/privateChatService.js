import { chatAPI, request, getCurrentUserId } from "./api";

const privateChatService = {
  /**
   * 获取私聊聊天室列表
   * @returns {Promise<Array>} 私聊聊天室数组
   */
  getPrivateRooms: async () => {
    const userId = getCurrentUserId();
    return await request("/chat-room/private/list", { method: "GET" }, userId);
  },

  /**
   * 创建或获取与指定用户的私聊聊天室
   * @param {number} targetUserId - 目标用户ID
   * @returns {Promise<Object>} 私聊聊天室信息
   */
  createOrGetPrivateRoom: async (targetUserId) => {
    const userId = getCurrentUserId();
    return await request(
      "/chat-room/private/create",
      {
        method: "POST",
        body: JSON.stringify({ targetUserId }),
      },
      userId
    );
  },

  /**
   * 获取私聊聊天室的消息
   * @param {string|number} chatRoomId
   * @returns {Promise<Array>} 消息数组
   */
  getMessages: async (chatRoomId) => chatAPI.getMessages(chatRoomId),

  /**
   * 获取指定聊天室在某时间以后的新消息
   * @param {string|number} chatRoomId
   * @param {string} timeStamp
   * @returns {Promise<Array>} 消息数组
   */
  getMessagesAfter: async (chatRoomId, timeStamp) => chatAPI.getMessagesAfter(chatRoomId, timeStamp),

  /**
   * 发送私聊消息
   * @param {string|number} chatRoomId
   * @param {string} content
   * @returns {Promise<Object>} 发送结果
   */
  sendMessage: async (chatRoomId, content) => chatAPI.sendMessage(chatRoomId, content),

  /**
   * 检查是否在私聊聊天室中
   * @param {string|number} chatRoomId
   * @returns {boolean} 结果
   */
  isInRoom: async (chatRoomId) => chatAPI.isInRoom(chatRoomId),

  /**
   * 获取自己在私聊聊天室的用户信息
   * @param {string|number} chatRoomId
   * @returns {Promise<Object>} 当前用户在聊天室的用户
   */
  getCurrentUserByRoomId: async (chatRoomId) => chatAPI.getCurrentUserByRoomId(chatRoomId),

  /**
   * 按chatRoomUserId获取私聊聊天室成员
   * @param {string|number} chatRoomId
   * @param {string|number} chatRoomUserId
   * @returns {Promise<Array>} 成员数组
   */
  getRoomUserByRoomUserId: async (chatRoomId, chatRoomUserId) => chatAPI.getRoomUserByRoomUserId(chatRoomId, chatRoomUserId),

  /**
   * 获取私聊聊天室成员（应该只有2个人）
   * @param {string|number} chatRoomId
   * @returns {Promise<Array>} 成员数组
   */
  getRoomUsers: async (chatRoomId) => chatAPI.getRoomUsers(chatRoomId),

  /**
   * 退出私聊聊天室
   * @param {string|number} chatRoomUserId
   * @returns {Promise<Object>} 退出结果
   */
  quitRoom: async (chatRoomUserId) => chatAPI.quitRoom(chatRoomUserId),

  /**
   * 删除私聊聊天室（双方都退出后）
   * @param {string|number} chatRoomId
   * @returns {Promise<Object>} 删除结果
   */
  deletePrivateRoom: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room/private/${chatRoomId}`,
      { method: "DELETE" },
      userId
    );
  },

  /**
   * 获取私聊聊天室的对方用户信息
   * @param {string|number} chatRoomId
   * @returns {Promise<Object>} 对方用户信息
   */
  getOtherUser: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room/private/${chatRoomId}/other-user`,
      { method: "GET" },
      userId
    );
  },

  /**
   * 分页获取私聊聊天室的消息
   * @param {string|number} chatRoomId
   * @param {Object} options { before, limit }
   * @returns {Promise<Array>} 消息数组
   */
  getMessagesPaged: async (chatRoomId, { before, limit }) => chatAPI.getMessagesPaged(chatRoomId, { before, limit }),
};

export default privateChatService; 