-- 为专家120（expert_id=8）初始化排班数据
-- 未来14天，每天8个时段，全部设置为状态0（空闲）

USE mentara;

-- 删除可能存在的旧数据
DELETE FROM expert_schedules WHERE expert_id = 8;

-- 插入新的初始化数据
INSERT INTO expert_schedules (expert_id, schedule_json) VALUES (
    8, 
    '{"2025-07-16":"0,0,0,0,0,0,0,0","2025-07-17":"0,0,0,0,0,0,0,0","2025-07-18":"0,0,0,0,0,0,0,0","2025-07-19":"0,0,0,0,0,0,0,0","2025-07-20":"0,0,0,0,0,0,0,0","2025-07-21":"0,0,0,0,0,0,0,0","2025-07-22":"0,0,0,0,0,0,0,0","2025-07-23":"0,0,0,0,0,0,0,0","2025-07-24":"0,0,0,0,0,0,0,0","2025-07-25":"0,0,0,0,0,0,0,0","2025-07-26":"0,0,0,0,0,0,0,0","2025-07-27":"0,0,0,0,0,0,0,0","2025-07-28":"0,0,0,0,0,0,0,0","2025-07-29":"0,0,0,0,0,0,0,0"}'
);

-- 验证插入的数据
SELECT 
    id,
    expert_id,
    LEFT(schedule_json, 100) as schedule_preview,
    LENGTH(schedule_json) as json_length
FROM expert_schedules 
WHERE expert_id = 8;

-- 显示成功信息
SELECT 'Expert 120 schedule initialized successfully!' as status;
SELECT 'All time slots for next 14 days set to status 0 (available)' as note; 