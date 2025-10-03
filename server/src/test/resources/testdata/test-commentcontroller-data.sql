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

-- 用户认证信息
INSERT INTO user_auths (id, email, password) VALUES
  (1, 'admin@example.com', 'testpass'),
  (2, 'user1@example.com', 'testpass');

-- 帖子
INSERT INTO posts (id, title, content, state, likes_count, comments_count, report_count, is_deleted, author_id, is_announcement, created_at)
VALUES
    (1, '测试帖子1', '内容1', 'VALID', 0, 3, 0, false, 2, false, CURRENT_TIMESTAMP),
    (2, 'admin的帖子', '内容2', 'VALID', 0, 0, 0, false, 1, false, CURRENT_TIMESTAMP);
ALTER TABLE posts ALTER COLUMN id RESTART WITH 3;
-- 评论
INSERT INTO comments (id, post_id, author_id, content, is_deleted, report_count, likes_count, replys_count)
VALUES (1, 1, 2, 'test comment', false, 0, 0, 0);
-- 新增一条子评论，top_comment_id=1，parent_id=1
INSERT INTO comments (id, post_id, author_id, content, is_deleted, report_count, likes_count, replys_count, parent_id, top_comment_id)
VALUES (2, 1, 2, 'reply comment', false, 0, 0, 0, 1, 1),
       (3, 1, 2, 'reply comment', false, 0, 0, 0, 2, 1);
ALTER TABLE comments ALTER COLUMN id RESTART WITH 4;

-- 点赞
INSERT INTO comment_likes (id, comment_id, user_id) VALUES (1, 1, 2);
ALTER TABLE comment_likes ALTER COLUMN id RESTART WITH 2;