import { postAPI } from './api';

export const moods = [
  { emoji: '😊', label: '开心', value: 'HAPPY' },
  { emoji: '😢', label: '难过', value: 'SAD' },
  { emoji: '😡', label: '愤怒', value: 'ANGRY' },
  { emoji: '😰', label: '焦虑', value: 'ANXIOUS' },
  { emoji: '😴', label: '疲惫', value: 'TIRED' },
  { emoji: '🤗', label: '温暖', value: 'GRATEFUL' },
  { emoji: '💪', label: '充满力量', value: 'MOTIVATED' },
  { emoji: '🎉', label: '兴奋', value: 'EXCITED' },
  { emoji: '😕', label: '困惑', value: 'CONFUSED' },
  { emoji: '😌', label: '平静', value: 'PEACEFUL' },
  { emoji: '😐', label: '平和', value: 'CALM' },
  // 可根据后端MoodType枚举继续补充
];

export const warmComments = [
  '加油！你的分享很温暖，希望你每天都开心！',
  '你很棒，继续保持积极的心态！',
  '感谢你的分享，愿你每天都有好心情！',
  '无论遇到什么，都要相信自己会越来越好！',
  '你的故事很打动我，祝你幸福快乐！'
];

export const postService = {
  getCommunityPosts: postAPI.getCommunityPosts,
  getPostsByFilter: postAPI.getPostsByFilter,
  getPostsByMood: postAPI.getPostsByMood,
  getPostById: postAPI.getPostById,
  createPost: postAPI.createPost,
  toggleLike: postAPI.toggleLike,
  deletePost: postAPI.deletePost,
  searchPosts: postAPI.searchPosts,
  getCommentsByPostId: postAPI.getCommentsByPostId,
  addCommentToPost: postAPI.addCommentToPost,
  toggleLikeComment: postAPI.toggleLikeComment,
  addReplyToComment: (commentId, replyData, topCommentId, postId) => postAPI.addReplyToComment(commentId, replyData, topCommentId, postId),
  getCommentById: postAPI.getCommentById,
  getAllRepliesRecursive: (commentId, page = 0, size = 10) => postAPI.getAllRepliesRecursive(commentId, page, size),
  getLastPageCommentsOfPost: (postId, size = 10) => postAPI.getLastPageCommentsOfPost(postId, size),
  getLastPageRepliesOfTopComment: (commentId, size = 10) => postAPI.getLastPageRepliesOfTopComment(commentId, size),
  getCommentsOfPostByPage: (postId, page = 0, size = 10) => postAPI.getCommentsOfPostByPage(postId, page, size),
  getRepliesByTopCommentPage: (commentId, page = 0, size = 10) => postAPI.getRepliesByTopCommentPage(commentId, page, size),
  getPageByCommentId:(commentId, parentId, size = 10) =>postAPI.getPageByCommentId(commentId, parentId, size),
  getPostsByUserId:(userId, page = 0, size = 10) => postAPI.getPostsByUserId(userId, page, size),
};
