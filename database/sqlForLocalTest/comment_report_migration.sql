-- 评论举报功能数据库迁移脚本

-- 1. 创建评论举报表
CREATE TABLE IF NOT EXISTS comment_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'PROCESSED', 'REJECTED') DEFAULT 'PENDING',
    processed_at TIMESTAMP NULL,
    processed_by BIGINT NULL,
    FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES user(id) ON DELETE SET NULL,
    UNIQUE KEY unique_comment_reporter (comment_id, reporter_id)
);

-- 2. 为评论表添加举报次数字段
ALTER TABLE comment ADD COLUMN report_count INT DEFAULT 0;

-- 3. 更新现有记录的report_count字段为0
UPDATE comment SET report_count = 0 WHERE report_count IS NULL;

-- 4. 创建索引以提高查询性能
CREATE INDEX idx_comment_report_comment_id ON comment_report(comment_id);
CREATE INDEX idx_comment_report_reporter_id ON comment_report(reporter_id);
CREATE INDEX idx_comment_report_status ON comment_report(status);
CREATE INDEX idx_comment_report_created_at ON comment_report(created_at);

-- 5. 插入示例数据（可选）
-- INSERT INTO comment_report (comment_id, reporter_id, reason) VALUES (1, 2, '不当或冒犯性内容'); 