package com.mentara.service;

import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.PostResponse;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.enums.MoodType;
import com.mentara.repository.PostRepository;
import com.mentara.repository.UserRepository;
import com.mentara.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 帖子性能测试
 * 测试1000个用户并发获取和发布帖子的性能
 */
@SpringBootTest
@ActiveProfiles("test")
public class PostPerformanceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataGenerator testDataGenerator;

    private List<User> testUsers;
    private static final int USER_COUNT = 1000;
    private static final int CONCURRENT_THREADS = 50; // 并发线程数
    
    private static final String TEST_RESULTS_FILE = "test-results.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 写入测试结果到文件
     */
    private void writeToFile(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_RESULTS_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println("[" + timestamp + "] " + content);
            writer.flush();
        } catch (IOException e) {
            System.err.println("写入测试结果文件失败: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        System.out.println("准备测试数据...");
        
        // 检查是否需要生成测试数据
        long userCount = userRepository.count();
        long testUserCount = userRepository.findAll().stream()
            .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testuser"))
            .count();
        
        System.out.println("当前用户总数: " + userCount);
        System.out.println("当前测试用户数: " + testUserCount);
        
        // 如果测试用户不足，生成测试用户
        if (testUserCount < USER_COUNT) {
            System.out.println("测试用户不足，开始生成测试用户...");
            testDataGenerator.generate1000TestUsers();
        }
        
        // 获取测试用户
        testUsers = userRepository.findAll().stream()
            .filter(user -> user.getUsername() != null && user.getUsername().startsWith("testuser"))
            .limit(USER_COUNT)
            .toList();
        
        System.out.println("找到 " + testUsers.size() + " 个测试用户");
        
        // 检查是否有足够的帖子
        long postCount = postRepository.count();
        System.out.println("数据库中帖子总数: " + postCount);
        
        if (postCount < 100) {
            System.out.println("帖子数量不足，开始生成测试帖子...");
            testDataGenerator.generateTestPosts(500);
        }
    }

    @Test
    public void testConcurrentGetPosts() {
        writeToFile("=== 开始并发获取帖子性能测试 ===");
        writeToFile("用户数量: " + testUsers.size());
        writeToFile("并发线程数: " + CONCURRENT_THREADS);
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        
        try {
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 创建并发任务
            List<CompletableFuture<Long>> futures = testUsers.stream()
                .map(user -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return measureGetPostsTime(user.getId());
                    } catch (Exception e) {
                        System.err.println("用户 " + user.getUsername() + " 获取帖子失败: " + e.getMessage());
                        return -1L; // 返回-1表示失败
                    }
                }, executor))
                .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 计算总时间
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 收集结果
            List<Long> responseTimes = futures.stream()
                .map(CompletableFuture::join)
                .filter(time -> time > 0) // 过滤掉失败的结果
                .toList();
            
            // 计算统计数据
            if (!responseTimes.isEmpty()) {
                double avgResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
                
                long minResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0);
                
                long maxResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);
                
                long successfulRequests = responseTimes.size();
                long failedRequests = testUsers.size() - successfulRequests;
                
                // 输出结果
                writeToFile("=== GET /api/posts 性能测试结果 ===");
                writeToFile("总测试时间: " + totalTime + "ms");
                writeToFile("成功请求数: " + successfulRequests);
                writeToFile("失败请求数: " + failedRequests);
                writeToFile("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
                writeToFile("最小响应时间: " + minResponseTime + "ms");
                writeToFile("最大响应时间: " + maxResponseTime + "ms");
                writeToFile("总吞吐量: " + String.format("%.2f", (double) successfulRequests / (totalTime / 1000.0)) + " 请求/秒");
                
                // 验证结果
                assertTrue(successfulRequests > 0, "应该有成功的请求");
                assertTrue(avgResponseTime > 0, "平均响应时间应该大于0");
                
            } else {
                writeToFile("所有请求都失败了");
                fail("所有请求都失败了");
            }
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void testConcurrentCreatePosts() {
        writeToFile("=== 开始并发发布帖子性能测试 ===");
        writeToFile("用户数量: " + testUsers.size());
        writeToFile("并发线程数: " + CONCURRENT_THREADS);
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        
        try {
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 创建并发任务
            List<CompletableFuture<Long>> futures = testUsers.stream()
                .map(user -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return measureCreatePostTime(user.getId());
                    } catch (Exception e) {
                        System.err.println("用户 " + user.getUsername() + " 发布帖子失败: " + e.getMessage());
                        return -1L; // 返回-1表示失败
                    }
                }, executor))
                .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 计算总时间
            long totalTime = System.currentTimeMillis() - startTime;
            
            // 收集结果
            List<Long> responseTimes = futures.stream()
                .map(CompletableFuture::join)
                .filter(time -> time > 0) // 过滤掉失败的结果
                .toList();
            
            // 计算统计数据
            if (!responseTimes.isEmpty()) {
                double avgResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
                
                long minResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0);
                
                long maxResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);
                
                long successfulRequests = responseTimes.size();
                long failedRequests = testUsers.size() - successfulRequests;
                
                // 输出结果
                writeToFile("=== POST /api/posts 性能测试结果 ===");
                writeToFile("总测试时间: " + totalTime + "ms");
                writeToFile("成功请求数: " + successfulRequests);
                writeToFile("失败请求数: " + failedRequests);
                writeToFile("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
                writeToFile("最小响应时间: " + minResponseTime + "ms");
                writeToFile("最大响应时间: " + maxResponseTime + "ms");
                writeToFile("总吞吐量: " + String.format("%.2f", (double) successfulRequests / (totalTime / 1000.0)) + " 请求/秒");
                
                // 验证结果
                assertTrue(successfulRequests > 0, "应该有成功的请求");
                assertTrue(avgResponseTime > 0, "平均响应时间应该大于0");
                
            } else {
                writeToFile("所有请求都失败了");
                fail("所有请求都失败了");
            }
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void testSequentialGetPosts() {
        writeToFile("=== 开始顺序获取帖子性能测试 ===");
        writeToFile("用户数量: " + testUsers.size());
        
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = testUsers.stream()
            .limit(100) // 限制为100个用户，避免测试时间过长
            .map(user -> {
                try {
                    return measureGetPostsTime(user.getId());
                } catch (Exception e) {
                    System.err.println("用户 " + user.getUsername() + " 获取帖子失败: " + e.getMessage());
                    return -1L;
                }
            })
            .filter(time -> time > 0)
            .toList();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        if (!responseTimes.isEmpty()) {
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
            
            writeToFile("=== 顺序GET测试结果 ===");
            writeToFile("总测试时间: " + totalTime + "ms");
            writeToFile("成功请求数: " + responseTimes.size());
            writeToFile("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
            
            assertTrue(avgResponseTime > 0, "平均响应时间应该大于0");
        }
    }

    @Test
    public void testSequentialCreatePosts() {
        writeToFile("=== 开始顺序发布帖子性能测试 ===");
        writeToFile("用户数量: " + testUsers.size());
        
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = testUsers.stream()
            .limit(50) // 限制为50个用户，避免测试时间过长
            .map(user -> {
                try {
                    return measureCreatePostTime(user.getId());
                } catch (Exception e) {
                    System.err.println("用户 " + user.getUsername() + " 发布帖子失败: " + e.getMessage());
                    return -1L;
                }
            })
            .filter(time -> time > 0)
            .toList();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        if (!responseTimes.isEmpty()) {
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
            
            writeToFile("=== 顺序POST测试结果 ===");
            writeToFile("总测试时间: " + totalTime + "ms");
            writeToFile("成功请求数: " + responseTimes.size());
            writeToFile("平均响应时间: " + String.format("%.2f", avgResponseTime) + "ms");
            
            assertTrue(avgResponseTime > 0, "平均响应时间应该大于0");
        }
    }

    /**
     * 测量获取帖子的时间
     */
    private Long measureGetPostsTime(Long userId) {
        long startTime = System.nanoTime();
        
        try {
            Pageable pageable = PageRequest.of(0, 20); // 第一页，20条记录
            Page<PostResponse> posts = postService.findAllPosts(pageable, userId);
            
            long endTime = System.nanoTime();
            return (endTime - startTime) / 1_000_000; // 转换为毫秒
        } catch (Exception e) {
            return -1L;
        }
    }

    /**
     * 测量发布帖子的时间
     */
    private Long measureCreatePostTime(Long userId) {
        long startTime = System.nanoTime();
        
        try {
            // 创建测试帖子请求
            PostRequest postRequest = new PostRequest();
            postRequest.setTitle("testuser" + userId + "测试发帖");
            postRequest.setContent("发帖内容");
            postRequest.setMood(MoodType.HAPPY);
            postRequest.setTagIds(List.of(2L)); // 使用标签ID 2
            postRequest.setImageUrls(List.of()); // 空图片列表
            
            // 调用服务创建帖子
            PostResponse postResponse = postService.createPostForUser(postRequest, userId);
            
            long endTime = System.nanoTime();
            return (endTime - startTime) / 1_000_000; // 转换为毫秒
        } catch (Exception e) {
            return -1L;
        }
    }
} 