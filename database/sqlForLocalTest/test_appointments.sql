-- 测试预约功能
USE mentara;

-- 1. 检查表是否存在
SHOW TABLES LIKE 'experts';
SHOW TABLES LIKE 'appointments';

-- 2. 检查专家数据
SELECT * FROM experts;

-- 3. 检查预约表结构
DESCRIBE appointments;

-- 4. 检查预约数据
SELECT * FROM appointments;

-- 5. 检查用户数据（用于测试）
SELECT id, username, nickname FROM users LIMIT 5;

-- 6. 测试插入预约数据（如果有专家和用户数据）
-- INSERT INTO appointments (user_id, expert_id, appointment_time, status, description, contact_info, duration) 
-- VALUES (1, 1, '2024-01-20 14:00:00', 'pending', '测试预约', 'test@example.com', 60); 