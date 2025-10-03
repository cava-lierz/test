package com.mentara.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mentara.exception.ResourceNotFoundException;
import com.mentara.entity.PostTag;
import com.mentara.service.TagService;
import com.mentara.repository.PostTagRepository;
import jakarta.annotation.PostConstruct;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private PostTagRepository postTagRepository;

    @Override
    @Cacheable(value = "tags", key = "'all'")
    public List<PostTag> findAll() {
        return postTagRepository.findAll();
    }

    @Override
    @Cacheable(value = "tags", key = "#id")
    public PostTag findById(Long id) {
        return postTagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
    }

    @Override
    @PostConstruct
    public void initTags() {
        if (postTagRepository.count() == 0) {
            postTagRepository.saveAll(Arrays.asList(
                new PostTag(null, "心理健康", "#4CAF50"),
                new PostTag(null, "成长", "#FF9800"),
                new PostTag(null, "匿名", "#9E9E9E"),
                new PostTag(null, "正能量", "#FFD700"),
                new PostTag(null, "情感", "#E91E63"),
                new PostTag(null, "学习", "#2196F3"),
                new PostTag(null, "生活", "#8BC34A"),
                new PostTag(null, "压力", "#F44336"),
                new PostTag(null, "求助", "#FF5722"),
                new PostTag(null, "分享", "#9C27B0")
            ));
        }
    }
}
