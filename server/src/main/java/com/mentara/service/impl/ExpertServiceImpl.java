package com.mentara.service.impl;

import com.mentara.entity.Expert;
import com.mentara.repository.ExpertRepository;
import com.mentara.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl implements ExpertService {

    private final ExpertRepository expertRepository;

    @Override
    @Cacheable(value = "experts", key = "'all'")
    public List<Expert> getAllExperts() {
        return expertRepository.findAll();
    }

    @Override
    @Cacheable(value = "experts", key = "#id")
    public Expert getExpertById(Long id) {
        return expertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("专家不存在"));
    }

    @Override
    @Cacheable(value = "experts", key = "'user_' + #userId")
    public Expert getExpertByUserId(Long userId) {
        return expertRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("专家不存在"));
    }

    @Override
    @CacheEvict(value = "experts", allEntries = true)
    public Expert addExpert(Expert expert) {
        if (expert.getName() == null || expert.getName().trim().isEmpty()) {
            throw new RuntimeException("专家姓名不能为空");
        }
        
        if (expert.getStatus() == null || expert.getStatus().trim().isEmpty()) {
            expert.setStatus("offline");
        }
        
        return expertRepository.save(expert);
    }

    @Override
    @CacheEvict(value = "experts", allEntries = true)
    public Expert updateExpert(Expert expert) {
        if (!expertRepository.existsById(expert.getId())) {
            throw new RuntimeException("专家不存在");
        }
        
        if (expert.getName() == null || expert.getName().trim().isEmpty()) {
            throw new RuntimeException("专家姓名不能为空");
        }
        
        return expertRepository.save(expert);
    }

    @Override
    @CacheEvict(value = "experts", allEntries = true)
    public void deleteExpert(Long id) {
        if (!expertRepository.existsById(id)) {
            throw new RuntimeException("专家不存在");
        }
        expertRepository.deleteById(id);
    }
} 