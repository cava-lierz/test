/**
 * API服务层 - 处理所有后端接口调用
 *
 * @author Mentara Team
 */

import { API_CONFIG, AUTH_CONFIG } from "../utils/constants";

// 通用请求函数
const request = async (url, options = {}, userId) => {
  const config = {
    headers: {
      ...options.headers,
    },
    timeout: API_CONFIG.TIMEOUT,
    ...options,
  };

  // 如果不是FormData，设置默认的Content-Type
  if (!(options.body instanceof FormData)) {
    config.headers["Content-Type"] = "application/json";
  }

  // 如果有token，添加到请求头
  if (userId) {
    const token = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } else {
    const token = localStorage.getItem(AUTH_CONFIG.TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }

  try {
    let response = await fetch(`${API_CONFIG.BASE_URL}${url}`, config);

    if (response.status === 401) {
      // accessToken 过期，尝试刷新
      const refreshToken = userId
        ? localStorage.getItem(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${userId}`)
        : localStorage.getItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);

      if (refreshToken) {
        try {
          const refreshRes = await fetch(
            `${API_CONFIG.BASE_URL}/auth/refresh`,
            {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ refreshToken }),
            }
          );

          if (refreshRes.ok) {
            const refreshData = await refreshRes.json();
            // 更新本地token
            if (userId) {
              localStorage.setItem(
                `${AUTH_CONFIG.TOKEN_KEY}_${userId}`,
                refreshData.accessToken
              );
              localStorage.setItem(
                `${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${userId}`,
                refreshData.refreshToken
              );
            } else {
              localStorage.setItem(
                AUTH_CONFIG.TOKEN_KEY,
                refreshData.accessToken
              );
              localStorage.setItem(
                AUTH_CONFIG.REFRESH_TOKEN_KEY,
                refreshData.refreshToken
              );
            }
            // 重试原请求
            config.headers.Authorization = `Bearer ${refreshData.accessToken}`;
            response = await fetch(`${API_CONFIG.BASE_URL}${url}`, config);
            if (!response.ok) {
              const errorData = await response.json().catch(() => ({}));
              throw new Error(
                errorData.message ||
                  `HTTP ${response.status}: ${response.statusText}`
              );
            }
          } else {
            // refresh token也失效了，清除所有认证信息
            // Refresh token已失效，清除认证信息
            if (userId) {
              authAPI.logout(userId);
            } else {
              // 清除所有相关的认证信息
              localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
              localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
              localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
            }
            // 直接清除认证信息并跳转到登录页面
            if (userId) {
              authAPI.logout(userId);
            } else {
              localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
              localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
              localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
            }
            window.location.href = "/login";
            throw new Error("登录已过期，请重新登录");
          }
        } catch (refreshError) {
          // Token刷新失败
          // 清除所有认证信息
          if (userId) {
            authAPI.logout(userId);
          } else {
            localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
            localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
            localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
          }
          // 直接清除认证信息并跳转到登录页面
          if (userId) {
            authAPI.logout(userId);
          } else {
            localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
            localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
            localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
          }
          window.location.href = "/login";
          throw new Error("登录已过期，请重新登录");
        }
      } else {
        // 没有refresh token，清除认证信息并重定向
        // 没有找到refresh token，清除认证信息
        if (userId) {
          authAPI.logout(userId);
        } else {
          localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
          localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
          localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
        }
        // 直接清除认证信息并跳转到登录页面
        if (userId) {
          authAPI.logout(userId);
        } else {
          localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
          localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
          localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);
        }
        window.location.href = "/login";
        throw new Error("登录已过期，请重新登录");
      }
    }

    // 检查HTTP状态码
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
      try {
        const errorData = await response.json();
        if (errorData.error) {
          errorMessage = errorData.error;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        }
      } catch (e) {
        // 如果无法解析JSON，使用默认错误信息
      }
      throw new Error(errorMessage);
    }

    // 检查响应是否为空
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      const data = await response.json();
      return data;
    } else {
      // 对于非JSON响应（如删除操作），返回null
      return null;
    }
  } catch (error) {
    throw error;
  }
};

// 获取当前登录用户id的工具函数
export function getCurrentUserId() {
  // 方法1: 从全局user_info获取
  const userInfo = localStorage.getItem(AUTH_CONFIG.USER_INFO_KEY);
  if (userInfo) {
    try {
      const parsed = JSON.parse(userInfo);
      return parsed.id;
    } catch (e) {
      // 解析失败，继续使用其他方法
    }
  }

  // 方法2: 从localStorage中查找所有token key，提取userId
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && key.startsWith(`${AUTH_CONFIG.TOKEN_KEY}_`)) {
      const userId = key.replace(`${AUTH_CONFIG.TOKEN_KEY}_`, "");
      const token = localStorage.getItem(key);
      if (token) {
        return userId;
      }
    }
  }

  return null;
}

// 认证相关API
export const authAPI = {
  /**
   * 用户登录
   * @param {Object} loginData - 登录数据
   * @param {string} loginData.username - 用户名（学号）
   * @param {string} loginData.password - 密码
   * @returns {Promise<Object>} 登录响应
   */
  login: async (loginData) => {
    try {
      const response = await request("/auth/signin", {
        method: "POST",
        body: JSON.stringify({
          username: loginData.username,
          password: loginData.password,
        }),
      });

      // 保存JWT token到localStorage
      if (response.accessToken && response.id) {
        localStorage.setItem(
          `${AUTH_CONFIG.TOKEN_KEY}_${response.id}`,
          response.accessToken
        );
        localStorage.setItem(
          `${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${response.id}`,
          response.refreshToken
        );
        localStorage.setItem(
          `${AUTH_CONFIG.USER_INFO_KEY}_${response.id}`,
          JSON.stringify({
            id: response.id,
            username: response.username,
            nickname: response.nickname,
            email: response.email,
            role: response.role,
            avatar: response.avatar,
          })
        );
        // 额外存一份user_info用于下次初始化时获取userId
        localStorage.setItem(
          AUTH_CONFIG.USER_INFO_KEY,
          JSON.stringify({
            id: response.id,
            username: response.username,
            nickname: response.nickname,
            email: response.email,
            role: response.role,
            avatar: response.avatar,
          })
        );
      }

      return response;
    } catch (error) {
      console.error("登录失败:", error);
      throw error;
    }
  },

  /**
   * 用户注册
   * @param {Object} registerData - 注册数据
   * @param {string} registerData.username - 用户名（学号）
   * @param {string} registerData.email - 邮箱
   * @param {string} registerData.password - 密码
   * @param {string} registerData.nickname - 昵称
   * @returns {Promise<Object>} 注册响应
   */
  register: async (registerData) => {
    try {
      const response = await request("/auth/signup", {
        method: "POST",
        body: JSON.stringify({
          username: registerData.username,
          email: registerData.email,
          password: registerData.password,
          nickname: registerData.nickname,
        }),
      });

      return response;
    } catch (error) {
      console.error("注册失败:", error);
      throw error;
    }
  },

  /**
   * 验证token有效性
   * @returns {Promise<boolean>} token是否有效
   */
  validateToken: async (userId) => {
    try {
      const token = userId
        ? localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`)
        : null;
      if (!token) {
        return false;
      }
      await request("/posts", { method: "GET" }, userId);
      return true;
    } catch (error) {
      return false;
    }
  },

  /**
   * 退出登录
   */
  logout: (userId) => {
    if (userId) {
      localStorage.removeItem(`${AUTH_CONFIG.USER_INFO_KEY}`);
      localStorage.removeItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
      localStorage.removeItem(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${userId}`);
      localStorage.removeItem(`${AUTH_CONFIG.USER_INFO_KEY}_${userId}`);
      localStorage.removeItem(`${AUTH_CONFIG.LOGIN_STATE_KEY}_${userId}`);
    } else {
      // 如果没有指定userId，清除所有相关的认证信息
      localStorage.removeItem(AUTH_CONFIG.TOKEN_KEY);
      localStorage.removeItem(AUTH_CONFIG.REFRESH_TOKEN_KEY);
      localStorage.removeItem(AUTH_CONFIG.USER_INFO_KEY);

      // 清除所有以特定前缀开头的键
      const keysToRemove = [];
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (
          key &&
          (key.startsWith(`${AUTH_CONFIG.TOKEN_KEY}_`) ||
            key.startsWith(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_`) ||
            key.startsWith(`${AUTH_CONFIG.USER_INFO_KEY}_`) ||
            key.startsWith(`${AUTH_CONFIG.LOGIN_STATE_KEY}_`))
        ) {
          keysToRemove.push(key);
        }
      }
      keysToRemove.forEach((key) => localStorage.removeItem(key));
    }
  },

  /**
   * 获取当前用户信息
   * @returns {Object|null} 用户信息
   */
  getCurrentUser: (userId) => {
    if (userId) {
      const userInfo = localStorage.getItem(
        `${AUTH_CONFIG.USER_INFO_KEY}_${userId}`
      );
      return userInfo ? JSON.parse(userInfo) : null;
    }
    return null;
  },

  /**
   * 检查是否已登录
   * @returns {boolean} 是否已登录
   */
  isLoggedIn: (userId) => {
    if (userId) {
      return !!localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);
    }
    return false;
  },

  /**
   * 重置密码
   * @param {Object} resetData - 重置密码数据
   * @param {string} resetData.studentId - 学号
   * @param {string} resetData.email - 邮箱
   * @param {string} resetData.newPassword - 新密码
   * @param {string} resetData.confirmPassword - 确认新密码
   * @returns {Promise<Object>} 重置响应
   */
  resetPassword: async (resetData) => {
    try {
      const response = await request("/auth/reset-password", {
        method: "POST",
        body: JSON.stringify(resetData),
      });

      return response;
    } catch (error) {
      console.error("密码重置失败:", error);
      throw error;
    }
  },
};

