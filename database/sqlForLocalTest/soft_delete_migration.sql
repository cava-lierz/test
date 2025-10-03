-- 软删除功能迁移脚本
-- 为posts和comments表添加软删除相关字段

-- 1. 为posts表添加软删除字段
ALTER TABLE posts 
ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间',
ADD COLUMN deleted_by BIGINT NULL COMMENT '删除操作的用户ID',
ADD COLUMN delete_reason VARCHAR(255) NULL COMMENT '删除原因';

-- 2. 为comments表添加软删除字段
ALTER TABLE comments 
ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间',
ADD COLUMN deleted_by BIGINT NULL COMMENT '删除操作的用户ID',
ADD COLUMN delete_reason VARCHAR(255) NULL COMMENT '删除原因';

-- 3. 创建索引以提高查询性能
CREATE INDEX idx_posts_is_deleted ON posts(is_deleted);
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at);
CREATE INDEX idx_posts_deleted_by ON posts(deleted_by);

CREATE INDEX idx_comments_is_deleted ON comments(is_deleted);
CREATE INDEX idx_comments_deleted_at ON comments(deleted_at);
CREATE INDEX idx_comments_deleted_by ON comments(deleted_by);

-- 4. 验证字段添加成功
DESCRIBE posts;
DESCRIBE comments;

-- 5. 显示新创建的索引
SHOW INDEX FROM posts WHERE Key_name LIKE '%deleted%';
SHOW INDEX FROM comments WHERE Key_name LIKE '%deleted%'; 