package com.mentara.converter;

import com.mentara.dto.response.CommentResponse;
import com.mentara.entity.Comment;
import com.mentara.entity.User;
import com.mentara.util.AvatarUtils;
import com.mentara.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class CommentConverter {

    @Autowired
    private OssService ossService;


    public CommentResponse toResponse(Comment comment, boolean isLiked) {
        return toResponse(comment, isLiked, null);
    }

    public CommentResponse toResponse(Comment comment, boolean isLiked, Long currentUserId) {
        User author = comment.getAuthor();
        
        // 处理已删除用户的情况
        String authorNickname = author.getNickname();
        String authorAvatar = author.getAvatar();
        
        // 如果用户已删除，使用默认值
        if (author.getIsDeleted() != null && author.getIsDeleted()) {
            authorNickname = "已删除用户";
            authorAvatar = AvatarUtils.getDeletedUserAvatar();
        }
        // 如果是object key，转换为临时URL
        if (authorAvatar != null) {
            authorAvatar = ossService.generatePresignedUrl(authorAvatar, 3600L);
        }
        
        // 处理软删除评论的情况
        String content = comment.getContent();
        if (comment.getIsDeleted() != null && comment.getIsDeleted()) {
            content = "此评论已被删除";
            // 已删除的评论无法被点赞或删除
            isLiked = false;
            authorAvatar = AvatarUtils.getDeletedUserAvatar();
        }
        
        // 判断用户是否可以删除此评论
        boolean canDelete = false;
        if (currentUserId != null && (comment.getIsDeleted() == null || !comment.getIsDeleted())) {
            // 用户可以删除自己的评论，但已删除的评论无法再次删除
            canDelete = author.getId().equals(currentUserId);
        }
        
        return CommentResponse.builder()
                .id(comment.getId())
                .content(content)
                .authorId(author.getId())
                .authorNickname(authorNickname)
                .authorAvatar(authorAvatar)
                .authorRole(author.getRole().name())
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .topCommentId(comment.getTopComment() != null ? comment.getTopComment().getId() : null)
                .repliesCount(comment.getReplysCount())
                .likesCount(comment.getLikesCount())
                .reportCount(comment.getReportCount() != null ? comment.getReportCount() : 0)
                .isLiked(isLiked)
                .canDelete(canDelete)
                .isDeleted(comment.getIsDeleted())
                .deletedAt(comment.getDeletedAt())
                .deletedBy(comment.getDeletedBy())
                .deleteReason(comment.getDeleteReason())
                .build();
    }
}