package com.mentara.dto.request;

import com.mentara.enums.MoodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 30, message = "标题长度必须在2-30个字符之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 1000, message = "内容长度必须在1-1000个字符之间")
    private String content;

    private MoodType mood; // 心情，公告帖子可为空

    @NotNull(message = "标签不能为空")
    @Size(max = 10, message = "标签数量不能超过10个")
    private List<Long> tagIds; // 标签集合，如 "#正能量", "#分享"

    @Size(max = 9, message = "图片数量不能超过9张")
    // Accept either a http(s) URL or an object key (e.g. posts/xxxxx.jpg)
    private List<@Pattern(regexp = "(https?://.+|[A-Za-z0-9_\\-./]+)", message = "图片URL格式不正确") String> imageUrls;

    private Boolean isAnnouncement = false; // 是否为公告帖子
}