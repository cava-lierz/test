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
  (2, 'user1', 'USER', false, 0, true, false);
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;

-- 帖子
INSERT INTO posts (id, title, content, state, likes_count, comments_count, report_count, is_deleted, author_id, is_announcement, created_at)
VALUES
  (1, '测试帖子1', '内容1', 'PENDING', 0, 0, 0, false, 2, false, CURRENT_TIMESTAMP),
  (2, '被软删除的帖子', '内容2', 'INVALID', 0, 0, 0, true, 2, false, CURRENT_TIMESTAMP);
ALTER TABLE posts ALTER COLUMN id RESTART WITH 3;

-- 评论
INSERT INTO comments (id, content, created_at, author_id, post_id, replys_count, likes_count, report_count, is_deleted)
VALUES
  (1, '测试评论1', CURRENT_TIMESTAMP, 2, 1, 0, 0, 0, false),
  (2, '被软删除的评论', CURRENT_TIMESTAMP, 2, 2, 0, 0, 0, true);
ALTER TABLE comments ALTER COLUMN id RESTART WITH 3;

-- 聊天室
INSERT INTO chat_rooms (id, name, description, type, is_deleted, created_at)
VALUES
  (1, '测试聊天室', '描述', 'REALNAME', false, CURRENT_TIMESTAMP);
ALTER TABLE chat_rooms ALTER COLUMN id RESTART WITH 2;