package com.mentara.controller;

import com.mentara.entity.PostTag;
import com.mentara.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/tags")
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping
    public List<PostTag> getAllTags() {
        return tagService.findAll();
    }
} 