// 统计相关API
export const statisticsAPI = {
  // 获取周报数据
  getWeeklyStats: async (year, week) => {
    const userId = getCurrentUserId();
    try {
      const response = await request(
        `/statistics/weekly?year=${year}&week=${week}`,
        {},
        userId
      );
      return response;
    } catch (error) {
      console.error("获取周报数据失败:", error);
      throw error;
    }
  },

  // 获取月报数据
  getMonthlyStats: async (year, month) => {
    const userId = getCurrentUserId();
    try {
      const response = await request(
        `/statistics/monthly?year=${year}&month=${month}`,
        {},
        userId
      );
      return response;
    } catch (error) {
      console.error("获取月报数据失败:", error);
      throw error;
    }
  },
};

// 帖子相关API
export const postAPI = {
  /**
   * 获取社区帖子
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @param {string} filter - 筛选条件（全部、最新、最热、心情）
   * @param {string} tags - 标签ID，逗号分隔
   * @returns {Promise<Object>} 帖子列表
   */
  getCommunityPosts: async (
    page = 0,
    size = 10,
    filter = null,
    tags = null
  ) => {
    const userId = getCurrentUserId();
    let url = `/posts?page=${page}&size=${size}`;
    if (filter) {
      url += `&filter=${encodeURIComponent(filter)}`;
    }
    if (tags) {
      url += `&tags=${encodeURIComponent(tags)}`;
    }
    const response = await request(url, {}, userId);
    return response;
  },

  /**
   * 按筛选条件获取帖子
   * @param {string} filterType - 筛选类型（全部、最新、最热、心情）
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 帖子列表
   */
  getPostsByFilter: async (filterType, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/filter/${encodeURIComponent(
        filterType
      )}?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 按心情类型获取帖子
   * @param {string} moodType - 心情类型
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 帖子列表
   */
  getPostsByMood: async (moodType, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/mood/${encodeURIComponent(moodType)}?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 获取帖子详情
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 帖子详情
   */
  getPostById: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(`/posts/${postId}`, {}, userId);
    return response;
  },

  getPostsByUserId: async (userId, page = 0, size = 10) => {
    // 这里userId是目标用户id，鉴权用当前登录用户id
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/posts/user/${userId}?page=${page}&size=${size}`,
      { method: "GET" },
      currentUserId
    );
    return response;
  },

  /**
   * 创建帖子
   * @param {Object} postData - 帖子数据，需包含title和content
   * @param {string} postData.title - 帖子标题
   * @param {string} postData.content - 帖子内容
   * @returns {Promise<Object>} 创建结果
   */
  createPost: async (postData) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/posts",
      {
        method: "POST",
        body: JSON.stringify(postData), // postData需包含title和content
      },
      userId
    );
    return response;
  },

  /**
   * 点赞/取消点赞帖子
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 点赞结果
   */
  toggleLike: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/${postId}/like`,
      {
        method: "POST",
      },
      userId
    );
    return response;
  },

  /**
   * 删除帖子
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 删除结果
   */
  deletePost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/${postId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 搜索帖子
   * @param {string} keyword - 搜索关键词
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 搜索结果
   */
  searchPosts: async (keyword, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/search?keyword=${keyword}&page=${page}&size=${size}`,
      { method: "GET" },
      userId
    );
    return response;
  },

  // 获取帖子的评论
  getCommentsByPostId: async (postId, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/${postId}/comments?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  // 发表评论
  addCommentToPost: async (postId, commentData) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/comments`,
      {
        method: "POST",
        body: JSON.stringify({ ...commentData, postId: postId }),
      },
      userId
    );
    return response.data;
  },

  // 点赞/取消点赞评论
  toggleLikeComment: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/comments/${commentId}/like`,
      {
        method: "POST",
      },
      userId
    );
    return response.data;
  },

  // 回复评论
  addReplyToComment: async (commentId, replyData, topCommentId, postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/comments`,
      {
        method: "POST",
        body: JSON.stringify({
          ...replyData,
          parentId: commentId,
          topCommentId: topCommentId,
          postId: postId,
        }),
      },
      userId
    );
    return response;
  },

  /**
   * 获取所有标签
   * @returns {Promise<Array>} 标签数组
   */
  getTags: async () => {
    // 标签一般不需要鉴权
    const userId = getCurrentUserId();
    return await request("/tags", {}, userId);
  },

  // 获取单条评论详情
  getCommentById: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/comments/${commentId}`,
      { method: "GET" },
      userId
    );
    return response;
  },

  // 获取某条评论的直接回复
  getRepliesByParentId: async (commentId) => {
    const userId = getCurrentUserId();
    const res = await request(`/comments/${commentId}/replies`, {}, userId);
    return res.content || [];
  },

  // 获取一条评论的所有reply并按createdAt排序
  getAllRepliesRecursive: async (commentId, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const allReplies = await request(
      `/comments/${commentId}/replies-to-top-comment?page=${page}&size=${size}`,
      {},
      userId
    );
    const res = allReplies.content || [];
    res.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    return { content: res, last: allReplies.last };
  },

  // 分页获取帖子的评论
  getCommentsOfPostByPage: async (postId, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/posts/${postId}/comments?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  getRepliesByTopCommentPage: async (commentId, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    return await request(
      `/comments/${commentId}/replies-to-top-comment?page=${page}&size=${size}`,
      {},
      userId
    );
  },

  //获取commentId对应comment所在page
  getPageByCommentId: async (commentId, parentId, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/comments/${commentId}/goto/${parentId}?size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 举报帖子
   * @param {number} postId - 帖子ID
   * @param {string} reason - 举报原因
   * @returns {Promise<Object>} 举报结果
   */
  reportPost: async (postId, reason = "") => {
    const userId = getCurrentUserId();
    const response = await request(
      "/reports/posts",
      {
        method: "POST",
        body: JSON.stringify({ postId, reason }),
      },
      userId
    );
    return response;
  },
};

// 心情记录相关API
export const moodAPI = {
  /**
   * 心情打卡
   * @param {Object} moodData - 心情数据
   * @returns {Promise<Object>} 打卡结果
   */
  checkin: async (moodData) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/checkins",
      {
        method: "POST",
        body: JSON.stringify(moodData),
      },
      userId
    );
    return response;
  },

  /**
   * 获取今天的心情记录
   * @returns {Promise<Object>} 今日心情记录
   */
  getTodayMood: async () => {
    const userId = getCurrentUserId();
    const response = await request("/checkins/today", {}, userId);
    return response;
  },

  /**
   * 获取最近心情记录
   * @param {number} days - 天数
   * @returns {Promise<Object>} 心情记录列表
   */
  getRecentMoods: async (days = 7) => {
    const userId = getCurrentUserId();
    const response = await request(`/checkins/recent?days=${days}`, {}, userId);
    return response;
  },

  /**
   * 获取当前周的心情记录
   * @returns {Promise<Array>} 当前周心情记录
   */
  getCurrentWeek: async () => {
    const userId = getCurrentUserId();
    const response = await request("/checkins/current-week", {}, userId);
    return response;
  },

  /**
   * 获取指定周的心情记录
   * @param {number} weekOffset - 周偏移量（1=前一周，2=前两周）
   * @returns {Promise<Array>} 指定周心情记录
   */
  getWeek: async (weekOffset) => {
    const userId = getCurrentUserId();
    const response = await request(`/checkins/week/${weekOffset}`, {}, userId);
    return response;
  },

  /**
   * 获取指定日期所在周的心情记录
   * @param {string} date - 日期字符串 (YYYY-MM-DD)
   * @returns {Promise<Array>} 指定日期所在周心情记录
   */
  getWeekByDate: async (date) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/checkins/week-by-date?date=${date}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 获取用户的心情记录
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户心情记录
   */
  getUserCheckins: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/checkins/user/${userId}`,
      {},
      currentUserId
    );
    return response;
  },

  /**
   * 获取指定用户的心情记录（分页）
   * @param {number} userId - 用户ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 用户心情记录
   */
  getUserMoods: async (userId, page = 0, size = 10) => {
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/checkins/user/${userId}?page=${page}&size=${size}`,
      {},
      currentUserId
    );
    return response;
  },
};

// 用户相关API
export const userAPI = {
  /**
   * 获取用户信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户信息
   */
  getUserProfile: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(`/users/${userId}`, {}, currentUserId);
    return response;
  },

  /**
   * 根据ID获取用户详细信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户详细信息
   */
  getUserById: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(`/users/${userId}`, {}, currentUserId);
    return response;
  },

  /**
   * 更新用户信息
   * @param {Object} userData - 用户数据
   * @returns {Promise<Object>} 更新结果
   */
  updateProfile: async (userData) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/users/profile",
      {
        method: "PUT",
        body: JSON.stringify(userData),
      },
      userId
    );
    return response;
  },

  /**
   * 获取当前用户统计信息
   * @returns {Promise<Object>} 用户统计信息
   */
  getCurrentUserStats: async () => {
    const userId = getCurrentUserId();
    const response = await request("/users/profile/stats", {}, userId);
    return response;
  },

  /**
   * 获取指定用户统计信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户统计信息
   */
  getUserStatsById: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(`/users/${userId}/stats`, {}, currentUserId);
    return response;
  },

  /**
   * 更新用户隐私设置
   * @param {boolean} isProfilePublic - 是否公开个人信息
   * @returns {Promise<Object>} 更新结果
   */
  updatePrivacySettings: async (isProfilePublic) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/users/profile/privacy",
      {
        method: "PUT",
        body: JSON.stringify({ isProfilePublic }),
      },
      userId
    );
    return response;
  },

  /**
   * 关注用户
   * @param {number} followedUserId - 被关注用户ID
   * @returns {Promise<Object>} 关注结果
   */
  followUser: async (followedUserId) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/user-follows/follow",
      {
        method: "POST",
        body: JSON.stringify({ followedUserId }),
      },
      userId
    );
    return response;
  },

  /**
   * 取消关注用户
   * @param {number} followedUserId - 被关注用户ID
   * @returns {Promise<Object>} 取消关注结果
   */
  unfollowUser: async (followedUserId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/user-follows/unfollow/${followedUserId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 获取已关注用户列表
   * @returns {Promise<Object>} 关注用户列表
   */
  getFollowedUsers: async () => {
    const userId = getCurrentUserId();
    return await request("/user-follows/followed-users", {}, userId);
  },

  /**
   * 获取关注统计信息
   * @returns {Promise<Object>} 关注统计
   */
  getFollowStats: async () => {
    const userId = getCurrentUserId();
    return await request("/user-follows/stats", {}, userId);
  },

  /**
   * 检查是否已关注用户
   * @param {number} followedUserId - 被关注用户ID
   * @returns {Promise<Object>} 关注状态
   */
  checkFollowStatus: async (followedUserId) => {
    const userId = getCurrentUserId();
    return await request(`/user-follows/check/${followedUserId}`, {}, userId);
  },
};

// 管理员相关API
export const adminAPI = {
  /**
   * 获取管理员统计数据
   * @returns {Promise<Object>} 管理员统计数据
   */
  getAdminStats: async () => {
    const userId = getCurrentUserId();
    const response = await request("/admin/stats", {}, userId);
    return response;
  },

  /**
   * 获取所有用户列表（分页）
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 用户列表
   */
  getAllUsers: async (page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/users?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 搜索用户列表（分页）
   * @param {string} keyword - 搜索关键词
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 用户列表
   */
  searchUsers: async (keyword, page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/users/search?keyword=${encodeURIComponent(
        keyword
      )}&page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 获取用户详细信息
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 用户详细信息
   */
  getUserDetails: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(`/admin/users/${userId}`, {}, currentUserId);
    return response;
  },

  /**
   * 禁用用户
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 操作结果
   */
  suspendUser: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/admin/users/${userId}/suspend`,
      {
        method: "PUT",
      },
      currentUserId
    );
    return response;
  },

  /**
   * 启用用户
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 操作结果
   */
  activateUser: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/admin/users/${userId}/activate`,
      {
        method: "PUT",
      },
      currentUserId
    );
    return response;
  },

  /**
   * 删除用户
   * @param {number} userId - 用户ID
   * @returns {Promise<Object>} 操作结果
   */
  deleteUser: async (userId) => {
    const currentUserId = getCurrentUserId();
    const response = await request(
      `/admin/users/${userId}`,
      {
        method: "DELETE",
      },
      currentUserId
    );
    return response;
  },

  /**
   * 获取被举报的帖子列表
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 被举报的帖子列表
   */
  getReportedPosts: async (page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/reported-posts?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 删除被举报的帖子（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  deleteReportedPost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 忽略帖子举报（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  ignorePostReports: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/ignore-reports`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 通过帖子举报（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  approveReportedPost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/approve-report`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 获取待审核的帖子列表
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @param {string} state - 帖子状态筛选（WAITING, INVALID, 可选）
   * @returns {Promise<Object>} 待审核的帖子列表
   */
  getPendingPosts: async (page = 0, size = 10, state = null) => {
    const userId = getCurrentUserId();
    let url = `/admin/pending-posts?page=${page}&size=${size}`;
    if (state) {
      url += `&state=${encodeURIComponent(state)}`;
    }
    const response = await request(url, {}, userId);
    return response;
  },

  /**
   * 通过帖子审核（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  approvePost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/approve`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 拒绝帖子审核（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  rejectPost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/reject`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 恢复帖子（从INVALID状态改为VALID）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  restorePost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/restore`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 删除帖子（管理员操作）
   * @param {number} postId - 帖子ID
   * @returns {Promise<Object>} 操作结果
   */
  deletePost: async (postId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 获取被举报的评论列表
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @param {string} state - 状态筛选（waiting/valid/invalid）
   * @returns {Promise<Object>} 被举报的评论列表
   */
  getReportedComments: async (page = 0, size = 10, state = null) => {
    const userId = getCurrentUserId();
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    });
    if (state) {
      params.append('state', state);
    }
    const response = await request(
      `/admin/reported-comments?${params.toString()}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 删除被举报的评论（管理员操作）
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 操作结果
   */
  deleteReportedComment: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/comments/${commentId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 管理员强制删除任意评论（管理员操作）
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 操作结果
   */
  forceDeleteComment: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/comments/${commentId}/force`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },

  /**
   * 通过评论举报（管理员操作）
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 操作结果
   */
  approveCommentReport: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/comments/${commentId}/approve-report`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 忽略评论举报（管理员操作）
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 操作结果
   */
  ignoreCommentReports: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/comments/${commentId}/ignore-reports`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 恢复评论（管理员操作）
   * @param {number} commentId - 评论ID
   * @returns {Promise<Object>} 操作结果
   */
  restoreComment: async (commentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/comments/${commentId}/restore`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 改变帖子状态（管理员操作）
   * @param {number} postId - 帖子ID
   * @param {string} status - 目标状态（VALID/INVALID）
   * @returns {Promise<Object>} 操作结果
   */
  changePostStatus: async (postId, status) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/posts/${postId}/change-status`,
      {
        method: "PUT",
        body: JSON.stringify({ status }),
      },
      userId
    );
    return response;
  },

  /**
   * 获取所有聊天室列表
   * @returns {Promise<Array>} 聊天室列表
   */
  getAllChatRooms: async () => {
    const userId = getCurrentUserId();
    const response = await request("/admin/chat-rooms", {}, userId);
    return response;
  },

  //删除聊天室用户（管理员操作）
  deleteChatRoomUser: async (chatRoomUserId) => {
    const userId = getCurrentUserId();
    return await request(
      `/admin//delete/chatRoomUserId/${chatRoomUserId}`,
      {
        method: "DELETE",
      },
      userId
    );
  },

  /**
   * 删除聊天室（管理员操作）
   * @param {number} roomId - 聊天室ID
   * @returns {Promise<Object>} 操作结果
   */
  deleteChatRoom: async (roomId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/admin/chat-rooms/${roomId}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response;
  },
};

