package com.mentara.repository;

import com.mentara.entity.Appointment;
import com.mentara.entity.Expert;
import com.mentara.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约数据访问层
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * 根据用户查找预约
     */
    Page<Appointment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 根据专家用户查找预约
     */
    Page<Appointment> findByExpertUserOrderByCreatedAtDesc(User expertUser, Pageable pageable);

    /**
     * 根据状态查找预约
     */
    Page<Appointment> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /**
     * 根据用户和状态查找预约
     */
    Page<Appointment> findByUserAndStatusOrderByCreatedAtDesc(User user, String status, Pageable pageable);

    /**
     * 根据专家用户和状态查找预约
     */
    Page<Appointment> findByExpertUserAndStatusOrderByCreatedAtDesc(User expertUser, String status, Pageable pageable);

    /**
     * 查找指定时间范围内的有效预约（只查pending和confirmed）
     */
    @Query("SELECT a FROM Appointment a WHERE a.expertUser = :expertUser AND a.appointmentTime BETWEEN :startTime AND :endTime AND a.status IN ('pending', 'confirmed')")
    List<Appointment> findByExpertUserAndTimeRange(@Param("expertUser") User expertUser, 
                                                  @Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找专家用户指定状态的所有预约
     */
    List<Appointment> findByExpertUserAndStatusIn(User expertUser, List<String> status);

    /**
     * 查找专家指定状态的所有预约
     */
    List<Appointment> findByExpertAndStatusIn(com.mentara.entity.Expert expert, List<String> status);

    /**
     * 统计用户的预约数量
     */
    long countByUser(User user);

    /**
     * 统计专家用户的预约数量
     */
    long countByExpertUser(User expertUser);

    /**
     * 统计指定状态的预约数量
     */
    long countByStatus(String status);

    /**
     * 根据专家用户和状态统计预约数量
     */
    long countByExpertUserAndStatus(User expertUser, String status);
} 