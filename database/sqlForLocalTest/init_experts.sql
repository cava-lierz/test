-- 初始化专家数据
USE mentara;

-- 清空现有专家数据（如果存在）
DELETE FROM experts;

-- 插入测试专家数据
INSERT INTO experts (name, specialty, contact, status) VALUES
('张心理', '焦虑症、抑郁症治疗', 'zhangxinli@example.com', 'online'),
('李咨询', '人际关系、情感咨询', 'lizixun@example.com', 'online'),
('王治疗', '学习压力、职业规划', 'wangzhiliao@example.com', 'offline'),
('陈专家', '家庭关系、亲子教育', 'chenzhuanjia@example.com', 'online'),
('刘医生', '创伤后应激障碍', 'liuyisheng@example.com', 'online');

-- 验证插入结果
SELECT * FROM experts; 