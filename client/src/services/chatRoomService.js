import { chatAPI } from "./api";

const chatRoomService = {
  /**
   * 获取聊天室列表
   * @returns {Promise<Array>} 聊天室数组
   */
  getRooms: async () => chatAPI.getRooms(),

  /**
   * 获取指定聊天室的消息
   * @param {string|number} chatRoomId
   * @returns {Promise<Array>} 消息数组
   */
  getMessages: async (chatRoomId) => chatAPI.getMessages(chatRoomId),

  /**
   * 获取指定聊天室在某时间以后的=的消息
   * @param {string|number} chatRoomId
   * @param {string} timeStamp
   * @returns {Promise<Array>} 消息数组
   */
  getMessagesAfter: async (chatRoomId, timeStamp) => chatAPI.getMessagesAfter(chatRoomId, timeStamp),

  /**
   * 发送消息到指定聊天室
   * @param {string|number} chatRoomId
   * @param {string} content
   * @returns {Promise<Object>} 发送结果
   */
  sendMessage: async (chatRoomId, content) => chatAPI.sendMessage(chatRoomId, content),

  /**
   * 获取自己是否在房间里
   * @param {string|number} chatRoomId
   * @returns {boolean} 结果
   */
  isInRoom: async (chatRoomId) => chatAPI.isInRoom(chatRoomId),

  /**
   * 获取自己在聊天室的用户
   * @param {string|number} chatRoomId
   * @returns {Promise<Object>} 当前用户在聊天室的用户
   */
  getCurrentUserByRoomId: async (chatRoomId) => chatAPI.getCurrentUserByRoomId(chatRoomId),

  /**
   * 按chatRoomUserId获取聊天室成员
   * @param {string|number} chatRoomId
   * @param {string|number} chatRoomUserId
   * @returns {Promise<Array>} 成员数组
   */
  getRoomUserByRoomUserId: async (chatRoomId, chatRoomUserId) => chatAPI.getRoomUserByRoomUserId(chatRoomId, chatRoomUserId),

  /**
   * 获取聊天室成员
   * @param {string|number} chatRoomId
   * @returns {Promise<Array>} 成员数组
   */
  getRoomUsers: async (chatRoomId) => chatAPI.getRoomUsers(chatRoomId),

  /**
   * 创建聊天室
   * @param {Object} roomData - { name, description, type }
   * @returns {Promise<Object>} 新聊天室
   */
  createRoom: async (roomData) => chatAPI.createRoom(roomData),

  /**
   * 加入聊天室
   * @param {string|number} chatRoomId
   * @returns {Promise<Object>} 加入结果
   */
  joinRoom: async (chatRoomId) => chatAPI.joinRoom(chatRoomId),

  /**
   * 上传聊天室头像
   * @param {string|number} chatRoomId
   * @param {FormData} formData - 包含头像文件的FormData
   * @returns {Promise<Object>} 上传结果
   */
  uploadAvatar: async (chatRoomId, formData) => chatAPI.uploadAvatar(chatRoomId, formData),

  /**
   * 设置聊天室昵称
   * @param {string|number} chatRoomId
   * @param {string} displayNickname - 要设置的昵称
   * @returns {Promise<Object>} 设置结果
   */
  setNickname: async (chatRoomId, displayNickname) => chatAPI.setNickname(chatRoomId, displayNickname),

  /**
   * 退出聊天室
   * @param {string|number} chatRoomUserId
   * @returns {Promise<Object>} 退出结果
   */
  quitRoom: async (chatRoomUserId) => chatAPI.quitRoom(chatRoomUserId),

  /**
   * 分页获取聊天室消息
   * @param {string|number} chatRoomId
   * @param {Object} params - { before: string, limit: number }
   * @returns {Promise<Array>} 消息数组
   */
  getMessagesPaged: async (chatRoomId, { before, limit }) => chatAPI.getMessagesPaged(chatRoomId, { before, limit }),
};

export default chatRoomService;