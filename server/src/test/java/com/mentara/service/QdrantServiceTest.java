package com.mentara.service;

import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.enums.MoodType;
import com.mentara.service.EmbeddingService;
import com.mentara.service.impl.QdrantServiceImpl;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;

@ExtendWith(MockitoExtension.class)
class QdrantServiceTest {

    @Mock
    private QdrantClient qdrantClient;

    @Mock
    private EmbeddingService embeddingService;

    private QdrantServiceImpl qdrantService;

    @BeforeEach
    void setUp() {
        qdrantService = new QdrantServiceImpl();
        
        // 使用反射设置依赖
        ReflectionTestUtils.setField(qdrantService, "qdrantClient", qdrantClient);
        ReflectionTestUtils.setField(qdrantService, "embeddingService", embeddingService);
        ReflectionTestUtils.setField(qdrantService, "collectionName", "post_vector");
    }

    @Test
    void testUpsertPostVector_Success() {
        // 准备测试数据
        Post post = createTestPost(1L, "喜欢香蕉", "喜欢苹果", MoodType.HAPPY);

        // mock embeddingService
        when(embeddingService.getEmbedding(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f));

        // 执行测试
        Long result = qdrantService.upsertPostVector(post);

        // 验证结果
        assertEquals(post.getId(), result, "向量存储应该返回帖子ID");
    }

    @Test
    void testUpsertPostVector_EmbeddingFailure() {
        // 准备测试数据
        Post post = createTestPost(1L, "测试标题", "测试内容", MoodType.HAPPY);
        
        // 模拟EmbeddingService返回null
        when(embeddingService.getEmbedding(anyString())).thenReturn(null);
        
        // 执行测试
        Long result = qdrantService.upsertPostVector(post);
        
        // 验证结果
        assertNull(result, "当无法获取向量时应该返回null");
    }