export const aiAPI = {
  getSessions: async () => {
    const userId = getCurrentUserId();
    const response = await request("/llm/chat/fetch", {}, userId);
    return response;
  },

  getMessages: async (sessionId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/llm/chat/messages/${sessionId}`,
      {},
      userId
    );
    return response;
  },

  sendMessage: async (sessionId, message) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/llm/chat/send`,
      {
        method: "POST",
        body: JSON.stringify({ sessionId, message }),
      },
      userId
    );
    return response;
  },

  clearSession: async (sessionId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/llm/chat/clear`,
      {
        method: "POST",
        body: JSON.stringify({ sessionId }),
      },
      userId
    );
    return response;
  },

  clearMemory: async (sessionId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/llm/chat/clear`,
      {
        method: "POST",
        body: JSON.stringify({ sessionId }),
      },
      userId
    );
    return response;
  },
};

// 通知相关API
export const notificationAPI = {
  /**
   * 获取当前用户的未读通知（循环拉取所有分页）
   * @param {number} size - 每页大小
   * @returns {Promise<Array>} 所有未读通知数组
   */
  getUnreadNotifications: async (size = 10) => {
    const userId = getCurrentUserId();
    let page = 0;
    let allNotifications = [];
    let hasMore = true;
    while (hasMore) {
      const response = await request(
        `/notifications/me/unread?page=${page}&size=${size}`,
        {},
        userId
      );
      // 兼容后端返回格式
      const content =
        response && response.content
          ? response.content
          : Array.isArray(response)
          ? response
          : [];
      allNotifications = allNotifications.concat(content);
      // 判断是否还有下一页
      if (response && typeof response.totalPages === "number") {
        hasMore = page + 1 < response.totalPages;
      } else if (content.length < size) {
        hasMore = false;
      } else {
        hasMore = true;
      }
      page++;
    }
    return allNotifications;
  },

  /**
   * 标记单条通知为已读
   * @param {number} notificationId - 通知ID
   * @returns {Promise<void>}
   */
  markAsRead: async (notificationId) => {
    const userId = getCurrentUserId();
    await request(
      `/notifications/${notificationId}/read`,
      { method: "PATCH" },
      userId
    );
  },

  /**
   * 一键全部标记为已读
   * @returns {Promise<void>}
   */
  markAllAsRead: async () => {
    const userId = getCurrentUserId();
    await request(`/notifications/me/readAll`, { method: "PATCH" }, userId);
  },

  /**
   * 删除单条通知
   * @param {number} notificationId - 通知ID
   * @returns {Promise<void>}
   */
  deleteNotification: async (notificationId) => {
    const userId = getCurrentUserId();
    await request(
      `/notifications/${notificationId}`,
      { method: "DELETE" },
      userId
    );
  },

  /**
   * 删除所有通知
   * @returns {Promise<void>}
   */
  deleteAllNotifications: async () => {
    const userId = getCurrentUserId();
    await request(`/notifications/me/all`, { method: "DELETE" }, userId);
  },
};

