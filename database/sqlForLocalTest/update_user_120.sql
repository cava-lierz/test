-- 更新用户ID为120的信息（不删除，只更新）
USE mentara;

-- 查看用户ID=120的当前信息
SELECT '用户ID=120的当前信息:' as info;
SELECT id, username, nickname, role, is_disabled, is_deleted, created_at, updated_at 
FROM users WHERE id = 120;

-- 更新用户ID=120的nickname和role
UPDATE users 
SET nickname = '张医生', 
    role = 'EXPERT',
    updated_at = NOW()
WHERE id = 120;

-- 验证更新结果
SELECT '更新后的用户ID=120信息:' as info;
SELECT id, username, nickname, role, is_disabled, is_deleted, created_at, updated_at 
FROM users WHERE id = 120;

-- 查看所有专家用户
SELECT '所有专家用户:' as info;
SELECT id, username, nickname, role FROM users WHERE role = 'EXPERT'; 