package com.mentara.repository;

import com.mentara.entity.ExpertSchedule;
import com.mentara.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExpertScheduleRepository extends JpaRepository<ExpertSchedule, Long> {
    Optional<ExpertSchedule> findByExpert(Expert expert);
    Optional<ExpertSchedule> findByExpertId(Long expertId);
} 