// 评论相关API
export const commentAPI = {
  /**
   * 举报评论
   * @param {number} commentId - 评论ID
   * @param {string} reason - 举报原因
   * @returns {Promise<void>}
   */
  reportComment: async (commentId, reason) => {
    const userId = getCurrentUserId();
    try {
      await request(
        `/comments/${commentId}/report`,
        {
          method: "POST",
          body: JSON.stringify({ commentId, reason }),
        },
        userId
      );
    } catch (error) {
      // 将后端错误消息传递给前端
      if (error.message) {
        throw new Error(error.message);
      }
      throw error;
    }
  },

  /**
   * 删除评论（用户删除自己的评论）
   * @param {number} commentId - 评论ID
   * @returns {Promise<void>}
   */
  deleteComment: async (commentId) => {
    const userId = getCurrentUserId();
    await request(`/comments/${commentId}`, { method: "DELETE" }, userId);
  },
};

// 心理专家相关API
export const expertAPI = {
  /**
   * 获取心理专家列表
   * @returns {Promise<Array>} 专家数组
   */
  getExperts: async () => {
    const userId = getCurrentUserId();
    return await request("/experts", {}, userId);
  },

  /**
   * 获取专家用户列表（推荐使用，包含用户信息和专家详细信息）
   * @returns {Promise<Array>} 专家用户数组
   */
  getExpertUsers: async () => {
    const userId = getCurrentUserId();
    return await request("/experts/users", {}, userId);
  },

  /**
   * 根据ID获取专家详情
   * @param {number} id - 专家ID
   * @returns {Promise<Object>} 专家信息
   */
  getExpertById: async (id) => {
    const userId = getCurrentUserId();
    return await request(`/experts/${id}`, {}, userId);
  },

  /**
   * 添加心理专家
   * @param {Object} expertData - 专家数据
   * @returns {Promise<Object>} 添加的专家信息
   */
  addExpert: async (expertData) => {
    const userId = getCurrentUserId();
    return await request(
      "/experts",
      {
        method: "POST",
        body: JSON.stringify(expertData),
      },
      userId
    );
  },

  /**
   * 更新心理专家信息
   * @param {number} id - 专家ID
   * @param {Object} expertData - 更新的专家数据
   * @returns {Promise<Object>} 更新后的专家信息
   */
  updateExpert: async (id, expertData) => {
    const userId = getCurrentUserId();
    return await request(
      `/experts/${id}`,
      {
        method: "PUT",
        body: JSON.stringify(expertData),
      },
      userId
    );
  },

  /**
   * 删除心理专家
   * @param {number} id - 专家ID
   * @returns {Promise<void>}
   */
  deleteExpert: async (id) => {
    const userId = getCurrentUserId();
    await request(
      `/experts/${id}`,
      {
        method: "DELETE",
      },
      userId
    );
  },

  /**
   * 获取当前专家用户的个人信息
   * @returns {Promise<Object>} 专家信息
   */
  getMyProfile: async () => {
    const userId = getCurrentUserId();
    return await request("/experts/profile", {}, userId);
  },

  /**
   * 更新当前专家用户的个人信息
   * @param {Object} expertData - 专家数据
   * @returns {Promise<Object>} 更新后的专家信息
   */
  updateMyProfile: async (expertData) => {
    const userId = getCurrentUserId();
    return await request(
      "/experts/profile",
      {
        method: "PUT",
        body: JSON.stringify(expertData),
      },
      userId
    );
  },
};

