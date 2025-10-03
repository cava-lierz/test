package com.mentara.service.impl;

import com.mentara.dto.request.CommentRequest;
import com.mentara.dto.response.CommentResponse;
import com.mentara.entity.Comment;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.exception.UnauthorizedException;
import com.mentara.exception.ResourceNotFoundException;
import com.mentara.repository.CommentRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.UserRepository;
import com.mentara.repository.CommentLikeRepository;
import com.mentara.repository.CommentReportRepository;
import com.mentara.converter.CommentConverter;
import com.mentara.service.CommentService;
import com.mentara.service.NotificationService;
import com.mentara.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.mentara.entity.CommentLike;
import com.mentara.entity.CommentReport;
import com.mentara.converter.NotificationResponseFactory;
import com.mentara.dto.response.ReportedCommentResponse;
import com.mentara.dto.request.CommentReportAuditRequest;
import com.mentara.dto.response.CommentReportAuditResponse;
import com.mentara.service.CommentReportAuditService;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentConverter commentConverter;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentReportRepository commentReportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentReportAuditService commentReportAuditService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public CommentResponse createComment(CommentRequest commentRequest, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Comment comment = new Comment();
        Comment savedComment = null;
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);

        if (commentRequest.getParentId() == null) {
            // It's a top-level comment
            Post post = postRepository.findById(commentRequest.getPostId())
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", commentRequest.getPostId()));
            comment.setPost(post);
            savedComment = commentRepository.save(comment);
            notificationService.createAndSendPostReplyNotification(post, savedComment, author);
        } else if (commentRequest.getTopCommentId() != null) {
            // It's a reply to the top comment or to a reply
            Comment parentComment = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Comment", "id", commentRequest.getParentId()));
            Comment topComment = commentRepository.findById(commentRequest.getTopCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Top Comment", "id", commentRequest.getTopCommentId()));
            
            // 检查父评论和顶级评论是否已被软删除
            if (parentComment.getIsDeleted()) {
                throw new RuntimeException("无法回复已删除的评论");
            }
            if (topComment.getIsDeleted()) {
                throw new RuntimeException("无法回复已删除的评论");
            }
            
            comment.setParent(parentComment);
            comment.setTopComment(topComment);
            comment.setPost(parentComment.getPost());
            commentRepository.updateReplyCount(commentRequest.getTopCommentId(), 1);
            savedComment = commentRepository.save(comment);
            notificationService.createAndSendCommentReplyNotification(parentComment, savedComment, author);
        } else {
            throw new IllegalArgumentException("Comment request is invalid.");
        }

        postRepository.updateCommentCount(commentRequest.getPostId(), 1);
        return commentConverter.toResponse(
                savedComment, isCommentLikedByUser(comment.getId(), authorId), authorId);
    }

    @Override
    public Page<CommentResponse> getCommentsOfPost(Long postId, Pageable pageable, Long currentUserId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        // 使用软删除查询方法，只查询未删除的评论
        Page<Comment> topLevelComments = commentRepository.findByPostIdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(postId, pageable);
        return optimizeCommentResponsePage(topLevelComments, currentUserId);
    }

    @Override
    public Page<CommentResponse> getRepliesOfComment(Long commentId, Pageable pageable, Long currentUserId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        // 使用软删除查询方法，只查询未删除的回复
        Page<Comment> replies = commentRepository.findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(commentId, pageable);
        return optimizeCommentResponsePage(replies, currentUserId);
    }

    @Override
    public CommentResponse getComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        
        return commentConverter.toResponse(comment, isCommentLikedByUser(commentId, currentUserId), currentUserId);
    }

    @Override
    public Page<CommentResponse> getRepliesOfTopComment(Long commentId, Pageable pageable, Long currentUserId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        // 使用软删除查询方法，只查询未删除的回复
        Page<Comment> replies = commentRepository.findByTopCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(commentId, pageable);
        return optimizeCommentResponsePage(replies, currentUserId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void likeComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (commentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        Comment comment = commentOpt.get();
        User user = userOpt.get();
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        
        if (!commentLikeRepository.existsByCommentAndUser(comment, user)) {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            commentRepository.updateLikeCount(commentId, 1);
            notificationService.createAndSendCommentLikeNotification(comment, user);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void unlikeComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (commentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        Comment comment = commentOpt.get();
        User user = userOpt.get();
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        
        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            commentLikeRepository.deleteByCommentAndUser(comment, user);
            commentRepository.updateLikeCount(commentId, -1);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        System.out.println("开始删除评论: commentId=" + commentId + ", userId=" + userId);
        
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (commentOpt.isEmpty()) {
            System.out.println("评论不存在: commentId=" + commentId);
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        if (userOpt.isEmpty()) {
            System.out.println("用户不存在: userId=" + userId);
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        Comment comment = commentOpt.get();
        User user = userOpt.get();
        
        System.out.println("找到评论: commentId=" + commentId + ", authorId=" + comment.getAuthor().getId());
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            System.out.println("评论已被删除: commentId=" + commentId);
            throw new RuntimeException("评论已被删除，无需重复删除");
        }
        
        // 直接验证权限，避免重复查询
        if (!comment.getAuthor().getId().equals(userId)) {
            System.out.println("权限验证失败: commentAuthorId=" + comment.getAuthor().getId() + ", currentUserId=" + userId);
            throw new UnauthorizedException("你不能删除该评论.");
        }
        
        System.out.println("权限验证通过，开始软删除评论");
        
        // 更新评论数量（在软删除前）
        updateCommentCountsOnSoftDelete(comment);
        
        // 软删除评论
        comment.setIsDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(userId);
        comment.setDeleteReason("用户删除");
        
        commentRepository.save(comment);
        
        System.out.println("评论软删除完成: commentId=" + commentId);
    }
    
    @Override
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (commentOpt.isPresent() && userOpt.isPresent()) {
            Comment comment = commentOpt.get();
            User user = userOpt.get();
            
            // 检查评论是否已被软删除
            if (comment.getIsDeleted()) {
                return false; // 已删除的评论无法被点赞
            }
            
            return commentLikeRepository.existsByCommentAndUser(comment, user);
        }
        return false;
    }

    @Override
    public boolean canUserDeleteComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (commentOpt.isPresent() && userOpt.isPresent()) {
            Comment comment = commentOpt.get();
            
            // 检查评论是否已被软删除
            if (comment.getIsDeleted()) {
                return false; // 已删除的评论无法再次删除
            }
            
            return comment.getAuthor().getId().equals(userId);
        }
        return false;
    }

    @Override
    public int calculateLastPageOfPost(Long postId, int size) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        // 使用软删除统计方法，只统计未删除的评论
        int totalTopLevelComments = commentRepository.countByPostIdAndParentIsNullAndIsDeletedFalse(postId);
        int LastPage = (totalTopLevelComments - 1) / size;
        return LastPage;
    }

    @Override
    public int calculateLastPageOfTopComment(Long commentId, int size) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        // 使用软删除统计方法，只统计未删除的回复
        int totalReplies = commentRepository.countByTopCommentIdAndIsDeletedFalse(commentId);
        int LastPage = (totalReplies - 1) / size;
        return LastPage;
    }

    @Override
    public int calculatePageOfReplyInTopComment(Long parentId, int size) {
        Comment comment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentId));
        
        // 使用软删除统计方法，只统计未删除的回复
        int totalRepliesBefore = commentRepository.countRepliesBeforeInTopCommentAndIsDeletedFalse(comment.getTopComment().getId(), comment.getCreatedAt());
        int page = totalRepliesBefore / size;
        return page;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void reportComment(Long commentId, Long currentUserId, String reason) {
        System.out.println("=== 开始处理评论举报请求 ===");
        System.out.println("commentId: " + commentId + ", currentUserId: " + currentUserId + ", reason: " + reason);
        System.out.println("当前线程: " + Thread.currentThread().getName());
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        User user = userService.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        
        // 检查是否是自己的评论
        if (comment.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("不能举报自己的评论");
        }
        
        // 检查是否是管理员的评论
        if (comment.getAuthor().getRole().name().equals("ADMIN")) {
            throw new RuntimeException("管理员的评论不可被举报");
        }
        
        // 检查是否已经举报过
        if (commentReportRepository.existsByUserAndComment(user, comment)) {
            throw new RuntimeException("您已经举报过这个评论");
        }
        
        System.out.println("=== 开始同步处理评论举报记录 ===");
        long syncStartTime = System.currentTimeMillis();
        
        // 创建举报记录
        CommentReport report = new CommentReport(user, comment, reason);
        report = commentReportRepository.save(report);
        
        // 增加评论的举报次数
        Integer currentReportCount = comment.getReportCount();
        if (currentReportCount == null) {
            currentReportCount = 0;
        }
        comment.setReportCount(currentReportCount + 1);
        commentRepository.save(comment);
        
        // 增加被举报用户的举报次数（持久化存储）
        userService.incrementReportedCount(comment.getAuthor().getId());

        long syncEndTime = System.currentTimeMillis();
        System.out.println("=== 同步处理完成，耗时: " + (syncEndTime - syncStartTime) + "ms ===");

        // 异步处理AI审核 - 通过ApplicationContext调用确保AOP代理生效
        System.out.println("=== 开始异步处理AI审核 ===");
        CommentServiceImpl self = applicationContext.getBean(CommentServiceImpl.class);
        self.processCommentReportAuditAsync(report, comment, reason);
        System.out.println("=== 异步处理已启动，主线程继续执行 ===");
    }

    @Override
    public Page<ReportedCommentResponse> getReportedComments(Pageable pageable) {
        // 只返回被举报且未删除的评论（举报次数大于0）
        return commentRepository.findByIsDeletedFalseAndReportCountGreaterThanOrderByReportCountDesc(pageable)
            .map(this::convertToReportedCommentResponse);
    }

    @Override
    public Page<ReportedCommentResponse> getWaitingCommentReports(Pageable pageable) {
        // 返回等待人工审核的评论举报
        return commentReportRepository.findByStateOrderByCreatedAtDesc(CommentReport.State.WAITING, pageable)
            .map(report -> convertToReportedCommentResponse(report.getComment()));
    }

    @Override
    public Page<ReportedCommentResponse> getValidCommentReports(Pageable pageable) {
        // 返回举报有效的评论（已被删除的评论）
        return commentRepository.findByIsDeletedTrueAndReportCountGreaterThanOrderByDeletedAtDesc(pageable)
            .map(this::convertToReportedCommentResponse);
    }

    @Override
    public Page<ReportedCommentResponse> getInvalidCommentReports(Pageable pageable) {
        // 返回举报无效的评论（举报被驳回，评论保持有效）
        return commentReportRepository.findByStateOrderByCreatedAtDesc(CommentReport.State.INVALID, pageable)
            .map(report -> convertToReportedCommentResponse(report.getComment()));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new RuntimeException("评论已被删除，无需重复删除");
        }
        
        // 删除所有举报记录（处理举报），但不重置评论的举报次数
        // 被举报的历史事实保持不变
        commentReportRepository.deleteByCommentId(commentId);
        
        // 更新评论数量（在软删除前）
        updateCommentCountsOnSoftDelete(comment);
        
        // 软删除评论
        comment.setIsDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(null); // 管理员删除
        comment.setDeleteReason("管理员删除");
        
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void forceDeleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new RuntimeException("评论已被删除，无需重复删除");
        }
        
        // 更新评论数量（在软删除前）
        updateCommentCountsOnSoftDelete(comment);
        
        // 管理员强制删除评论：软删除
        comment.setIsDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(null);
        comment.setDeleteReason("管理员强制删除");
        
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void approveCommentReport(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new RuntimeException("评论已被删除，无需重复处理");
        }
        
        // 更新评论数量（在软删除前）
        updateCommentCountsOnSoftDelete(comment);
        
        // 软删除评论
        comment.setIsDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(null); // 管理员删除
        comment.setDeleteReason("管理员通过举报删除");
        
        // 更新所有相关举报记录的状态为VALID
        List<CommentReport> reports = commentReportRepository.findByCommentOrderByCreatedAtDesc(comment);
        for (CommentReport report : reports) {
            report.setState(CommentReport.State.VALID);
        }
        commentReportRepository.saveAll(reports);
        
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void ignoreCommentReports(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // 检查评论是否已被软删除
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        
        // 更新所有相关举报记录的状态为INVALID
        List<CommentReport> reports = commentReportRepository.findByCommentOrderByCreatedAtDesc(comment);
        for (CommentReport report : reports) {
            report.setState(CommentReport.State.INVALID);
        }
        commentReportRepository.saveAll(reports);
        
        // 重置评论的举报次数为0
        comment.setReportCount(0);
        commentRepository.save(comment);
    }

    @Override
    public Integer getTotalReportedCommentsCount() {
        return commentRepository.countByIsDeletedFalseAndReportCountGreaterThan();
    }

    /**
     * 异步处理评论举报并自动审核
     */
    @Async("aiAuditExecutor")
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void processCommentReportAuditAsync(CommentReport report, Comment comment, String reason) {
        System.out.println("=== 异步线程开始处理评论AI审核 ===");
        System.out.println("线程名称: " + Thread.currentThread().getName());
        System.out.println("commentId: " + comment.getId() + ", userId: " + report.getUser().getId());
        
        long asyncStartTime = System.currentTimeMillis();
        
        try {
            System.out.println("开始异步处理评论举报并自动审核，commentId : "+comment.getId()+ " userId :" +report.getUser().getId());

            // 构建审核请求
            CommentReportAuditRequest auditRequest = CommentReportAuditRequest.builder()
                    .reportReason(reason)
                    .commentContent(comment.getContent())
                    .authorUsername(comment.getAuthor().getUsername())
                    .postTitle(comment.getPost().getTitle())
                    .build();

            System.out.println("=== 开始调用AI审核服务 ===");
            long aiStartTime = System.currentTimeMillis();
            
            // 执行AI审核
            CommentReportAuditResponse auditResponse = commentReportAuditService.auditCommentReport(auditRequest);

            long aiEndTime = System.currentTimeMillis();
            System.out.println("=== AI审核完成，耗时: " + (aiEndTime - aiStartTime) + "ms ===");

            // 根据审核结果统一处理举报和评论状态
            if (auditResponse.getNeedAdminCheck()) {
                System.out.println("AI审核认为需要人工审核: "+auditResponse.getAuditReason());
                // 举报设置为等待人工审核状态，保持reportCount >0
                report.setState(CommentReport.State.WAITING);
                // 保持reportCount > 0，表示这是被举报的评论
                commentReportRepository.save(report);
            } else {
                if (auditResponse.getIsValidReport()) {
                    System.out.println("AI审核认为举报有效，评论将被删除: "+auditResponse.getAuditReason());
                    // 举报有效，评论被删除（保持reportCount > 0，表示被举报删除）
                    report.setState(CommentReport.State.VALID);
                    comment.setIsDeleted(true);
                    comment.setDeletedAt(LocalDateTime.now());
                    comment.setDeletedBy(null); // AI删除
                    comment.setDeleteReason("AI审核举报有效：" + auditResponse.getAuditReason());
                    // 保持reportCount > 0，表示被举报删除
                    commentReportRepository.save(report);
                    commentRepository.save(comment);
                } else {
                    System.out.println("AI审核认为举报无效，评论保持有效: "+auditResponse.getAuditReason());
                    // 举报无效，评论保持有效，保持reportCount > 0
                    report.setState(CommentReport.State.INVALID);
                    // 评论状态保持正常，reportCount > 0表示被举报过
                    commentReportRepository.save(report);
                }
            }

            long asyncEndTime = System.currentTimeMillis();
            System.out.println("=== 异步处理完成，总耗时: " + (asyncEndTime - asyncStartTime) + "ms ===");

        } catch (Exception e) {
            System.err.println("异步处理评论举报并自动审核时发生异常: "+e.getMessage());
            e.printStackTrace();
            // 发生异常时，将举报状态设置为等待人工审核
            try {
                report.setState(CommentReport.State.WAITING);
                // 保持reportCount > 0，表示这是被举报的评论
                commentReportRepository.save(report);
                System.out.println("异常处理完成，举报状态已设置为等待人工审核");
            } catch (Exception saveException) {
                System.err.println("保存状态时发生异常: "+saveException.getMessage());
            }
        }
    }
    
    // ================== 新增软删除相关方法 ==================
    
    @Override
    @Transactional
    @CacheEvict(value = {"posts", "statistics"}, allEntries = true)
    public void restoreComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        if (!comment.getIsDeleted()) {
            throw new RuntimeException("评论未被删除，无需恢复");
        }
        
        // 恢复评论
        commentRepository.restoreComment(commentId);
        
        // 更新评论数量（在恢复后）
        updateCommentCountsOnRestore(comment);
    }
    
    @Override
    public Page<CommentResponse> getDeletedComments(Pageable pageable) {
        return commentRepository.findByIsDeletedTrueOrderByDeletedAtDesc(pageable)
            .map(comment -> commentConverter.toResponse(comment, false));
    }
    
    @Override
    public Page<CommentResponse> getDeletedCommentsByUser(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorIdAndIsDeletedTrueOrderByDeletedAtDesc(userId, pageable)
            .map(comment -> commentConverter.toResponse(comment, false));
    }

    // 私有方法：软删除评论时更新评论数量
    private void updateCommentCountsOnSoftDelete(Comment comment) {
        System.out.println("开始更新软删除评论的数量: commentId=" + comment.getId());
        
        // 计算要删除的评论总数（包括当前评论及其所有回复）
        int totalCommentsToDelete = 1; // 当前评论
        
        // 如果当前评论是顶级评论，需要计算其所有回复数量
        if (comment.getTopComment() == null) {
            // 当前评论是顶级评论，计算其所有回复数量
            totalCommentsToDelete += comment.getReplysCount();
        } else {
            // 当前评论是回复，只需要计算其直接回复数量
            totalCommentsToDelete += comment.getReplysCount();
        }
        
        System.out.println("要删除的评论总数: " + totalCommentsToDelete + " (包括 " + comment.getReplysCount() + " 个回复)");
        
        // 更新父评论的回复数
        if (comment.getTopComment() != null) {
            System.out.println("更新父评论回复数: topCommentId=" + comment.getTopComment().getId());
            commentRepository.updateReplyCount(comment.getTopComment().getId(), -1);
        }
        
        // 更新帖子的评论数（减去所有被删除的评论和回复）
        System.out.println("更新帖子评论数: postId=" + comment.getPost().getId() + ", 减少数量=" + totalCommentsToDelete);
        postRepository.updateCommentCount(comment.getPost().getId(), -totalCommentsToDelete);
        
        System.out.println("软删除评论数量更新完成: commentId=" + comment.getId());
    }
    
    // 私有方法：恢复评论时更新评论数量
    private void updateCommentCountsOnRestore(Comment comment) {
        System.out.println("开始更新恢复评论的数量: commentId=" + comment.getId());
        
        // 计算要恢复的评论总数（包括当前评论及其所有回复）
        int totalCommentsToRestore = 1; // 当前评论
        
        // 如果当前评论是顶级评论，需要计算其所有回复数量
        if (comment.getTopComment() == null) {
            // 当前评论是顶级评论，计算其所有回复数量
            totalCommentsToRestore += comment.getReplysCount();
        } else {
            // 当前评论是回复，只需要计算其直接回复数量
            totalCommentsToRestore += comment.getReplysCount();
        }
        
        System.out.println("要恢复的评论总数: " + totalCommentsToRestore + " (包括 " + comment.getReplysCount() + " 个回复)");
        
        // 更新父评论的回复数
        if (comment.getTopComment() != null) {
            System.out.println("更新父评论回复数: topCommentId=" + comment.getTopComment().getId());
            commentRepository.updateReplyCount(comment.getTopComment().getId(), 1);
        }
        
        // 更新帖子的评论数（加上所有被恢复的评论和回复）
        System.out.println("更新帖子评论数: postId=" + comment.getPost().getId() + ", 增加数量=" + totalCommentsToRestore);
        postRepository.updateCommentCount(comment.getPost().getId(), totalCommentsToRestore);
        
        System.out.println("恢复评论数量更新完成: commentId=" + comment.getId());
    }

    // 私有方法：删除评论及其所有回复（硬删除，保留用于参考）
    private void deleteCommentAndReplies(Comment comment) {
        System.out.println("开始删除评论及其回复: commentId=" + comment.getId());
        
        // 计算要删除的评论总数（包括当前评论及其所有回复）
        int totalCommentsToDelete = 1; // 当前评论
        
        // 如果当前评论是顶级评论，需要计算其所有回复数量
        if (comment.getTopComment() == null) {
            // 当前评论是顶级评论，计算其所有回复数量
            totalCommentsToDelete += comment.getReplysCount();
        } else {
            // 当前评论是回复，只需要计算其直接回复数量
            totalCommentsToDelete += comment.getReplysCount();
        }
        
        System.out.println("要删除的评论总数: " + totalCommentsToDelete + " (包括 " + comment.getReplysCount() + " 个回复)");
        
        // 更新父评论的回复数
        if (comment.getTopComment() != null) {
            System.out.println("更新父评论回复数: topCommentId=" + comment.getTopComment().getId());
            commentRepository.updateReplyCount(comment.getTopComment().getId(), -1);
        }
        
        // 更新帖子的评论数（减去所有被删除的评论和回复）
        System.out.println("更新帖子评论数: postId=" + comment.getPost().getId() + ", 减少数量=" + totalCommentsToDelete);
        postRepository.updateCommentCount(comment.getPost().getId(), -totalCommentsToDelete);
        
        // 删除评论（数据库会自动级联删除所有回复、点赞记录、举报记录等）
        System.out.println("执行评论删除操作");
        commentRepository.delete(comment);
        
        System.out.println("评论删除操作完成: commentId=" + comment.getId());
    }

    // 私有方法：转换为ReportedCommentResponse
    private ReportedCommentResponse convertToReportedCommentResponse(Comment comment) {
        ReportedCommentResponse response = new ReportedCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setReportCount(comment.getReportCount() != null ? comment.getReportCount() : 0);
        response.setCreatedAt(comment.getCreatedAt());
        response.setAuthorName(comment.getAuthor().getUsername());
        response.setAuthorId(comment.getAuthor().getId());
        response.setPostId(comment.getPost().getId());
        response.setPostTitle(comment.getPost().getContent().length() > 50 ? 
            comment.getPost().getContent().substring(0, 50) + "..." : 
            comment.getPost().getContent());
        response.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        response.setTopCommentId(comment.getTopComment() != null ? comment.getTopComment().getId() : null);
        response.setRepliesCount(comment.getReplysCount());
        response.setLikesCount(comment.getLikesCount());
        return response;
    }

    // 私有方法：优化CommentResponse分页查询，避免N+1查询问题
    private Page<CommentResponse> optimizeCommentResponsePage(Page<Comment> commentsPage, Long currentUserId) {
        List<Comment> comments = commentsPage.getContent();
        Map<Long, Boolean> likeStatusMap = new HashMap<>();
        
        if (currentUserId != null && !comments.isEmpty()) {
            List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
            List<CommentLike> userLikes = commentLikeRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds);
            Set<Long> likedCommentIds = userLikes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());
            
            for (Long commentId : commentIds) {
                likeStatusMap.put(commentId, likedCommentIds.contains(commentId));
            }
        }
        
        return commentsPage.map(comment -> commentConverter.toResponse(comment, 
            likeStatusMap.getOrDefault(comment.getId(), false), currentUserId));
    }
} 