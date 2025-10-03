package com.mentara.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.mentara.enums.MoodType;
import com.mentara.entity.PostTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private MoodType mood;
    private List<PostTag> tags;
    private List<String> imageUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private boolean isLiked;
    private Boolean isAnnouncement;
    private String authorRole; // 用户角色，用于前端样式判断
}
