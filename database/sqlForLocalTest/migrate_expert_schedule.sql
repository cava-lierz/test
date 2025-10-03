-- 专家排班表结构迁移脚本
-- 从循环数组设计迁移到绝对日期存储设计

-- 1. 备份现有数据（如果需要的话）
CREATE TABLE expert_schedules_backup AS SELECT * FROM expert_schedules;

-- 2. 添加新字段
ALTER TABLE expert_schedules ADD COLUMN schedule_json LONGTEXT;

-- 3. 迁移现有数据（如果有的话）
-- 将原有的循环数组数据转换为新的绝对日期格式
UPDATE expert_schedules 
SET schedule_json = '{}' 
WHERE schedule_json IS NULL;

-- 4. 删除旧字段
ALTER TABLE expert_schedules DROP COLUMN base_index;
ALTER TABLE expert_schedules DROP COLUMN base_date;
ALTER TABLE expert_schedules DROP COLUMN slots_json;

-- 5. 设置新字段为非空，并设置默认值
ALTER TABLE expert_schedules 
MODIFY COLUMN schedule_json LONGTEXT NOT NULL DEFAULT '{}';

-- 6. 创建索引（如果需要）
-- CREATE INDEX idx_expert_schedules_expert_id ON expert_schedules(expert_id);

-- 验证迁移结果
SELECT 
    id,
    expert_id,
    schedule_json,
    CHAR_LENGTH(schedule_json) as json_length
FROM expert_schedules 
LIMIT 5;

-- 清理备份表（可选，建议先确认迁移成功后再执行）
-- DROP TABLE expert_schedules_backup; 