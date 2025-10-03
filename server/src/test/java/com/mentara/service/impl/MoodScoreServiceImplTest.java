package com.mentara.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

class MoodScoreServiceImplTest {
    @InjectMocks
    private MoodScoreServiceImpl moodScoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void dummyTest() {
        assertNotNull(moodScoreService);
    }
} 