// 预约相关API
export const appointmentAPI = {
  /**
   * 创建预约
   * @param {Object} appointmentData - 预约数据
   * @returns {Promise<Object>} 预约信息
   */
  createAppointment: async (appointmentData) => {
    const userId = getCurrentUserId();
    const response = await request(
      "/appointments",
      {
        method: "POST",
        body: JSON.stringify(appointmentData),
      },
      userId
    );
    return response;
  },

  /**
   * 获取当前用户的预约列表
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 预约列表
   */
  getMyAppointments: async (page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/my?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 获取预约详情
   * @param {number} appointmentId - 预约ID
   * @returns {Promise<Object>} 预约详情
   */
  getAppointmentById: async (appointmentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 取消预约
   * @param {number} appointmentId - 预约ID
   * @returns {Promise<Object>} 取消结果
   */
  cancelAppointment: async (appointmentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}/cancel`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 评价预约
   * @param {number} appointmentId - 预约ID
   * @param {string} userRating - 用户评价
   * @param {number} rating - 评分（1-5）
   * @returns {Promise<Object>} 评价结果
   */
  rateAppointment: async (appointmentId, userRating, rating) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}/rate`,
      {
        method: "PUT",
        body: JSON.stringify({ userRating, rating }),
      },
      userId
    );
    return response;
  },

  /**
   * 获取专家的预约列表
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<Object>} 预约列表
   */
  getExpertAppointments: async (page = 0, size = 10) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/expert?page=${page}&size=${size}`,
      {},
      userId
    );
    return response;
  },

  /**
   * 专家确认预约
   * @param {number} appointmentId - 预约ID
   * @param {string} reply - 专家回复
   * @returns {Promise<Object>} 确认结果
   */
  confirmAppointment: async (appointmentId, reply = "") => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}/confirm`,
      {
        method: "PUT",
        body: JSON.stringify({ reply }),
      },
      userId
    );
    return response;
  },

  /**
   * 专家拒绝预约
   * @param {number} appointmentId - 预约ID
   * @param {string} reply - 专家回复
   * @returns {Promise<Object>} 拒绝结果
   */
  rejectAppointment: async (appointmentId, reply = "") => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}/reject`,
      {
        method: "PUT",
        body: JSON.stringify({ reply }),
      },
      userId
    );
    return response;
  },

  /**
   * 专家完成预约
   * @param {number} appointmentId - 预约ID
   * @returns {Promise<Object>} 完成结果
   */
  completeAppointment: async (appointmentId) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/${appointmentId}/complete`,
      {
        method: "PUT",
      },
      userId
    );
    return response;
  },

  /**
   * 获取专家待处理预约数量
   * @returns {Promise<Object>} 待处理预约数量
   */
  getExpertPendingCount: async () => {
    const userId = getCurrentUserId();
    const response = await request(
      `/appointments/expert/pending-count`,
      {},
      userId
    );
    return response;
  },
};

