package com.mentara.repository;

import com.mentara.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    /**
     * 根据用户ID查找专家
     */
    Optional<Expert> findByUserId(Long userId);
} 