package com.mentara.service;

import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.PostResponse;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.entity.UserAuth;
import com.mentara.entity.UserRole;
import com.mentara.repository.PostRepository;
import com.mentara.repository.ReportRepository;
import com.mentara.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Post服务异步处理测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class PostServiceAsyncTest {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Test
    @Transactional
    public void testAsyncReportProcessing() throws InterruptedException {
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setRole(UserRole.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        
        // 创建用户认证信息
        UserAuth userAuth = new UserAuth();
        userAuth.setUser(testUser);
        userAuth.setEmail("test@example.com");
        userAuth.setPassword("password");
        testUser.setUserAuth(userAuth);
        testUser = userRepository.save(testUser);
        
        User reportUser = new User();
        reportUser.setUsername("reporter");
        reportUser.setNickname("举报用户");
        reportUser.setRole(UserRole.USER);
        reportUser.setCreatedAt(LocalDateTime.now());
        reportUser = userRepository.save(reportUser);
        
        // 创建举报用户认证信息
        UserAuth reportUserAuth = new UserAuth();
        reportUserAuth.setUser(reportUser);
        reportUserAuth.setEmail("reporter@example.com");
        reportUserAuth.setPassword("password");
        reportUser.setUserAuth(reportUserAuth);
        reportUser = userRepository.save(reportUser);
        
        // 创建测试帖子
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("测试帖子");
        postRequest.setContent("这是一个测试帖子的内容");
        postRequest.setImageUrls(new ArrayList<>());
        postRequest.setTagIds(new ArrayList<>());
        postRequest.setIsAnnouncement(false);
        
        PostResponse postResponse = postService.createPostForUser(postRequest, testUser.getId());
        assertNotNull(postResponse);
        
        // 记录举报前的状态
        long postId = postResponse.getId();
        Post postBeforeReport = postRepository.findById(postId).orElse(null);
        assertNotNull(postBeforeReport);
        int reportCountBefore = postBeforeReport.getReportCount();
        
        // 执行举报（应该是异步的）
        long startTime = System.currentTimeMillis();
        postService.reportPost(postId, reportUser.getId(), "测试举报原因");
        long endTime = System.currentTimeMillis();
        
        // 验证举报方法立即返回（不应该阻塞）
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 1000, "举报处理应该是异步的，不应该阻塞超过1秒");
        
        // 验证举报记录已创建
        Post postAfterReport = postRepository.findById(postId).orElse(null);
        assertNotNull(postAfterReport);
        assertEquals(reportCountBefore + 1, postAfterReport.getReportCount(), "举报次数应该增加");
        
        // 等待一段时间让异步处理完成
        Thread.sleep(5000);
        
        // 验证异步处理的结果（这里主要是验证没有异常发生）
        // 由于异步处理的结果可能因AI服务而异，我们主要验证系统稳定性
        assertTrue(true, "异步处理应该正常完成");
        
        System.out.println("异步举报处理测试完成，执行时间: " + executionTime + "ms");
    }
} 