package com.mentara.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

class DeepSeekServiceImplTest {
    @InjectMocks
    private DeepSeekServiceImpl deepSeekService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void dummyTest() {
        // 由于 DeepSeekServiceImpl 依赖实际外部服务，这里仅做实例化测试
        assertNotNull(deepSeekService);
    }
} 