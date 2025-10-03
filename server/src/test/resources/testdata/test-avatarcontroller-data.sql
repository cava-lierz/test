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
INSERT INTO users (id, username, role, is_disabled, reported_count, is_profile_public, is_deleted, avatar) VALUES
  (1, 'admin', 'ADMIN', false, 0, true, false, NULL),
  (2, 'user1', 'USER', false, 0, true, false, NULL);
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;
-- 用户认证信息
INSERT INTO user_auths (id, email, password) VALUES
  (1, 'admin@example.com', 'testpass'),
  (2, 'user1@example.com', 'testpass'); 