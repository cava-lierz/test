// 表单验证工具函数

export const validateUsername = (username) => {
  if (!username.trim()) {
    return '用户名不能为空';
  }
  if (username.length < 3) {
    return '用户名至少3个字符';
  }
  if (username.length > 20) {
    return '用户名不能超过20个字符';
  }
  return null;
};

export const validateEmail = (email) => {
  if (!email.trim()) {
    return '邮箱不能为空';
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return '请输入有效的邮箱地址';
  }
  return null;
};

export const validatePassword = (password) => {
  if (!password) {
    return '密码不能为空';
  }
  if (password.length < 6) {
    return '密码至少6个字符';
  }
  if (password.length > 50) {
    return '密码不能超过50个字符';
  }
  return null;
};

export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) {
    return '请确认密码';
  }
  if (password !== confirmPassword) {
    return '两次输入的密码不一致';
  }
  return null;
};

export const validatePostTitle = (title) => {
  if (!title.trim()) {
    return '标题不能为空';
  }
  if (title.length > 100) {
    return '标题不能超过100个字符';
  }
  return null;
};

export const validatePostContent = (content) => {
  if (!content.trim()) {
    return '内容不能为空';
  }
  if (content.length > 2000) {
    return '内容不能超过2000个字符';
  }
  return null;
}; 