package com.mentara.service;

import com.mentara.dto.response.DepressCheckResponse;
import com.mentara.entity.MoodScore;
import com.mentara.entity.User;
import com.mentara.entity.Post;
import com.mentara.entity.Checkin;
import com.mentara.repository.MoodScoreRepository;
import com.mentara.repository.UserRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.CheckinRepository;
import com.mentara.service.impl.MoodScoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoodScoreServiceTest {

    @Mock
    private MoodScoreRepository moodScoreRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private DepressCheckService depressCheckService;

    @Mock
    private TextScoreService textScoreService;

    @InjectMocks
    private MoodScoreServiceImpl moodScoreService;

    private User testUser;
    private Post testPost;
    private Checkin testCheckin;
    private Long testUserId = 1L;
    private Long testPostId = 1L;
    private Long testCheckinId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        
        testPost = new Post();
        testPost.setId(testPostId);
        testPost.setAuthor(testUser);
        
        testCheckin = new Checkin();
        testCheckin.setId(testCheckinId);
        testCheckin.setUser(testUser);
    }

    @Test
    public void testCreateMoodScoreForPost() {
        // 准备测试数据
        String content = "今天心情不太好，感觉很沮丧";

        // Mock依赖服务
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(textScoreService.scoreText(content, "mood")).thenReturn(2);
        when(depressCheckService.checkDepress(content, "depression")).thenReturn(new DepressCheckResponse(1, 0.8));
        when(moodScoreRepository.save(any(MoodScore.class))).thenAnswer(invocation -> {
            MoodScore moodScore = invocation.getArgument(0);
            moodScore.setId(1L);
            return moodScore;
        });

        // 执行测试
        MoodScore result = moodScoreService.createMoodScoreForPost(testPostId, testUserId, content);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(MoodScore.ContentType.POST, result.getContentType());
        assertEquals(content, result.getContentText());
        assertEquals(2, result.getMoodGrade());
        assertTrue(result.getIsAlert()); // label为1时应该触发报警
        assertEquals(0.8, result.getAlertScore()); // 抑郁分数0.8

        // 验证方法调用
        verify(userRepository).findById(testUserId);
        verify(postRepository).findById(testPostId);
        verify(textScoreService).scoreText(content, "mood");
        verify(depressCheckService).checkDepress(content, "depression");
        verify(moodScoreRepository).save(any(MoodScore.class));
    }

    @Test
    public void testCreateMoodScoreForCheckin() {
        // 准备测试数据
        String content = "今天很开心，和朋友一起出去玩";

        // Mock依赖服务
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(checkinRepository.findById(testCheckinId)).thenReturn(Optional.of(testCheckin));
        when(textScoreService.scoreText(content, "mood")).thenReturn(9);
        when(depressCheckService.checkDepress(content, "depression")).thenReturn(new DepressCheckResponse(0, 0.1));
        when(moodScoreRepository.save(any(MoodScore.class))).thenAnswer(invocation -> {
            MoodScore moodScore = invocation.getArgument(0);
            moodScore.setId(1L);
            return moodScore;
        });

        // 执行测试
        MoodScore result = moodScoreService.createMoodScoreForCheckin(testCheckinId, testUserId, content);

        // 验证结果
        assertNotNull(result);
        assertEquals(MoodScore.ContentType.CHECKIN, result.getContentType());
        assertEquals(content, result.getContentText());
        assertEquals(9, result.getMoodGrade());
        assertFalse(result.getIsAlert()); // 积极内容不应该触发报警
        assertEquals(0.1, result.getAlertScore());

        // 验证方法调用
        verify(userRepository).findById(testUserId);
        verify(checkinRepository).findById(testCheckinId);
        verify(textScoreService).scoreText(content, "mood");
        verify(moodScoreRepository).save(any(MoodScore.class));
    }

    @Test
    public void testShouldTriggerAlert() {
        // 测试低心情得分
        assertTrue(moodScoreService.shouldTriggerAlert(1, 0.5, "正常内容"));
        
        // 测试高报警分数
        assertTrue(moodScoreService.shouldTriggerAlert(8, 0.8, "正常内容"));
        
        // 测试危险内容
        assertTrue(moodScoreService.shouldTriggerAlert(8, 0.5, "我想自杀"));
        
        // 测试正常情况
        assertFalse(moodScoreService.shouldTriggerAlert(8, 0.5, "今天心情很好"));
    }

    @Test
    public void testCalculateAlertScore() {
        // Mock抑郁检查服务的响应
        when(depressCheckService.checkDepress("今天心情很好", "depression")).thenReturn(new DepressCheckResponse(0, 0.1));
        when(depressCheckService.checkDepress("今天心情很好", "anxiety")).thenReturn(new DepressCheckResponse(0, 0.1));
        when(depressCheckService.checkDepress("今天心情很好", "stress")).thenReturn(new DepressCheckResponse(0, 0.1));
        
        // 测试正常内容
        Double score1 = moodScoreService.calculateAlertScore("今天心情很好");
        assertNotNull(score1);
        assertTrue(score1 >= 0.0 && score1 <= 1.0);
        
        // 测试空内容
        Double score2 = moodScoreService.calculateAlertScore("");
        assertEquals(0.0, score2);
        
        // 测试null内容
        Double score3 = moodScoreService.calculateAlertScore(null);
        assertEquals(0.0, score3);
    }
} 