package com.mentara.converter;

import com.mentara.dto.response.PostResponse;
import com.mentara.entity.Post;
import com.mentara.entity.User;
import com.mentara.util.AvatarUtils;
import org.springframework.stereotype.Component;


@Component
public class PostConverter {
    public PostResponse toResponse(Post post, boolean isLiked) {
        User author = post.getAuthor();
        
        // 处理已删除用户的情况
        String authorNickname = author.getNickname();
        String authorAvatar = author.getAvatar();
        String authorRole = author.getRole().name();
        
        // 如果用户已删除，使用默认值
        if (author.getIsDeleted() != null && author.getIsDeleted()) {
            authorNickname = "已删除用户";
            authorAvatar = AvatarUtils.getDeletedUserAvatar();
            authorRole = "USER"; // 已删除用户显示为普通用户
        }
    
        PostResponse response = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .mood(post.getMood())
                .tags(post.getTags())
                .imageUrls(post.getImageUrls())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .authorId(author.getId())
                .authorNickname(authorNickname)
                .authorAvatar(authorAvatar)
                .isLiked(isLiked)
                .isAnnouncement(post.getIsAnnouncement())
                .authorRole(authorRole)
                .build();
        return response;
    }
}