// 专家排班相关API
export const expertScheduleAPI = {
  /**
   * 获取专家未来14天的可预约时间段
   * @param {number} expertId - 专家ID
   * @returns {Promise<boolean[][]>} 可预约时段二维数组
   */
  getAvailableSlots: async (expertId) => {
    const userId = getCurrentUserId();
    return await request(`/expert-schedule/${expertId}/slots`, {}, userId);
  },

  /**
   * 通过用户ID获取专家未来14天的可预约时间段
   * @param {number} userId - 专家用户ID
   * @returns {Promise<boolean[][]>} 可预约时段二维数组
   */
  getAvailableSlotsByUserId: async (userId) => {
    const currentUserId = getCurrentUserId();
    return await request(
      `/expert-schedule/user/${userId}/slots`,
      {},
      currentUserId
    );
  },

  /**
   * 获取专家的详细时间表状态（包含预约占用信息）
   * @param {number} expertId - 专家ID
   * @returns {Promise<number[][]>} 详细状态二维数组 (0=专家不可用, 1=可预约, 2=已被预约)
   */
  getDetailedSlots: async (expertId) => {
    const userId = getCurrentUserId();
    return await request(`/expert-schedule/${expertId}/detailed-slots`, {}, userId);
  },

  /**
   * 通过用户ID获取专家的详细时间表状态（包含预约占用信息）
   * @param {number} userId - 专家用户ID
   * @returns {Promise<number[][]>} 详细状态二维数组 (0=专家不可用, 1=可预约, 2=已被预约)
   */
  getDetailedSlotsByUserId: async (userId) => {
    const currentUserId = getCurrentUserId();
    return await request(`/expert-schedule/user/${userId}/detailed-slots`, {}, currentUserId);
  },

  /**
   * 预约指定专家的某一天某个时间段（仅用于测试/演示，正式预约请用appointmentAPI）
   * @param {number} expertId - 专家ID
   * @param {number} dayOffset - 距今天的天数（0-13）
   * @param {number} periodIndex - 时段索引（0-7）
   * @returns {Promise<boolean>} 是否预约成功
   */
  bookSlot: async (expertId, dayOffset, periodIndex) => {
    const userId = getCurrentUserId();
    return await request(
      `/expert-schedule/${expertId}/book?dayOffset=${dayOffset}&periodIndex=${periodIndex}`,
      { method: "POST" },
      userId
    );
  },

  /**
   * 获取专家的排班信息
   * @param {number} expertId - 专家ID
   * @returns {Promise<Object>} 排班信息
   */
  getExpertSchedule: async (expertId) => {
    const userId = getCurrentUserId();
    return await request(`/expert-schedule/${expertId}/schedule`, {}, userId);
  },

  /**
   * 获取当前专家用户的排班信息
   * @returns {Promise<Object>} 排班信息
   */
  getMyExpertSchedule: async () => {
    const userId = getCurrentUserId();
    return await request(`/expert-schedule/schedule`, {}, userId);
  },

  /**
   * 更新专家的排班信息
   * @param {number} expertId - 专家ID
   * @param {number} dayOffset - 距今天的天数（0-13）
   * @param {number} periodIndex - 时段索引（0-7）
   * @param {boolean} available - 是否可预约
   * @returns {Promise<Object>} 更新结果
   */
  updateSchedule: async (expertId, dayOffset, periodIndex, available) => {
    const userId = getCurrentUserId();
    return await request(
      `/expert-schedule/${expertId}/update?dayOffset=${dayOffset}&periodIndex=${periodIndex}&available=${available}`,
      { method: "PUT" },
      userId
    );
  },

  /**
   * 更新当前专家用户的排班信息
   * @param {number} dayOffset - 距今天的天数（0-13）
   * @param {number} periodIndex - 时段索引（0-7）
   * @param {boolean} available - 是否可预约
   * @returns {Promise<Object>} 更新结果
   */
  updateMySchedule: async (dayOffset, periodIndex, available) => {
    const userId = getCurrentUserId();
    return await request(
      `/expert-schedule/update?dayOffset=${dayOffset}&periodIndex=${periodIndex}&available=${available}`,
      { method: "PUT" },
      userId
    );
  },

  /**
   * 批量更新专家的排班信息
   * @param {Array} updates - 更新请求数组，每个元素包含 {dayOffset, periodIndex, available}
   * @returns {Promise<Object>} 批量更新结果
   */
  batchUpdateMySchedule: async (updates) => {
    const userId = getCurrentUserId();
    return await request(
      "/expert-schedule/batch-update",
      {
        method: "PUT",
        body: JSON.stringify(updates),
      },
      userId
    );
  },
};

