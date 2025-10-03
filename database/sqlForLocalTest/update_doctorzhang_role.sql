-- 更新doctorzhang用户的角色为专家
USE mentara;

-- 查找doctorzhang用户
SELECT id, username, nickname, role FROM users WHERE username LIKE '%doctorzhang%' OR nickname LIKE '%doctorzhang%';

-- 更新用户角色为专家（假设用户ID为找到的ID）
-- 请根据上面的查询结果替换实际的用户ID
UPDATE users SET role = 'EXPERT' WHERE username LIKE '%doctorzhang%' OR nickname LIKE '%doctorzhang%';

-- 验证更新结果
SELECT id, username, nickname, role FROM users WHERE role = 'EXPERT'; 