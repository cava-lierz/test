# Spring Boot 缓存使用指南

## 概述

本项目使用Spring Boot内置的缓存机制，无需额外安装Redis服务器。缓存基于内存存储，使用ConcurrentHashMap实现，适合中小型应用。

## 缓存配置

### 1. 依赖配置
已在`pom.xml`中添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### 2. 缓存配置类
`src/main/java/com/mentara/config/CacheConfig.java`
- 配置了12个缓存区域
- 使用ConcurrentMapCacheManager作为缓存管理器

### 3. 配置文件
`src/main/resources/application.properties`
```properties
# 缓存配置
spring.cache.type=simple
spring.cache.cache-names=users,posts,comments,notifications,userBlocks,userFollows,tags,experts,appointments,chatRooms,moodScores,statistics
```

## 缓存注解使用

### @Cacheable - 缓存查询结果
```java
@Cacheable(value = "users", key = "#id")
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}
```

### @CacheEvict - 清除缓存
```java
@CacheEvict(value = "users", allEntries = true)
public User save(User user) {
    return userRepository.save(user);
}
```

### @CachePut - 更新缓存
```java
@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

## 缓存区域说明

| 缓存名称 | 用途 | 示例 |
|---------|------|------|
| users | 用户信息缓存 | 用户基本信息、用户统计 |
| posts | 帖子缓存 | 帖子内容、帖子列表 |
| comments | 评论缓存 | 评论内容、评论列表 |
| notifications | 通知缓存 | 通知消息、通知列表 |
| userBlocks | 用户拉黑缓存 | 拉黑关系、拉黑列表 |
| userFollows | 用户关注缓存 | 关注关系、关注列表 |
| tags | 标签缓存 | 标签信息、标签列表 |
| experts | 专家缓存 | 专家信息、专家列表 |
| appointments | 预约缓存 | 预约信息、预约列表 |
| chatRooms | 聊天室缓存 | 聊天室信息、消息历史 |
| moodScores | 心情评分缓存 | 心情数据、统计信息 |
| statistics | 统计数据缓存 | 各种统计数据 |

## 缓存管理API

### 查看缓存信息
```bash
GET /api/cache/info
```

### 查看缓存名称列表
```bash
GET /api/cache/names
```

### 清除指定缓存
```bash
DELETE /api/cache/{cacheName}
```

### 清除所有缓存
```bash
DELETE /api/cache/all
```

## 使用示例

### 1. 在Service中使用缓存
```java
@Service
public class PostService {
    
    @Cacheable(value = "posts", key = "#postId")
    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }
    
    @CacheEvict(value = "posts", allEntries = true)
    public Post createPost(Post post) {
        return postRepository.save(post);
    }
    
    @CacheEvict(value = "posts", key = "#postId")
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }
}
```

### 2. 使用缓存工具类
```java
@Autowired
private CacheUtils cacheUtils;

// 清除指定缓存
cacheUtils.evictCache("users");

// 清除所有缓存
cacheUtils.evictAllCaches();
```

## 性能优化建议

### 1. 合理设置缓存键
```java
// 好的做法：使用具体参数作为键
@Cacheable(value = "users", key = "#userId")
public User getUser(Long userId) { ... }

// 避免：使用复杂对象作为键
@Cacheable(value = "users", key = "#user")
public User getUser(User user) { ... }
```

### 2. 及时清除缓存
```java
// 在数据更新时清除相关缓存
@CacheEvict(value = {"users", "posts"}, allEntries = true)
public void updateUser(User user) { ... }
```

### 3. 避免缓存大对象
```java
// 只缓存必要的数据
@Cacheable(value = "users", key = "#userId")
public UserSummary getUserSummary(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    return new UserSummary(user.getId(), user.getNickname(), user.getAvatar());
}
```

## 监控和调试

### 1. 启用缓存统计
在`application.properties`中：
```properties
management.endpoints.web.exposure.include=caches,health,info
```

### 2. 查看缓存命中率
访问：`http://localhost:8080/api/actuator/caches`

### 3. 调试缓存
```java
@Cacheable(value = "users", key = "#id")
public Optional<User> findById(Long id) {
    log.info("从数据库查询用户: {}", id);
    return userRepository.findById(id);
}
```

## 注意事项

1. **内存使用**：缓存存储在内存中，注意监控内存使用情况
2. **数据一致性**：及时清除缓存，避免数据不一致
3. **缓存穿透**：对于不存在的键，考虑缓存null值
4. **缓存雪崩**：设置不同的过期时间，避免同时失效
5. **缓存预热**：应用启动时预加载常用数据

## 扩展为Redis

如果后续需要切换到Redis，只需要：

1. 添加Redis依赖
2. 修改缓存配置
3. 保持注解不变

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```properties
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
``` 