// 聊天本地存储工具

// 获取自己在房间的chatRoomUser
export function getMyChatRoomUser(userId, chatRoomId) {
  return JSON.parse(localStorage.getItem(`chatRoomUser_me_${userId}_${chatRoomId}`));
}
export function setMyChatRoomUser(userId, chatRoomId, data) {
  localStorage.setItem(`chatRoomUser_me_${userId}_${chatRoomId}`, JSON.stringify(data));
}
// 获取其它chatRoomUser
export function getChatRoomUser(userId, chatRoomId) {
  return JSON.parse(localStorage.getItem(`chatRoomUser_${userId}_${chatRoomId}`)) || [];
}
export function setChatRoomUser(userId, chatRoomId, data) {
  localStorage.setItem(`chatRoomUser_${userId}_${chatRoomId}`, JSON.stringify(data));
}
// 获取消息列表
export function getChatMessages(userId, chatRoomId) {
  return JSON.parse(localStorage.getItem(`chatMessage_${userId}_${chatRoomId}`)) || [];
}
export function setChatMessages(userId, chatRoomId, messages) {
  localStorage.setItem(`chatMessage_${userId}_${chatRoomId}`, JSON.stringify(messages));
}
// 合并去重消息
export function mergeById(arr1, arr2) {
  const map = {};
  const safeArr1 = arr1 || [];
  const safeArr2 = arr2 || [];
  [...safeArr1, ...safeArr2].forEach(item => { map[item.id] = item; });
  return Object.values(map);
}
// 获取某个chatRoomUserId的对象
export function findChatRoomUser(userId, chatRoomId, chatRoomUserId) {
  const arr = getChatRoomUser(userId, chatRoomId);
  return arr.find(u => String(u.chatRoomUserId) === String(chatRoomUserId));
}
// 更新/插入某个chatRoomUser
export function upsertChatRoomUser(userId, chatRoomId, userObj) {
  let arr = getChatRoomUser(userId, chatRoomId);
  const idx = arr.findIndex(u => String(u.chatRoomUserId) === String(userObj.chatRoomUserId));
  if (idx >= 0) {
    arr[idx] = userObj;
  } else {
    arr.push(userObj);
  }
  setChatRoomUser(userId, chatRoomId, arr);
} 