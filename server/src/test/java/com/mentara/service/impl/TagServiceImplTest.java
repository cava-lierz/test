package com.mentara.service.impl;

import com.mentara.entity.PostTag;
import com.mentara.repository.PostTagRepository;
import com.mentara.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceImplTest {
    @Mock
    private PostTagRepository postTagRepository;
    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_shouldReturnTag_whenExists() {
        Long tagId = 1L;
        PostTag tag = new PostTag();
        tag.setId(tagId);
        when(postTagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        PostTag result = tagService.findById(tagId);
        assertEquals(tag, result);
    }

    @Test
    void findById_shouldThrow_whenNotExists() {
        Long tagId = 1L;
        when(postTagRepository.findById(tagId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tagService.findById(tagId));
    }
} 