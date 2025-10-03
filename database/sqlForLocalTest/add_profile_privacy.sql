-- 添加用户个人信息公开设置字段
ALTER TABLE users ADD COLUMN is_profile_public BOOLEAN NOT NULL DEFAULT TRUE;

-- 更新现有用户为公开状态（可选）
UPDATE users SET is_profile_public = TRUE WHERE is_profile_public IS NULL; 