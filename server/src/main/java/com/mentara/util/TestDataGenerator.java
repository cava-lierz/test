package com.mentara.util;

import com.mentara.entity.Post;
import com.mentara.entity.PostLike;
import com.mentara.entity.PostTag;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import com.mentara.entity.UserRole;
import com.mentara.enums.MoodType;
import com.mentara.enums.PostState;
import com.mentara.repository.PostRepository;
import com.mentara.repository.PostTagRepository;
import com.mentara.repository.UserRepository;
import com.mentara.repository.PostLikeRepository;
import com.mentara.repository.CommentRepository;
import com.mentara.repository.CommentLikeRepository;
import com.mentara.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据生成器
 * 用于生成大量测试用户和帖子数据
 */
@Component
public class TestDataGenerator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 生成1000个测试用户
     */
    public void generate1000TestUsers() {
        System.out.println("开始生成1000个测试用户...");
        
        List<User> users = new ArrayList<>();
        
        for (int i = 1; i <= 1000; i++) {
            // 创建用户
            User user = new User();
            user.setUsername("testuser" + i);
            user.setNickname("测试用户" + i);
            user.setRole(UserRole.USER);
            user.setIsDisabled(false);
            user.setReportedCount(0);
            user.setIsProfilePublic(true);
            user.setIsDeleted(false);
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=testuser" + i);
            
            // 创建认证信息
            UserAuth userAuth = new UserAuth();
            userAuth.setEmail("testuser@" + i + ".com");
            userAuth.setPassword(passwordEncoder.encode("000000"));
            userAuth.setUser(user);
            user.setUserAuth(userAuth);
            
            users.add(user);
            
            // 每100个用户输出一次进度
            if (i % 100 == 0) {
                System.out.println("已生成 " + i + " 个用户");
            }
        }
        
        // 批量保存用户（UserAuth会通过级联自动保存）
        userRepository.saveAll(users);
        System.out.println("用户和认证信息保存完成");
        
        System.out.println("1000个测试用户生成完成！");
    }

    /**
     * 生成测试帖子
     */
    public void generateTestPosts(int count) {
        System.out.println("开始生成 " + count + " 个测试帖子...");
        
        List<User> users = userRepository.findAll();
        List<PostTag> tags = postTagRepository.findAll();
        
        if (users.isEmpty()) {
            System.out.println("错误：没有找到用户，请先生成测试用户");
            return;
        }
        
        List<Post> posts = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Post post = new Post();
            post.setTitle("测试帖子标题 " + i);
            post.setContent("这是测试帖子 " + i + " 的内容，用于性能测试。");
            post.setMood(MoodType.HAPPY);
            post.setState(PostState.VALID);
            post.setLikesCount(0);
            post.setCommentsCount(0);
            post.setReportCount(0);
            post.setIsDeleted(false);
            post.setIsAnnouncement(false);
            post.setCreatedAt(LocalDateTime.now());
            
            // 随机分配作者
            User author = users.get(i % users.size());
            post.setAuthor(author);
            
            // 随机分配标签
            if (!tags.isEmpty()) {
                List<PostTag> postTags = new ArrayList<>();
                postTags.add(tags.get(i % tags.size()));
                post.setTags(postTags);
            }
            
            posts.add(post);
            
            // 每100个帖子输出一次进度
            if (i % 100 == 0) {
                System.out.println("已生成 " + i + " 个帖子");
            }
        }
        
        // 批量保存帖子
        postRepository.saveAll(posts);
        System.out.println(count + " 个测试帖子生成完成！");
    }

    /**
     * 清理测试数据
     */
    public void cleanupTestData() {
        System.out.println("开始清理测试数据...");
        
        // 获取测试用户ID列表
        List<Long> testUserIds = userRepository.findAll().stream()
            .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testuser"))
            .map(User::getId)
            .toList();
        
        // 获取测试帖子ID列表
        List<Long> testPostIds = postRepository.findAll().stream()
            .filter(post -> post.getTitle() != null && post.getTitle().startsWith("测试帖子标题"))
            .map(Post::getId)
            .toList();
        
        if (!testUserIds.isEmpty() || !testPostIds.isEmpty()) {
            // 1. 删除测试帖子相关的点赞记录
            if (!testPostIds.isEmpty()) {
                for (Long postId : testPostIds) {
                    postLikeRepository.deleteByPostId(postId);
                }
                System.out.println("已清理测试帖子点赞数据");
            }
            
            // 2. 删除测试用户的点赞记录
            if (!testUserIds.isEmpty()) {
                for (Long userId : testUserIds) {
                    commentLikeRepository.deleteByUserId(userId);
                }
                System.out.println("已清理测试用户点赞数据");
            }
            
            // 3. 删除测试帖子
            if (!testPostIds.isEmpty()) {
                List<Post> testPosts = postRepository.findAllById(testPostIds);
                postRepository.deleteAll(testPosts);
                System.out.println("已删除 " + testPosts.size() + " 个测试帖子");
            }
            
            // 4. 删除测试用户
            if (!testUserIds.isEmpty()) {
                List<User> testUsers = userRepository.findAllById(testUserIds);
                userRepository.deleteAll(testUsers);
                System.out.println("已删除 " + testUsers.size() + " 个测试用户");
            }
        } else {
            System.out.println("没有找到需要清理的测试数据");
        }
        
        System.out.println("测试数据清理完成！");
    }

    /**
     * 检查测试数据状态
     */
    public void checkTestDataStatus() {
        long userCount = userRepository.count();
        long testUserCount = userRepository.findAll().stream()
            .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testuser"))
            .count();
        
        long postCount = postRepository.count();
        long testPostCount = postRepository.findAll().stream()
            .filter(post -> post.getTitle() != null && post.getTitle().startsWith("测试帖子标题"))
            .count();
        
        System.out.println("=== 测试数据状态 ===");
        System.out.println("总用户数: " + userCount);
        System.out.println("测试用户数: " + testUserCount);
        System.out.println("总帖子数: " + postCount);
        System.out.println("测试帖子数: " + testPostCount);
    }
} 