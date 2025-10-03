-- 删除旧的专家排班表字段，只保留新的schedule_json字段
-- 警告：此操作会删除旧字段中的数据，请确保已经备份或迁移完成

USE mentara;

-- 1. 首先检查当前表结构
SELECT 'Current table structure:' as info;
DESCRIBE expert_schedules;

-- 2. 检查是否存在schedule_json字段
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'mentara' 
  AND TABLE_NAME = 'expert_schedules'
  AND COLUMN_NAME = 'schedule_json';

-- 3. 添加schedule_json字段（LONGTEXT不能有默认值，所以分两步）
-- 步骤3a: 先添加可空的字段
ALTER TABLE expert_schedules 
ADD COLUMN schedule_json LONGTEXT;

-- 步骤3b: 为所有现有记录设置默认值
UPDATE expert_schedules 
SET schedule_json = '{}' 
WHERE schedule_json IS NULL;

-- 步骤3c: 将字段改为非空
ALTER TABLE expert_schedules 
MODIFY COLUMN schedule_json LONGTEXT NOT NULL;

-- 4. 删除旧字段
-- 注意：这会永久删除这些字段中的数据
-- 如果字段不存在，MySQL会提示错误，可以忽略
ALTER TABLE expert_schedules DROP COLUMN base_date;
ALTER TABLE expert_schedules DROP COLUMN base_index;
ALTER TABLE expert_schedules DROP COLUMN slots_json;

-- 5. 验证最终表结构
SELECT 'Final table structure after cleanup:' as info;
DESCRIBE expert_schedules;

-- 6. 检查表中的记录数
SELECT COUNT(*) as total_records FROM expert_schedules;

-- 7. 显示一些示例数据
SELECT 
    id,
    expert_id,
    schedule_json,
    LENGTH(schedule_json) as json_length
FROM expert_schedules 
LIMIT 5;

-- 提示信息
SELECT 'Old fields removed successfully!' as status;
SELECT 'Only schedule_json field remains for schedule data.' as note;
SELECT 'Applications will now use the new field structure.' as next_step; 