// 头像上传相关API
export const uploadAvatar = async (formData) => {
  const userId = getCurrentUserId();
  return await request(
    "/avatar/upload",
    {
      method: "POST",
      body: formData,
    },
    userId
  );
};

export const deleteAvatar = async () => {
  const userId = getCurrentUserId();
  const response = await request(
    "/avatar/delete",
    { method: "DELETE" },
    userId
  );
  // 由于DELETE请求可能返回null，我们需要构造一个标准响应
  return response || { success: true, message: "头像删除成功" };
};

// 帖子图片上传相关API
export const postImageAPI = {
  /**
   * 上传帖子图片
   * @param {FormData} formData - 包含图片文件的FormData
   * @returns {Promise<Object>} 上传响应，包含imageUrl
   */
  uploadPostImage: async (formData) => {
    const userId = getCurrentUserId();
    return await request(
      "/post-image/upload",
      {
        method: "POST",
        body: formData,
      },
      userId
    );
  },

  /**
   * 删除帖子图片
   * @param {string} imageUrl - 要删除的图片URL
   * @returns {Promise<Object>} 删除响应
   */
  deletePostImage: async (imageUrl) => {
    const userId = getCurrentUserId();
    const response = await request(
      `/post-image/delete?imageUrl=${encodeURIComponent(imageUrl)}`,
      {
        method: "DELETE",
      },
      userId
    );
    return response || { success: true, message: "图片删除成功" };
  },
};

