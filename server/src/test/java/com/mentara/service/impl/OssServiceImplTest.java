package com.mentara.service.impl;

import com.obs.services.ObsClient;
import com.mentara.config.OssConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

class OssServiceImplTest {
    @Mock
    private ObsClient ossClient;
    @Mock
    private OssConfig ossConfig;
    @InjectMocks
    private OssServiceImpl ossService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateUniqueFileName_shouldReturnFileName() {
        String result = ossService.generateUniqueFileName("test.jpg", "prefix_");
        assertTrue(result.startsWith("prefix_"));
        assertTrue(result.endsWith(".jpg"));
    }
} 