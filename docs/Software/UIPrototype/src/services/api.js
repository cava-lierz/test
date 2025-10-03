/**
 * API服务层 - 处理所有后端接口调用
 * 
 * @author Mentara Team
 */

// API基础配置
const API_BASE_URL = 'http://localhost:8080/api';

// 通用请求函数
const request = async (url, options = {}) => {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  // 如果有token，添加到请求头
  const token = localStorage.getItem('mentara_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, config);
    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || '请求失败');
    }

    return data;
  } catch (error) {
    console.error('API请求错误:', error);
    throw error;
  }
};

// 认证相关API
export const authAPI = {
  // 用户登录
  login: async (loginData) => {
    const response = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(loginData),
    });
    
    // 保存token到localStorage
    if (response.data.token) {
      localStorage.setItem('mentara_token', response.data.token);
      localStorage.setItem('mentara_refresh_token', response.data.refreshToken);
    }
    
    return response.data;
  },

  // 用户注册
  register: async (registerData) => {
    const response = await request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(registerData),
    });
    
    // 保存token到localStorage
    if (response.data.token) {
      localStorage.setItem('mentara_token', response.data.token);
      localStorage.setItem('mentara_refresh_token', response.data.refreshToken);
    }
    
    return response.data;
  },

  // 刷新token
  refreshToken: async () => {
    const refreshToken = localStorage.getItem('mentara_refresh_token');
    if (!refreshToken) {
      throw new Error('没有刷新令牌');
    }

    const response = await request(`/auth/refresh?refreshToken=${refreshToken}`, {
      method: 'POST',
    });
    
    // 更新token
    if (response.data.token) {
      localStorage.setItem('mentara_token', response.data.token);
    }
    
    return response.data;
  },

  // 退出登录
  logout: () => {
    localStorage.removeItem('mentara_token');
    localStorage.removeItem('mentara_refresh_token');
    localStorage.removeItem('mentara_login_state');
    localStorage.removeItem('mentara_user_info');
  },
};

// 帖子相关API
export const postAPI = {
  // 获取社区帖子
  getCommunityPosts: async (page = 0, size = 10) => {
    const response = await request(`/posts?page=${page}&size=${size}`);
    return response.data;
  },

  // 获取帖子详情
  getPostById: async (postId) => {
    const response = await request(`/posts/${postId}`);
    return response.data;
  },

  // 创建帖子
  createPost: async (postData) => {
    const response = await request('/posts', {
      method: 'POST',
      body: JSON.stringify(postData),
    });
    return response.data;
  },

  // 点赞/取消点赞帖子
  toggleLike: async (postId) => {
    const response = await request(`/posts/${postId}/like`, {
      method: 'POST',
    });
    return response.data;
  },

  // 删除帖子
  deletePost: async (postId) => {
    const response = await request(`/posts/${postId}`, {
      method: 'DELETE',
    });
    return response.data;
  },

  // 搜索帖子
  searchPosts: async (keyword, page = 0, size = 10) => {
    const response = await request(`/posts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
    return response.data;
  },
};

// 心情记录相关API
export const moodAPI = {
  // 心情打卡
  checkin: async (moodData) => {
    const response = await request('/mood/checkin', {
      method: 'POST',
      body: JSON.stringify(moodData),
    });
    return response.data;
  },

  // 获取今天的心情记录
  getTodayMood: async () => {
    const response = await request('/mood/today');
    return response.data;
  },

  // 获取最近心情记录
  getRecentMoods: async (days = 7) => {
    const response = await request(`/mood/recent?days=${days}`);
    return response.data;
  },

  // 获取指定日期范围的心情记录
  getMoodRange: async (startDate, endDate) => {
    const response = await request(`/mood/range?startDate=${startDate}&endDate=${endDate}`);
    return response.data;
  },

  // 获取心情平均值
  getAverageRating: async () => {
    const response = await request('/mood/average');
    return response.data;
  },

  // 检查今天是否已打卡
  hasCheckedInToday: async () => {
    const response = await request('/mood/check-today');
    return response.data;
  },
};

// 用户相关API（后续扩展）
export const userAPI = {
  // 获取用户信息
  getUserProfile: async (userId) => {
    const response = await request(`/users/${userId}`);
    return response.data;
  },

  // 更新用户信息
  updateProfile: async (userData) => {
    const response = await request('/users/profile', {
      method: 'PUT',
      body: JSON.stringify(userData),
    });
    return response.data;
  },
};

// 导出默认API对象
const api = {
  auth: authAPI,
  post: postAPI,
  mood: moodAPI,
  user: userAPI,
};

export default api; 