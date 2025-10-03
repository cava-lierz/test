package com.mentara.repository;

import com.mentara.entity.Checkin;
import com.mentara.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {
    
    Optional<Checkin> findByUserAndCheckinDate(User user, LocalDate checkinDate);

        
    @Query("SELECT AVG(c.rating) FROM Checkin c WHERE c.user.id = :userId AND c.checkinDate >= :startDate")
    Double getAverageRatingByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    // 获取用户平均心情评分
    @Query("SELECT AVG(c.rating) FROM Checkin c WHERE c.user.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
    
    // 获取全局平均心情评分
    @Query("SELECT AVG(c.rating) FROM Checkin c")
    Double findGlobalAverageRating();

    List<Checkin> findByUserAndCheckinDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    // 获取指定用户的心情记录（分页）
    Page<Checkin> findByUserIdOrderByCheckinDateDesc(Long userId, Pageable pageable);
}