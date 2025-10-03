package com.mentara.service;

import java.util.List;

import com.mentara.entity.PostTag;

public interface TagService {
    List<PostTag> findAll();
    PostTag findById(Long id);

    void initTags();
}

