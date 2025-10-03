package com.mentara.service.impl;

import com.mentara.dto.request.PostReportAuditRequest;
import com.mentara.dto.request.PostRequest;
import com.mentara.dto.response.PostReportAuditResponse;
import com.mentara.dto.response.PostResponse;
import com.mentara.dto.response.ReportedPostResponse;
import com.mentara.entity.Post;
import com.mentara.entity.PostLike;
import com.mentara.entity.Report;
import com.mentara.entity.User;
import com.mentara.repository.PostRepository;
import com.mentara.repository.PostLikeRepository;
import com.mentara.repository.ReportRepository;
import com.mentara.repository.CommentRepository;
import com.mentara.repository.CommentLikeRepository;
import com.mentara.service.*;
import com.mentara.converter.PostConverter;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.NotificationRepository;
import com.mentara.enums.MoodType;

import io.qdrant.client.grpc.Points;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mentara.converter.NotificationResponseFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.ApplicationContext;
import com.mentara.dto.response.PostAuditResponse;
import com.mentara.enums.PostState;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private TagService tagService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationResponseFactory responseFactory;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private MoodScoreService moodScoreService;

    @Autowired
    private QdrantService qdrantService;

    @Autowired
    private PostReportAuditService postReportAuditService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PostAuditService postAuditService;

    @Override
    @Cacheable(value = "posts", key = "#postId + '_' + #currentUserId")
    public PostResponse findById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 检查帖子是否已被软删除
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 检查帖子状态是否为VALID（只有已通过的帖子才能被查看）
        if (post.getState() != PostState.VALID) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 查询点赞状态
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = postLikeRepository.existsByPostAndUser(post, userService.findById(currentUserId).orElse(null));
        }
        
        return postConverter.toResponse(post, isLiked);
    }

    @Override
    @Cacheable(value = "posts", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #currentUserId")
    public Page<PostResponse> findAllPosts(Pageable pageable, Long currentUserId) {
        // 只返回已审核通过的帖子
        Page<Post> postsPage = postRepository.findValidPosts(pageable);
        return optimizePostResponsePage(postsPage, currentUserId);
    }

    @Override
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable, Long currentUserId) {
        // 如果关键词为空，返回所有帖子
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllPosts(pageable, currentUserId);
        }
        
        try {
            // 通过向量搜索获取相似的帖子ID
            List<Points.ScoredPoint> postVecs = qdrantService.queryPostVector(keyword);
            
            if (postVecs == null || postVecs.isEmpty()) {
                // 如果没有找到相似帖子，返回空的分页结果
                return Page.empty(pageable);
            }
            
            // 提取帖子ID列表
            List<Long> postIds = postVecs.stream()
                .map(postVec -> postVec.getId().getNum())
                .collect(Collectors.toList());
            
            // 根据ID列表查询帖子（只查询未软删除的）
            List<Post> posts = postRepository.findByIdInAndIsDeletedFalseOrderByCreatedAtDesc(postIds);
            
            // 按照向量搜索的相似度顺序重新排序帖子
            List<Post> sortedPosts = new ArrayList<>();
            for (Long postId : postIds) {
                posts.stream()
                    .filter(post -> post.getId().equals(postId))
                    .findFirst()
                    .ifPresent(sortedPosts::add);
            }
            
            // 应用分页
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), sortedPosts.size());
            
            if (start >= sortedPosts.size()) {
                return Page.empty(pageable);
            }
            
            List<Post> pagedPosts = sortedPosts.subList(start, end);
            
            // 批量查询用户信息，避免N+1查询
            optimizeUserInfoLoading(pagedPosts);
            
            // 批量查询点赞状态
            Map<Long, Boolean> likeStatusMap = batchQueryLikeStatus(pagedPosts, currentUserId);
            
            // 转换为 PostResponse
            List<PostResponse> postResponses = pagedPosts.stream()
                .map(post -> postConverter.toResponse(post, likeStatusMap.getOrDefault(post.getId(), false)))
                .collect(Collectors.toList());
            
            // 创建分页结果
            return new org.springframework.data.domain.PageImpl<>(
                postResponses, 
                pageable, 
                sortedPosts.size()
            );
            
        } catch (Exception e) {
            // 如果向量搜索失败，回退到普通搜索或返回空结果
            System.err.println("向量搜索失败: " + e.getMessage());
            return Page.empty(pageable);
        }
    }

    @Override
    @Cacheable(value = "posts", key = "'user_' + #userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #currentUserId")
    public Page<PostResponse> findPostsByUser(Long userId, Pageable pageable, Long currentUserId) {
        // 只返回已审核通过且未软删除的帖子
        Page<Post> postsPage = postRepository.findByAuthorIdAndStateAndIsDeletedFalseOrderByCreatedAtDesc(userId, PostState.VALID, pageable);
        return optimizePostResponsePage(postsPage, currentUserId);
    }

    @Override
    @Cacheable(value = "posts", key = "'filter_' + #filter + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #currentUserId")
    public Page<PostResponse> findPostsByFilter(String filter, Pageable pageable, Long currentUserId) {
        long startTime = System.currentTimeMillis();
        Page<Post> posts;
        
        switch (filter) {
            case "最新":
                // 使用数据库层面查询：最近24小时的已通过帖子
                LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
                posts = postRepository.findByStateAndIsDeletedFalseAndCreatedAtAfterOrderByIsAnnouncementDescCreatedAtDesc(PostState.VALID, twentyFourHoursAgo, pageable);
                break;
            case "最热":
                // 使用数据库层面查询：点赞数大于等于4的已通过帖子
                posts = postRepository.findByStateAndIsDeletedFalseAndLikesCountGreaterThanEqualOrderByIsAnnouncementDescLikesCountDesc(PostState.VALID, 4, pageable);
                break;
            case "心情":
                // 使用数据库层面查询：有心情的已通过帖子
                posts = postRepository.findByStateAndMoodIsNotNullAndIsDeletedFalseOrderByIsAnnouncementDescCreatedAtDesc(PostState.VALID, pageable);
                break;
            default:
                // 全部：按创建时间排序的已通过帖子
                posts = postRepository.findValidPosts(pageable);
                break;
        }
        
        return optimizePostResponsePage(posts, currentUserId);
    }

    @Override
    public Page<PostResponse> findPostsByTags(List<Long> tagIds, Pageable pageable, Long currentUserId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return findAllPosts(pageable, currentUserId);
        }
        // 使用数据库层面查询：按标签筛选已通过的帖子
        long tagCount = tagIds.size();
        Page<Post> posts = postRepository.findByStateAndTagIdsAndIsDeletedFalse(PostState.VALID, tagIds, tagCount, pageable);
        return optimizePostResponsePage(posts, currentUserId);
    }

    @Override
    @Cacheable(value = "posts", key = "'mood_' + #mood + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #currentUserId")
    public Page<PostResponse> findPostsByMood(MoodType mood, Pageable pageable, Long currentUserId) {
        // 使用数据库层面查询：按心情类型筛选已通过的帖子
        Page<Post> posts = postRepository.findByStateAndMoodAndIsDeletedFalseOrderByIsAnnouncementDescCreatedAtDesc(PostState.VALID, mood, pageable);
        return optimizePostResponsePage(posts, currentUserId);
    }

    /**
     * 获取待审核的帖子（管理员功能）
     */
    public Page<PostResponse> getPendingAuditPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findPendingAuditPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }

    /**
     * 获取需要人工审核的帖子（管理员功能）
     */
    public Page<PostResponse> getNeedAdminCheckPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findNeedAdminCheckPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }

    /**
     * 获取已拒绝的帖子（管理员功能）
     */
    public Page<PostResponse> getInvalidPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findInvalidPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }

    /**
     * 获取已删除的帖子（管理员功能）
     */
    public Page<PostResponse> getDeletedPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findDeletedPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void approvePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setState(PostState.VALID);
        // 确保审核通过的帖子reportCount为0    post.setReportCount(0);
        postRepository.save(post);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void rejectPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setState(PostState.INVALID);
        post.setIsDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(null); // 管理员删除
        post.setDeleteReason("管理员拒绝");
        // 确保审核删除的帖子reportCount为0，以区分被举报删除的帖子
        post.setReportCount(0);
        postRepository.save(post);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void deletePostByAdmin(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 软删除帖子
        post.setIsDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(null); // 管理员删除
        post.setDeleteReason("管理员删除");
        
        // 更新所有相关举报记录的状态为VALID
        List<Report> reports = reportRepository.findByPostOrderByCreatedAtDesc(post);
        for (Report report : reports) {
            report.setState(Report.State.VALID);
        }
        reportRepository.saveAll(reports);
        
        postRepository.save(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void changePostStatus(Long postId, String status) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 根据状态参数改变帖子状态
        if ("VALID".equals(status)) {
            post.setState(PostState.VALID);
            // 如果帖子被软删除，恢复它
            if (post.getIsDeleted()) {
                post.setIsDeleted(false);
                post.setDeletedAt(null);
                post.setDeletedBy(null);
                post.setDeleteReason(null);
            }
        } else if ("INVALID".equals(status)) {
            post.setState(PostState.INVALID);
            // 软删除帖子
            post.setIsDeleted(true);
            post.setDeletedAt(LocalDateTime.now());
            post.setDeletedBy(null); // 管理员操作
            post.setDeleteReason("管理员驳回举报");
        } else {
            throw new IllegalArgumentException("无效的状态值: " + status);
        }
        
        // 更新所有相关举报记录的状态
        List<Report> reports = reportRepository.findByPostOrderByCreatedAtDesc(post);
        for (Report report : reports) {
            if ("VALID".equals(status)) {
                report.setState(Report.State.INVALID); // 驳回举报
            } else if ("INVALID".equals(status)) {
                report.setState(Report.State.VALID); // 通过举报
            }
        }
        reportRepository.saveAll(reports);
        
        postRepository.save(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void approvePostReport(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 软删除帖子
        post.setIsDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(null); // 管理员删除
        post.setDeleteReason("管理员通过举报删除");
        
        // 更新所有相关举报记录的状态为VALID
        List<Report> reports = reportRepository.findByPostOrderByCreatedAtDesc(post);
        for (Report report : reports) {
            report.setState(Report.State.VALID);
        }
        reportRepository.saveAll(reports);
        
        postRepository.save(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public PostResponse createPostForUser(PostRequest postRequest, Long currentUserId) {
        if (Boolean.TRUE.equals(postRequest.getIsAnnouncement())) {
            User user = userService.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
            if (!user.isAdmin()) {
                throw new AccessDeniedException("只有管理员可以发布公告帖");
            }
        }
        System.out.println("=== 开始创建Post ===");
        System.out.println("当前线程: " + Thread.currentThread().getName());
        
        User user = userService.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setIsAnnouncement(postRequest.getIsAnnouncement() != null ? postRequest.getIsAnnouncement() : false);
        
        // 公告帖子不需要心情和标签
        if (!post.getIsAnnouncement()) {
            post.setMood(postRequest.getMood());
            if (postRequest.getTagIds() != null && !postRequest.getTagIds().isEmpty()) {
                post.setTags(postRequest.getTagIds().stream()
                    .map(tagService::findById)
                    .collect(Collectors.toList()));
            } else {
                post.setTags(new ArrayList<>());
            }

        } else {
            // 公告帖子设置为null
            post.setMood(null);
            post.setTags(new ArrayList<>());
        }
        
        post.setImageUrls(postRequest.getImageUrls());
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());
        
        // 设置初始状态为待审核
        post.setState(PostState.PENDING);
        
        Post savedPost = postRepository.save(post);

        // 异步处理向量存储和心情评分（非公告帖子）
        if (!savedPost.getIsAnnouncement()) {
            try {
                // 通过ApplicationContext调用确保AOP代理生效
                PostServiceImpl self = applicationContext.getBean(PostServiceImpl.class);
                
                // 分别异步处理向量存储和心情评分
                self.processPostVectorAsync(savedPost);
                self.processMoodScoreAsync(savedPost.getId(), currentUserId, postRequest.getContent());
                
            } catch (Exception e) {
                // 记录错误但不影响post的创建
                System.err.println("启动异步向量存储和心情评分失败: " + e.getMessage());
            }
        }

        // 异步处理内容审核 - 通过ApplicationContext调用确保AOP代理生效
        System.out.println("=== 开始异步内容审核 ===");
        PostServiceImpl self = applicationContext.getBean(PostServiceImpl.class);
        self.processPostAuditAsync(savedPost);
        System.out.println("=== 异步审核已启动，主线程继续执行 ===");

        System.out.println("=== Post创建完成（等待审核） ===");
        // 返回待审核状态的帖子响应
        return postConverter.toResponse(savedPost, false);
    }

    /**
     * 异步处理向量存储和心情评分
     */
    @Async("aiAuditExecutor")
    public void processPostVectorAndMoodScoreAsync(Post post, Long currentUserId, String content) {
        System.out.println("=== 异步线程开始处理向量存储和心情评分 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("postId: " + post.getId() + ", authorId: " + post.getAuthor().getId());
        
        long asyncStartTime = System.currentTimeMillis();
        
        try {
            // 异步存储向量
            System.out.println("=== 开始异步向量存储 ===");
            long vectorStartTime = System.currentTimeMillis();
            qdrantService.upsertPostVector(post);
            long vectorEndTime = System.currentTimeMillis();
            System.out.println("=== 向量存储完成，耗时: " + (vectorEndTime - vectorStartTime) + "ms ===");

            // 异步创建心情评分
            System.out.println("=== 开始异步心情评分 ===");
            long moodStartTime = System.currentTimeMillis();
            moodScoreService.createMoodScoreForPost(post.getId(), currentUserId, content);
            long moodEndTime = System.currentTimeMillis();
            System.out.println("=== 心情评分完成，耗时: " + (moodEndTime - moodStartTime) + "ms ===");

            long asyncEndTime = System.currentTimeMillis();
            System.out.println("=== 异步向量存储和心情评分完成，总耗时: " + (asyncEndTime - asyncStartTime) + "ms ===");

        } catch (Exception e) {
            System.err.println("异步处理向量存储和心情评分时发生异常: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 异步处理向量存储
     */
    @Async("vectorExecutor")
    public void processPostVectorAsync(Post post) {
        System.out.println("=== 异步线程开始处理向量存储 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("postId: " + post.getId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            qdrantService.upsertPostVector(post);
            long endTime = System.currentTimeMillis();
            System.out.println("=== 向量存储完成，耗时: " + (endTime - startTime) + "ms ===");
        } catch (Exception e) {
            System.err.println("异步向量存储失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 异步处理心情评分
     */
    @Async("moodScoreExecutor")
    public void processMoodScoreAsync(Long postId, Long currentUserId, String content) {
        System.out.println("=== 异步线程开始处理心情评分 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("postId: " + postId + ", userId: " + currentUserId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            moodScoreService.createMoodScoreForPost(postId, currentUserId, content);
            long endTime = System.currentTimeMillis();
            System.out.println("=== 心情评分完成，耗时: " + (endTime - startTime) + "ms ===");
        } catch (Exception e) {
            System.err.println("异步心情评分失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 异步处理Post内容审核
     */
    @Async("aiAuditExecutor")
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void processPostAuditAsync(Post post) {
        System.out.println("=== 异步线程开始处理内容审核 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("postId: " + post.getId() + ", authorId: " + post.getAuthor().getId());
        
        long asyncStartTime = System.currentTimeMillis();
        
        try {
            System.out.println("开始异步处理Post内容审核，postId : "+post.getId());

            System.out.println("=== 开始调用AI审核服务 ===");
            long aiStartTime = System.currentTimeMillis();
            
            // 执行AI审核
            PostAuditResponse auditResponse = postAuditService.auditPostContent(post);
            
            long aiEndTime = System.currentTimeMillis();
            System.out.println("=== AI审核完成，耗时: " + (aiEndTime - aiStartTime) + "ms ===");

            // 根据审核结果更新帖子状态
            if (auditResponse.getIsCompliant()) {
                System.out.println("AI审核认为内容合规: "+auditResponse.getAuditReason());
                // 设置为已通过状态
                post.setState(PostState.VALID);
                postRepository.save(post);
                System.out.println("帖子状态已更新为：已通过");
            } else {
                System.out.println("AI审核认为内容不合规: "+auditResponse.getAuditReason());
                if (auditResponse.getNeedAdminCheck()) {
                    System.out.println("需要人工审核，设置为等待人工审核状态");
                    post.setState(PostState.WAITING);
                    // 确保审核WAITING状态的帖子reportCount为0
                    post.setReportCount(0);
                    postRepository.save(post);
                } else {
                    System.out.println("内容严重违规，设置为已拒绝状态并软删除");
                    post.setState(PostState.INVALID);
                    post.setIsDeleted(true);
                    post.setDeletedAt(LocalDateTime.now());
                    post.setDeletedBy(null); // AI删除，没有具体用户
                    post.setDeleteReason("AI审核拒绝：" + auditResponse.getAuditReason());
                    // 确保审核删除的帖子reportCount为0，以区分被举报删除的帖子
                    post.setReportCount(0);
                    postRepository.save(post);
                }
            }

            long asyncEndTime = System.currentTimeMillis();
            System.out.println("=== 异步审核完成，总耗时: " + (asyncEndTime - asyncStartTime) + "ms ===");

        } catch (Exception e) {
            System.err.println("异步处理Post内容审核时发生异常: "+e.getMessage());
            e.printStackTrace();
            // 发生异常时，设置为等待人工审核状态
            try {
                post.setState(PostState.WAITING);
                // 确保审核WAITING状态的帖子reportCount为0
                post.setReportCount(0);
                postRepository.save(post);
                System.out.println("异常处理完成，帖子状态已设置为：等待人工审核");
            } catch (Exception saveException) {
                System.err.println("保存帖子状态时发生异常: "+saveException.getMessage());
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!canUserDeletePost(postId, currentUserId)) {
            throw new AccessDeniedException("只有作者可以删除帖子");
        }
        
        // 软删除帖子
        post.setIsDeleted(true);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUserId);
        post.setDeleteReason("用户删除");
        
        // 删除帖子的图片
        post.getImageUrls().stream().forEach(fileUploadService::deletePostImage);
        
        // 保存软删除的帖子
        postRepository.save(post);
        
        // 从向量数据库中删除帖子向量
        qdrantService.deletePostVector(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void likePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userService.findById(userId);
        if (postOpt.isEmpty()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // 检查帖子是否已被软删除
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 检查帖子状态是否为VALID（只有已通过的帖子才能被点赞）
        if (post.getState() != PostState.VALID) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        if (!postLikeRepository.existsByPostAndUser(post, user)) {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
            postRepository.updateLikeCount(postId, 1);
            notificationService.createAndSendPostLikeNotification(post, user);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void unlikePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userService.findById(userId);
        if (postOpt.isEmpty()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        Post post = postOpt.get();
        User user = userOpt.get();
        
        // 检查帖子是否已被软删除
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 检查帖子状态是否为VALID（只有已通过的帖子才能被取消点赞）
        if (post.getState() != PostState.VALID) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            postLikeRepository.deleteByPostAndUser(post, user);
            postRepository.updateLikeCount(postId, -1);
        }
    }



    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userService.findById(userId);  
        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();
            
            // 检查帖子是否已被软删除
            if (post.getIsDeleted()) {
                return false; // 已删除的帖子无法被点赞
            }
            
            // 检查帖子状态是否为VALID（只有已通过的帖子才能被点赞）
            if (post.getState() != PostState.VALID) {
                return false; // 非VALID状态的帖子无法被点赞
            }
            
            return postLikeRepository.existsByPostAndUser(post, user);
        }
        return false;
    }

    @Override
    public boolean canUserDeletePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userService.findById(userId);
        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            
            // 检查帖子是否已被软删除
            if (post.getIsDeleted()) {
                return false; // 已删除的帖子无法再次删除
            }
            
            // 检查帖子状态是否为VALID（只有已通过的帖子才能被删除）
            if (post.getState() != PostState.VALID) {
                return false; // 非VALID状态的帖子无法被删除
            }
            
            return post.getAuthor().getId().equals(userId);
        }
        return false;
    }

    // 举报相关方法实现
    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void reportPost(Long postId, Long currentUserId, String reason) {
        System.out.println("=== 开始处理举报请求 ===");
        System.out.println("postId: " + postId + ", currentUserId: " + currentUserId + ", reason: " + reason);
        System.out.println("当前线程: " + Thread.currentThread().getName());
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        User user = userService.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        
        // 检查帖子是否已被软删除
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 检查帖子状态是否为VALID（只有已通过的帖子才能被举报）
        if (post.getState() != PostState.VALID) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 检查是否是自己的帖子
        if (post.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("不能举报自己的帖子");
        }
        
        // 检查是否已经举报过
        if (reportRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("您已经举报过这个帖子");
        }
        
        System.out.println("=== 开始同步处理举报记录 ===");
        long syncStartTime = System.currentTimeMillis();
        
        // 创建举报记录
        Report report = new Report();
        report.setPost(post);
        report.setReason(reason);
        report.setUser(user);
        report = reportRepository.save(report);
        
        // 增加帖子的举报次数
        post.setReportCount(post.getReportCount() + 1);
        postRepository.save(post);
        
        // 增加被举报用户的举报次数（持久化存储）
        userService.incrementReportedCount(post.getAuthor().getId());

        long syncEndTime = System.currentTimeMillis();
        System.out.println("=== 同步处理完成，耗时: " + (syncEndTime - syncStartTime) + "ms ===");

        // 异步处理AI审核 - 通过ApplicationContext调用确保AOP代理生效
        System.out.println("=== 开始异步处理AI审核 ===");
        PostServiceImpl self = applicationContext.getBean(PostServiceImpl.class);
        self.processPostReportAuditAsync(report, post, reason);
        System.out.println("=== 异步处理已启动，主线程继续执行 ===");
    }

    /**
     * 异步处理Post举报并自动审核
     */
    @Async("aiAuditExecutor")
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void processPostReportAuditAsync(Report report, Post post, String reason) {
        System.out.println("=== 异步线程开始处理AI审核 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("postId: " + post.getId() + ", userId: " + report.getUser().getId());
        
        long asyncStartTime = System.currentTimeMillis();
        
        try {
            System.out.println("开始异步处理Post举报并自动审核，postId : "+post.getId()+ " userId :" +report.getUser().getId());

            // 构建审核请求
            PostReportAuditRequest auditRequest = PostReportAuditRequest.builder()
                    .reportReason(reason)
                    .postTitle(post.getTitle())
                    .postContent(post.getContent())
                    .build();

            System.out.println("=== 开始调用AI审核服务 ===");
            long aiStartTime = System.currentTimeMillis();
            
            // 执行AI审核
            PostReportAuditResponse auditResponse = postReportAuditService.auditPostReport(auditRequest);

            long aiEndTime = System.currentTimeMillis();
            System.out.println("=== AI审核完成，耗时: " + (aiEndTime - aiStartTime) + "ms ===");

            // 根据审核结果统一处理举报和帖子状态
            if (auditResponse.getNeedAdminCheck()) {
                System.out.println("AI审核认为需要人工审核: "+auditResponse.getAuditReason());
                // 举报和帖子都设置为等待人工审核状态，保持reportCount >0
                report.setState(Report.State.WAITING);
                post.setState(PostState.WAITING);
                // 保持reportCount > 0，表示这是被举报的帖子
                reportRepository.save(report);
                postRepository.save(post);
            } else {
                if (auditResponse.getIsValidReport()) {
                    System.out.println("AI审核认为举报有效，帖子将被标记为无效: "+auditResponse.getAuditReason());
                    // 举报有效，帖子变为无效（保持reportCount > 0，表示被举报删除）
                    report.setState(Report.State.VALID);
                    post.setState(PostState.INVALID);
                    post.setIsDeleted(true);
                    post.setDeletedAt(LocalDateTime.now());
                    post.setDeletedBy(null); // AI删除
                    post.setDeleteReason("AI审核举报有效：" + auditResponse.getAuditReason());
                    // 保持reportCount > 0，表示被举报删除
                    reportRepository.save(report);
                    postRepository.save(post);
                } else {
                    System.out.println("AI审核认为举报无效，帖子保持有效: "+auditResponse.getAuditReason());
                    // 举报无效，帖子保持有效，保持reportCount > 0
                    report.setState(Report.State.INVALID);
                    // 帖子状态保持VALID不变，reportCount > 0表示被举报过
                    reportRepository.save(report);
                }
            }

            long asyncEndTime = System.currentTimeMillis();
            System.out.println("=== 异步处理完成，总耗时: " + (asyncEndTime - asyncStartTime) + "ms ===");

        } catch (Exception e) {
            System.err.println("异步处理Post举报并自动审核时发生异常: "+e.getMessage());
            e.printStackTrace();
            // 发生异常时，将举报和帖子状态都设置为等待人工审核
            try {
                report.setState(Report.State.WAITING);
                post.setState(PostState.WAITING);
                // 保持reportCount > 0，表示这是被举报的帖子
                reportRepository.save(report);
                postRepository.save(post);
                System.out.println("异常处理完成，举报和帖子状态已设置为等待人工审核");
            } catch (Exception saveException) {
                System.err.println("保存状态时发生异常: "+saveException.getMessage());
            }
        }
    }

    @Override
    public Page<ReportedPostResponse> getReportedPosts(Pageable pageable) {
        // 只返回被举报的帖子（举报次数大于0且未删除）
        return postRepository.findByIsDeletedFalseAndReportCountGreaterThanOrderByReportCountDesc(0, pageable)
            .map(this::convertToReportedPostResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void ignorePostReports(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 检查帖子是否已被软删除
        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        // 删除所有举报记录
        reportRepository.deleteByPostId(postId);
        
        // 重置帖子的举报次数为0
        post.setReportCount(0);
        postRepository.save(post);
    }

    @Override
    public Integer getTotalReportedPostsCount() {
        return postRepository.countByIsDeletedFalseAndReportCountGreaterThan(0);
    }
    
    // ================== 区分删除原因的查询方法 ==================
    
    @Override
    public Page<PostResponse> getReportedDeletedPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findReportedDeletedPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }
    
    @Override
    public Page<PostResponse> getAuditDeletedPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAuditDeletedPosts(pageable);
        return postsPage.map(post -> postConverter.toResponse(post, false));
    }
    
    @Override
    public Integer getReportedDeletedPostsCount() {
        return postRepository.countReportedDeletedPosts();
    }
    
    @Override
    public Integer getAuditDeletedPostsCount() {
        return postRepository.countAuditDeletedPosts();
    }
    
    // ================== 新增软删除相关方法 ==================
    
    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void restorePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // 检查帖子状态是否为INVALID
        if (post.getState() != PostState.INVALID) {
            throw new RuntimeException("只有INVALID状态的帖子才能被恢复");
        }
        
        // 从INVALID状态恢复为VALID状态
        post.setState(PostState.VALID);
        
        // 如果帖子被软删除了，也要恢复软删除状态
        if (post.getIsDeleted()) {
        post.setIsDeleted(false);
        post.setDeletedAt(null);
        post.setDeletedBy(null);
        post.setDeleteReason(null);
        }
        
        postRepository.save(post);
    }
    
    @Override
    public Page<PostResponse> getDeletedPostsByUser(Long userId, Pageable pageable) {
        // 先获取所有已删除的帖子，然后按用户过滤
        Page<Post> allDeletedPosts = postRepository.findDeletedPosts(pageable);
        List<Post> userDeletedPosts = allDeletedPosts.getContent().stream()
            .filter(post -> userId.equals(post.getAuthor().getId()))
            .collect(Collectors.toList());
        
        Page<Post> posts = new org.springframework.data.domain.PageImpl<>(userDeletedPosts, pageable, userDeletedPosts.size());
        return posts.map(post -> postConverter.toResponse(post, false));
    }

    // 私有方法：优化PostResponse分页查询，避免N+1查询问题
    private Page<PostResponse> optimizePostResponsePage(Page<Post> postsPage, Long currentUserId) {
        List<Post> posts = postsPage.getContent();
        
        // 批量查询用户信息，避免N+1查询
        optimizeUserInfoLoading(posts);
        
        // 批量查询点赞状态
        Map<Long, Boolean> likeStatusMap = batchQueryLikeStatus(posts, currentUserId);
        
        return postsPage.map(post -> postConverter.toResponse(post, 
            likeStatusMap.getOrDefault(post.getId(), false)));
    }
    
    /**
     * 优化用户信息加载，避免N+1查询问题
     * 使用批量查询并手动组装的方式
     */
    private void optimizeUserInfoLoading(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        
        // 1. 收集所有作者ID
        Set<Long> authorIds = posts.stream()
            .map(post -> post.getAuthor().getId())
            .collect(Collectors.toSet());
        
        // 2. 一次性查询所有用户
        Map<Long, User> userMap = userService.findByIds(authorIds)
            .stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // 3. 手动组装，将完整的User对象设置到Post中
        for (Post post : posts) {
            User author = post.getAuthor();
            if (author != null && author.getId() != null) {
                User completeUser = userMap.get(author.getId());
                if (completeUser != null) {
                    // 直接设置完整的用户对象，避免后续的惰性加载
                    post.setAuthor(completeUser);
                }
            }
        }
    }
    
    /**
     * 批量查询点赞状态，避免N+1查询
     */
    private Map<Long, Boolean> batchQueryLikeStatus(List<Post> posts, Long currentUserId) {
        Map<Long, Boolean> likeStatusMap = new HashMap<>();
        
        if (currentUserId != null && !posts.isEmpty()) {
            List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
            List<PostLike> userLikes = postLikeRepository.findByUserIdAndPostIdIn(currentUserId, postIds);
            Set<Long> likedPostIds = userLikes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());
            
            for (Long postId : postIds) {
                likeStatusMap.put(postId, likedPostIds.contains(postId));
            }
        }
        
        return likeStatusMap;
    }

    // 私有方法：转换为ReportedPostResponse
    private ReportedPostResponse convertToReportedPostResponse(Post post) {
        ReportedPostResponse response = new ReportedPostResponse();
        
        // 设置帖子基本信息（精简版）
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setMood(post.getMood());
        response.setCreatedAt(post.getCreatedAt());
        response.setState(post.getState());
        
        // 设置作者信息（精简版）
        response.setAuthorName(post.getAuthor().getUsername());
        response.setAuthorId(post.getAuthor().getId());
        
        // 设置举报核心信息
        response.setReportCount(post.getReportCount());
        
        // 获取该帖子的所有举报记录
        List<Report> reports = reportRepository.findByPostOrderByCreatedAtDesc(post);
        
        if (!reports.isEmpty()) {
            // 设置最近一次举报时间
            response.setLastReportTime(reports.get(0).getCreatedAt());
            // 收集所有举报原因
            List<String> reasons = reports.stream()
                .map(Report::getReason)
                .distinct() // 去重
                .collect(Collectors.toList());
            response.setReportReasons(reasons);
        }
        
        return response;
    }
}