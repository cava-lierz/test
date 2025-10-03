-- 安全的级联删除修复脚本
-- 只修改外键约束，不删除任何数据
-- 执行前请备份数据库！

-- 1. 先检查现有的外键约束
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = 'mentara' 
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- 2. 删除现有的外键约束（不删除数据）
-- comments表
SET @sql = (SELECT CONCAT('ALTER TABLE comments DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'comments' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CONCAT('ALTER TABLE comments DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'comments' 
            AND REFERENCED_TABLE_NAME = 'posts' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- comment_likes表
SET @sql = (SELECT CONCAT('ALTER TABLE comment_likes DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'comment_likes' 
            AND REFERENCED_TABLE_NAME = 'comments' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CONCAT('ALTER TABLE comment_likes DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'comment_likes' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- post_likes表
SET @sql = (SELECT CONCAT('ALTER TABLE post_likes DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'post_likes' 
            AND REFERENCED_TABLE_NAME = 'posts' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CONCAT('ALTER TABLE post_likes DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'post_likes' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- reports表
SET @sql = (SELECT CONCAT('ALTER TABLE reports DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'reports' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CONCAT('ALTER TABLE reports DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'reports' 
            AND REFERENCED_TABLE_NAME = 'posts' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- checkins表
SET @sql = (SELECT CONCAT('ALTER TABLE checkins DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'checkins' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- posts表
SET @sql = (SELECT CONCAT('ALTER TABLE posts DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'posts' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- notifications表
SET @sql = (SELECT CONCAT('ALTER TABLE notifications DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'notifications' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- appointments表
SET @sql = (SELECT CONCAT('ALTER TABLE appointments DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'appointments' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CONCAT('ALTER TABLE appointments DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'appointments' 
            AND REFERENCED_TABLE_NAME = 'experts' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- user_auths表
SET @sql = (SELECT CONCAT('ALTER TABLE user_auths DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'user_auths' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 重新创建带级联删除的外键约束

-- comments表
ALTER TABLE comments ADD CONSTRAINT fk_comment_author 
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE comments ADD CONSTRAINT fk_comment_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;

-- comment_likes表
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_comment 
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- post_likes表
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- reports表
ALTER TABLE reports ADD CONSTRAINT fk_report_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE reports ADD CONSTRAINT fk_report_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;

-- checkins表
ALTER TABLE checkins ADD CONSTRAINT fk_checkin_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- posts表
ALTER TABLE posts ADD CONSTRAINT fk_post_author 
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;

-- notifications表
ALTER TABLE notifications ADD CONSTRAINT fk_notification_receiver 
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE;

-- appointments表
ALTER TABLE appointments ADD CONSTRAINT fk_appointment_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE appointments ADD CONSTRAINT fk_appointment_expert 
    FOREIGN KEY (expert_id) REFERENCES experts(id) ON DELETE CASCADE;

-- user_auths表
ALTER TABLE user_auths ADD CONSTRAINT fk_user_auth_user 
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE;

-- 4. 验证约束是否创建成功
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = 'mentara' 
ORDER BY TABLE_NAME, CONSTRAINT_NAME; 