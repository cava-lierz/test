-- 完整的数据库初始化脚本
USE mentara;

-- 1. 确保专家表存在并插入数据
INSERT INTO experts (name, specialty, contact, status) VALUES
('张心理', '焦虑症、抑郁症治疗', 'zhangxinli@example.com', 'online'),
('李咨询', '人际关系、情感咨询', 'lizixun@example.com', 'online'),
('王治疗', '学习压力、职业规划', 'wangzhiliao@example.com', 'offline'),
('陈专家', '家庭关系、亲子教育', 'chenzhuanjia@example.com', 'online'),
('刘医生', '创伤后应激障碍', 'liuyisheng@example.com', 'online')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    specialty = VALUES(specialty),
    contact = VALUES(contact),
    status = VALUES(status);

-- 2. 检查表结构
SELECT 'Experts table structure:' as info;
DESCRIBE experts;

SELECT 'Appointments table structure:' as info;
DESCRIBE appointments;

-- 3. 显示当前数据
SELECT 'Current experts:' as info;
SELECT * FROM experts;

SELECT 'Current appointments:' as info;
SELECT * FROM appointments;

-- 4. 检查用户数据
SELECT 'Sample users:' as info;
SELECT id, username, nickname, role FROM users LIMIT 5; 