// 聊天室相关API
export const chatAPI = {
  // 获取聊天室列表
  getRooms: async () => {
    const userId = getCurrentUserId();
    return await request("/chat-room/list", { method: "GET" }, userId);
  },
  // 获取聊天室消息
  getMessages: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-message/room/${chatRoomId}`,
      { method: "GET" },
      userId
    );
  },
  // 获取某时间以后的新消息
  getMessagesAfter: async (chatRoomId, timeStamp) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-message/room/${chatRoomId}/after?timestamp=${timeStamp}`,
      {
        method: "GET",
      },
      userId
    );
  },
  // 发送消息
  sendMessage: async (chatRoomId, content) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-message/send`,
      {
        method: "POST",
        body: JSON.stringify({ chatRoomId, content }),
      },
      userId
    );
  },
  isInRoom: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/room/${chatRoomId}/isInRoom`,
      { method: "GET" },
      userId
    );
  },
  // 获取自己在聊天室的用户
  getCurrentUserByRoomId: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/room/${chatRoomId}/me`,
      { method: "GET" },
      userId
    );
  },
  //退出房间
  quitRoom: async (chatRoomUserId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/quitRoom/chatRoomUserId/${chatRoomUserId}`,
      { method: "POST" },
      userId
    );
  },
  // 上传头像和用户名
  // 上传头像
  uploadAvatar: async (chatRoomId, formData) => {
    const userId = getCurrentUserId();

    const response = await request(
      `/chat-room-user/room/${chatRoomId}/me/uploadAvatar`,
      {
        method: "POST",
        body: formData,
      },
      userId
    );

    return response;
  },

  // 设置昵称
  setNickname: async (chatRoomId, displayNickname) => {
    const userId = getCurrentUserId();

    // 创建 FormData 来匹配后端的 @RequestParam
    const formData = new FormData();
    formData.append("displayNickname", displayNickname);

    const response = await request(
      `/chat-room-user/room/${chatRoomId}/me/setNickname`,
      {
        method: "PUT",
        body: formData,
      },
      userId
    );

    return response;
  },
  // 按聊天室用户ID获取单个聊天室用户
  getRoomUserByRoomUserId: async (chatRoomId, chatRoomUserId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/room/${chatRoomId}/RoomUser/${chatRoomUserId}`,
      { method: "GET" },
      userId
    );
  },
  // 获取聊天室成员
  getRoomUsers: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/room/${chatRoomId}`,
      { method: "GET" },
      userId
    );
  },
  // 创建聊天室
  createRoom: async ({ name, description, type }) => {
    const userId = getCurrentUserId();
    return await request(
      "/chat-room/create",
      {
        method: "POST",
        body: JSON.stringify({ name, description, type }),
      },
      userId
    );
  },
  // 加入聊天室
  joinRoom: async (chatRoomId) => {
    const userId = getCurrentUserId();
    return await request(
      `/chat-room-user/join/${chatRoomId}`,
      {
        method: "POST",
      },
      userId
    );
  },
  getMessagesPaged: async (chatRoomId, { before, limit }) => {
    const userId = getCurrentUserId();
    let url = `/chat-message/room/${chatRoomId}/paged?limit=${limit}`;
    if (before) url += `&before=${encodeURIComponent(before)}`;
    return await request(url, { method: "GET" }, userId);
  },
};

// 拉黑相关API
export const blockAPI = {
  /**
   * 拉黑用户
   * @param {number} blockedUserId - 被拉黑用户ID
   * @param {string} reason - 拉黑原因
   * @returns {Promise<Object>} 拉黑结果
   */
  blockUser: async (blockedUserId, reason = "") => {
    const userId = getCurrentUserId();
    return await request(
      "/user-blocks/block",
      {
        method: "POST",
        body: JSON.stringify({ blockedUserId, reason }),
      },
      userId
    );
  },

  /**
   * 取消拉黑用户
   * @param {number} blockedUserId - 被拉黑用户ID
   * @returns {Promise<Object>} 取消拉黑结果
   */
  unblockUser: async (blockedUserId) => {
    const userId = getCurrentUserId();
    return await request(
      `/user-blocks/unblock/${blockedUserId}`,
      {
        method: "DELETE",
      },
      userId
    );
  },

  /**
   * 获取已拉黑用户列表
   * @returns {Promise<Object>} 拉黑用户列表
   */
  getBlockedUsers: async () => {
    const userId = getCurrentUserId();
    return await request("/user-blocks/blocked-users", {}, userId);
  },

  /**
   * 获取拉黑统计信息
   * @returns {Promise<Object>} 拉黑统计
   */
  getBlockStats: async () => {
    const userId = getCurrentUserId();
    return await request("/user-blocks/stats", {}, userId);
  },

  /**
   * 检查是否已拉黑用户
   * @param {number} blockedUserId - 被拉黑用户ID
   * @returns {Promise<Object>} 拉黑状态
   */
  checkBlockStatus: async (blockedUserId) => {
    const userId = getCurrentUserId();
    return await request(`/user-blocks/check/${blockedUserId}`, {}, userId);
  },
};

// 导出默认API对象
const api = {
  auth: authAPI,
  post: postAPI,
  mood: moodAPI,
  user: userAPI,
  admin: adminAPI,
  ai: aiAPI,
  notification: notificationAPI,
  expert: expertAPI,
  appointment: appointmentAPI,
  expertSchedule: expertScheduleAPI,
  chat: chatAPI,
  block: blockAPI,
};

export default api;

export { request };
