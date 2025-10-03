/**
 * 表单验证工具函数
 */

/**
 * 验证学号是否有效
 * @param {string} studentId - 学号
 * @returns {Object} 验证结果
 */
export const validateStudentId = (studentId) => {
  if (!studentId || !studentId.trim()) {
    return { isValid: false, message: '请输入学号' };
  }
  
  const trimmedId = studentId.trim();
  
  // 学号长度验证（一般为6-12位）
  if (trimmedId.length < 6 || trimmedId.length > 12) {
    return { isValid: false, message: '学号长度应为6-12位' };
  }
  
  // 学号只能包含数字和字母
  if (!/^[a-zA-Z0-9]+$/.test(trimmedId)) {
    return { isValid: false, message: '学号只能包含数字和字母' };
  }
  
  return { isValid: true, message: '' };
};

/**
 * 验证密码是否有效
 * @param {string} password - 密码
 * @returns {Object} 验证结果
 */
export const validatePassword = (password) => {
  if (!password || !password.trim()) {
    return { isValid: false, message: '请输入密码' };
  }
  
  if (password.length < 6) {
    return { isValid: false, message: '密码长度至少6位' };
  }
  
  if (password.length > 50) {
    return { isValid: false, message: '密码长度不能超过50位' };
  }
  
  return { isValid: true, message: '' };
};

/**
 * 验证邮箱是否有效
 * @param {string} email - 邮箱
 * @returns {Object} 验证结果
 */
export const validateEmail = (email) => {
  if (!email || !email.trim()) {
    return { isValid: false, message: '请输入邮箱' };
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  
  if (!emailRegex.test(email)) {
    return { isValid: false, message: '请输入有效的邮箱地址' };
  }
  
  return { isValid: true, message: '' };
};

/**
 * 验证密码确认是否匹配
 * @param {string} password - 原密码
 * @param {string} confirmPassword - 确认密码
 * @returns {Object} 验证结果
 */
export const validatePasswordConfirm = (password, confirmPassword) => {
  if (!confirmPassword || !confirmPassword.trim()) {
    return { isValid: false, message: '请确认密码' };
  }
  
  if (password !== confirmPassword) {
    return { isValid: false, message: '两次输入的密码不一致' };
  }
  
  return { isValid: true, message: '' };
};

/**
 * 综合表单验证
 * @param {Object} formData - 表单数据
 * @param {Array} fields - 需要验证的字段
 * @returns {Object} 验证结果
 */
export const validateForm = (formData, fields) => {
  const validators = {
    studentId: validateStudentId,
    password: validatePassword,
    email: validateEmail,
    confirmPassword: (value) => validatePasswordConfirm(formData.password, value),
  };
  
  for (const field of fields) {
    const validator = validators[field];
    if (validator) {
      const result = validator(formData[field]);
      if (!result.isValid) {
        return result;
      }
    }
  }
  
  return { isValid: true, message: '' };
};

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