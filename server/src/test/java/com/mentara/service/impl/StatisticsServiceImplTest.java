package com.mentara.service.impl;

import com.mentara.dto.response.StatisticsResponse;
import com.mentara.repository.UserRepository;
import com.mentara.repository.PostRepository;
import com.mentara.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ReportRepository reportRepository;
    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWeeklyStatistics_shouldReturnResponse() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(postRepository.findAll()).thenReturn(Collections.emptyList());
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());
        StatisticsResponse response = statisticsService.getWeeklyStatistics(2024, 1);
        assertNotNull(response);
    }
} 