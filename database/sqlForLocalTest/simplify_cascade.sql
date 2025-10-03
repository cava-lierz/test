-- 简化级联关系脚本
-- 移除不必要的级联删除，因为现在使用软删除

-- 1. 先检查现有的外键约束
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = 'mentara' 
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

-- 2. 删除现有的级联删除约束（不删除数据）

-- comments表 - 移除级联删除
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

-- posts表 - 移除级联删除
SET @sql = (SELECT CONCAT('ALTER TABLE posts DROP FOREIGN KEY ', CONSTRAINT_NAME) 
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'mentara' AND TABLE_NAME = 'posts' 
            AND REFERENCED_TABLE_NAME = 'users' AND CONSTRAINT_NAME IS NOT NULL);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 重新创建不带级联删除的外键约束

-- comments表 - 使用RESTRICT（默认）
ALTER TABLE comments ADD CONSTRAINT fk_comment_author 
    FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT fk_comment_post 
    FOREIGN KEY (post_id) REFERENCES posts(id);

-- posts表 - 使用RESTRICT（默认）
ALTER TABLE posts ADD CONSTRAINT fk_post_author 
    FOREIGN KEY (author_id) REFERENCES users(id);

-- 4. 保留必要的级联删除（点赞、举报等关联数据）
-- 这些数据在软删除时仍然需要级联删除，因为它们没有业务价值

-- comment_likes表 - 保留级联删除
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_comment 
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;
ALTER TABLE comment_likes ADD CONSTRAINT fk_comment_like_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- post_likes表 - 保留级联删除
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;
ALTER TABLE post_likes ADD CONSTRAINT fk_post_like_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- reports表 - 保留级联删除
ALTER TABLE reports ADD CONSTRAINT fk_report_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE reports ADD CONSTRAINT fk_report_post 
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;

-- 5. 验证约束是否创建成功
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    DELETE_RULE
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = 'mentara' 
ORDER BY TABLE_NAME, CONSTRAINT_NAME; 