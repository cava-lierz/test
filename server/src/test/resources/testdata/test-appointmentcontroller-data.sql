-- 通知相关
DELETE FROM notifications;
-- 举报/点赞/标签/关联
DELETE FROM comment_reports;
DELETE FROM reports;
DELETE FROM post_likes;
DELETE FROM comment_likes;
DELETE FROM post_tags;
DELETE FROM tags;
-- 评论/帖子/心情分/签到
DELETE FROM comments;
DELETE FROM mood_scores;
DELETE FROM post_images;
DELETE FROM posts;
DELETE FROM checkins;
-- 用户关系
DELETE FROM user_blocks;
DELETE FROM user_follows;
-- 聊天相关
DELETE FROM chat_messages;
DELETE FROM chat_room_users;
DELETE FROM chat_rooms;
-- 专家/预约
DELETE FROM appointments;
DELETE FROM expert_schedules;
DELETE FROM experts;
-- 用户认证/主表
DELETE FROM user_auths;
DELETE FROM users;

-- 用户
INSERT INTO users (id, username, role, is_disabled, reported_count, is_profile_public, is_deleted) VALUES
  (1, 'admin', 'ADMIN', false, 0, true, false),
  (2, 'user1', 'USER', false, 0, true, false),
  (3, 'expert1', 'EXPERT', false, 0, true, false);
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;

-- 专家
INSERT INTO experts (id, user_id, name, specialty, contact, status) VALUES
  (1, 3, '专家A', '心理咨询', 'expert1@example.com', 'online');
ALTER TABLE experts ALTER COLUMN id RESTART WITH 2;

-- 预约
INSERT INTO appointments (id, user_id, expert_user_id, expert_id, appointment_time, status, description, contact_info, duration, created_at, updated_at)
VALUES
  (1, 2, 3, 1, '2024-01-01 10:00:00', 'pending', '预约测试', 'user1@example.com', 55, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
ALTER TABLE appointments ALTER COLUMN id RESTART WITH 2;