    @Test
    void testQueryPostVector_Success() {
        // 准备测试数据
        Post queryPost = createTestPost(3L, "心情状态", "今天心情很好", MoodType.HAPPY);
        List<Float> mockVector = List.of(0.1f, 0.2f, 0.3f);
        
        // 创建模拟的搜索结果
        Points.ScoredPoint mockPoint = Points.ScoredPoint.newBuilder()
                .setId(Points.PointId.newBuilder().setNum(1L).build())
                .setScore(0.95f)
                .build();
        List<Points.ScoredPoint> mockResults = List.of(mockPoint);
        
        // 模拟服务调用
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockVector);
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(mockResults));
        
        // 查询相似帖子
        List<Points.ScoredPoint> results = qdrantService.queryPostVector("心情状态 今天心情很好");
        
        // 验证结果
        assertNotNull(results, "应该找到相似的帖子");
        assertEquals(1, results.size(), "应该返回一个结果");
        assertEquals(1L, results.get(0).getId().getNum(), "应该返回正确的帖子ID");
    }

    @Test
    void testQueryPostVector_NoSimilarPosts() {
        // 准备测试数据
        Post queryPost = createTestPost(1L, "测试查询", "测试内容", MoodType.HAPPY);
        List<Float> mockVector = List.of(0.1f, 0.2f, 0.3f);
        
        // 模拟服务调用
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockVector);
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(null));
        
        // 查询相似帖子
        List<Points.ScoredPoint> results = qdrantService.queryPostVector("测试查询 测试内容");
        
        // 验证结果
        assertNull(results, "没有存储的帖子时应该返回null");
    }

    @Test
    void testQueryPostVector_EmbeddingFailure() {
        // 准备测试数据
        Post queryPost = createTestPost(1L, "测试查询", "测试内容", MoodType.HAPPY);
        
        // 模拟EmbeddingService返回null
        when(embeddingService.getEmbedding(anyString())).thenReturn(null);
        
        // 查询相似帖子
        List<Points.ScoredPoint> results = qdrantService.queryPostVector(buildQueryText(queryPost));
        
        // 验证结果
        assertNull(results, "当无法获取向量时应该返回null");
    }

    @Test
    void testDeletePostVector_Success() {
        // 准备测试数据
        Post post = createTestPost(1L, "测试标题", "测试内容", MoodType.HAPPY);
        
        // 执行删除
        Long result = qdrantService.deletePostVector(post);
        
        // 验证结果
        assertEquals(post.getId(), result, "删除应该返回帖子ID");
    }

    @Test
    void testAccurateQueryPostVector_Success() {
        // 准备测试数据
        Post queryPost = createTestPost(3L, "心情状态", "今天心情很好", MoodType.HAPPY);
        List<Float> mockVector = List.of(0.1f, 0.2f, 0.3f);
        
        // 创建模拟的搜索结果
        Points.ScoredPoint mockPoint = Points.ScoredPoint.newBuilder()
                .setId(Points.PointId.newBuilder().setNum(1L).build())
                .setScore(0.95f)
                .build();
        List<Points.ScoredPoint> mockResults = List.of(mockPoint);
        
        // 模拟服务调用
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockVector);
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(mockResults));
        
        // 精确查询相似帖子
        List<Points.ScoredPoint> results = qdrantService.accurateQueryPostVector("心情状态 今天心情很好");
        
        // 验证结果
        assertNotNull(results, "应该找到相似的帖子");
        assertEquals(1, results.size(), "应该返回一个结果");
        assertEquals(1L, results.get(0).getId().getNum(), "应该返回正确的帖子ID");
    }

    @Test
    void testAccurateQueryPostVector_NoResults() {
        // 准备测试数据
        Post queryPost = createTestPost(1L, "测试查询", "测试内容", MoodType.HAPPY);
        List<Float> mockVector = List.of(0.1f, 0.2f, 0.3f);
        
        // 模拟服务调用
        when(embeddingService.getEmbedding(anyString())).thenReturn(mockVector);
        when(qdrantClient.queryAsync(any())).thenReturn(Futures.immediateFuture(null));
        
        // 精确查询相似帖子
        List<Points.ScoredPoint> results = qdrantService.accurateQueryPostVector(buildQueryText(queryPost));
        
        // 验证结果
        assertNull(results, "没有找到相似帖子时应该返回null");
    }

    @Test
    void testBuildPostText_CompletePost() {
        // 准备测试数据
        Post post = createTestPost(1L, "测试标题", "测试内容", MoodType.HAPPY);
        
        // 使用反射调用私有方法
        String postText = (String) ReflectionTestUtils.invokeMethod(qdrantService, "buildPostText", post);
        
        // 验证结果
        assertNotNull(postText, "构建的文本不应该为null");
        assertTrue(postText.contains("测试标题"), "应该包含标题");
        assertTrue(postText.contains("测试内容"), "应该包含内容");
        assertTrue(postText.contains("心情: HAPPY"), "应该包含心情信息");
    }

    @Test
    void testBuildPostText_MinimalPost() {
        // 准备测试数据
        Post post = createTestPost(1L, null, null, null);
        
        // 使用反射调用私有方法
        String postText = (String) ReflectionTestUtils.invokeMethod(qdrantService, "buildPostText", post);
        
        // 验证结果
        assertNotNull(postText, "构建的文本不应该为null");
        assertEquals("", postText.trim(), "空帖子应该生成空文本");
    }

    @Test
    void testBuildPostText_WithTags() {
        // 准备测试数据
        Post post = createTestPost(1L, "测试标题", "测试内容", MoodType.HAPPY);
        // 这里可以添加标签测试，如果需要的话
        
        // 使用反射调用私有方法
        String postText = (String) ReflectionTestUtils.invokeMethod(qdrantService, "buildPostText", post);
        
        // 验证结果
        assertNotNull(postText, "构建的文本不应该为null");
        assertTrue(postText.contains("测试标题"), "应该包含标题");
        assertTrue(postText.contains("测试内容"), "应该包含内容");
        assertTrue(postText.contains("心情: HAPPY"), "应该包含心情信息");
    }

    // 辅助方法
    private Post createTestPost(Long id, String title, String content, MoodType mood) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setMood(mood);
        post.setCreatedAt(LocalDateTime.now());
        post.setIsAnnouncement(false);
        
        // 创建模拟用户
        User user = new User();
        user.setId(1L);
        post.setAuthor(user);
        
        return post;
    }

    // 辅助方法
    private String buildQueryText(Post post) {
        StringBuilder text = new StringBuilder();
        if (post.getTitle() != null) {
            text.append(post.getTitle()).append(" ");
        }
        if (post.getContent() != null) {
            text.append(post.getContent());
        }
        return text.toString().trim();
    }
} 