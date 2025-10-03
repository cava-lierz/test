package com.mentara.service;

import com.mentara.entity.Post;
import io.qdrant.client.grpc.Points;

import java.util.List;

public interface QdrantService {
    Long upsertPostVector(Post post);
    List<Points.ScoredPoint> queryPostVector(String query);
    List<Points.ScoredPoint> accurateQueryPostVector(String query);
    Long deletePostVector(Post post);
}
