package com.mentara.service.impl;

import com.mentara.service.EmbeddingService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QdrantServiceImplTest {
    @Mock
    private QdrantClient qdrantClient;
    @Mock
    private EmbeddingService embeddingService;
    @InjectMocks
    private QdrantServiceImpl qdrantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void queryPostVector_shouldReturnList() throws Exception {
        String keyword = "test";
        List<Float> vector = List.of(0.1f, 0.2f, 0.3f);
        List<Points.ScoredPoint> points = Collections.singletonList(mock(Points.ScoredPoint.class));
        when(embeddingService.getEmbedding(keyword)).thenReturn(vector);
        @SuppressWarnings("unchecked")
        ListenableFuture<List<Points.ScoredPoint>> future = mock(ListenableFuture.class);
        when(qdrantClient.queryAsync(any(Points.QueryPoints.class))).thenReturn(future);
        when(future.get()).thenReturn(points);
        List<Points.ScoredPoint> result = qdrantService.queryPostVector(keyword);
        assertEquals(points, result);
        verify(embeddingService, times(1)).getEmbedding(keyword);
        verify(qdrantClient, times(1)).queryAsync(any(Points.QueryPoints.class));
    }
} 