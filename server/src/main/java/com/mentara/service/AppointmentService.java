package com.mentara.service;

import com.mentara.dto.AppointmentDTO;
import com.mentara.dto.AppointmentRequestDTO;
import com.mentara.entity.Expert;
import com.mentara.repository.ExpertRepository;
import com.mentara.entity.Appointment;
import com.mentara.entity.User;
import com.mentara.entity.UserRole;
import com.mentara.repository.AppointmentRepository;
import com.mentara.repository.ExpertRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.ExpertScheduleService;
import com.mentara.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 预约服务
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentRepository appointmentRepository;
    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;
    private final ExpertScheduleService expertScheduleService;
    private final ExpertService expertService;

    /**
     * 将小时转换为periodIndex
     * 时段定义：08:00, 09:00, 10:00, 11:00, 14:00, 15:00, 16:00, 17:00
     * 12:00-14:00为休息时间，不开放预约
     * 对于异常时间（如01:00, 02:00等），映射到最近的有效时间段
     */
    private int hourToPeriodIndex(int hour) {
        switch (hour) {
            case 8: return 0;   // 08:00
            case 9: return 1;   // 09:00
            case 10: return 2;  // 10:00
            case 11: return 3;  // 11:00
            case 14: return 4;  // 14:00
            case 15: return 5;  // 15:00
            case 16: return 6;  // 16:00
            case 17: return 7;  // 17:00
            case 1: return 0;   // 01:00 -> 映射到08:00
            case 2: return 0;   // 02:00 -> 映射到08:00
            case 3: return 0;   // 03:00 -> 映射到08:00
            case 4: return 0;   // 04:00 -> 映射到08:00
            case 5: return 0;   // 05:00 -> 映射到08:00
            case 6: return 0;   // 06:00 -> 映射到08:00
            case 7: return 0;   // 07:00 -> 映射到08:00
            case 12: return 4;  // 12:00 -> 映射到14:00
            case 13: return 4;  // 13:00 -> 映射到14:00
            case 18: return 7;  // 18:00 -> 映射到17:00
            case 19: return 7;  // 19:00 -> 映射到17:00
            case 20: return 7;  // 20:00 -> 映射到17:00
            case 21: return 7;  // 21:00 -> 映射到17:00
            case 22: return 7;  // 22:00 -> 映射到17:00
            case 23: return 7;  // 23:00 -> 映射到17:00
            case 0: return 0;   // 00:00 -> 映射到08:00
            default: return -1; // 其他无效时间段
        }
    }

    /**
     * 检查指定的专家在指定时间是否可预约
     */
    public boolean isExpertAvailable(Long expertId, LocalDateTime appointmentTime) {
        int periodIndex = hourToPeriodIndex(appointmentTime.getHour());
        if (periodIndex == -1) {
            return false; // 无效时间段
        }

        int dayOffset = (int) java.time.Duration.between(
            LocalDateTime.now().toLocalDate().atStartOfDay(),
            appointmentTime.toLocalDate().atStartOfDay()
        ).toDays();

        // 获取专家的可用时段
        boolean[][] availableSlots = expertScheduleService.getAvailableSlots(expertId);
        
        // 检查范围是否有效
        if (dayOffset < 0 || dayOffset >= availableSlots.length || 
            periodIndex < 0 || periodIndex >= availableSlots[dayOffset].length) {
            return false;
        }
        
        return availableSlots[dayOffset][periodIndex];
    }

    /**
     * 检查当前用户是否为指定专家的所有者
     */
    public boolean isExpertOwner(Long expertId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            return user.getId().equals(expertId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查当前用户是否为指定预约的专家所有者
     */
    public boolean isAppointmentExpertOwner(Long appointmentId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("预约不存在"));
            
            return appointment.getExpertUser().getId().equals(user.getId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建预约
     */
    @Transactional
    public AppointmentDTO createAppointment(Long userId, AppointmentRequestDTO requestDTO) {
        log.info("[创建预约] 开始处理预约请求 - userId: {}, expertUserId: {}, appointmentTime: {}", 
                userId, requestDTO.getExpertUserId(), requestDTO.getAppointmentTime());
        
        // 添加详细的请求数据调试
        log.info("[创建预约调试] ========== 请求数据详情 ==========");
        log.info("[创建预约调试] userId: {}", userId);
        log.info("[创建预约调试] requestDTO: {}", requestDTO);
        log.info("[创建预约调试] expertUserId: {}", requestDTO.getExpertUserId());
        log.info("[创建预约调试] appointmentTime: {}", requestDTO.getAppointmentTime());
        log.info("[创建预约调试] description: {}", requestDTO.getDescription());
        log.info("[创建预约调试] contactInfo: {}", requestDTO.getContactInfo());
        log.info("[创建预约调试] duration: {}", requestDTO.getDuration());
        log.info("[创建预约调试] =======================================");
        
        // 验证必要字段
        if (requestDTO.getExpertUserId() == null) {
            throw new RuntimeException("expertUserId不能为空");
        }
        
        // 验证专家用户是否存在且角色为EXPERT
        User expertUser = userRepository.findById(requestDTO.getExpertUserId())
                .orElseThrow(() -> new RuntimeException("专家用户不存在，expertUserId: " + requestDTO.getExpertUserId()));
        
        log.info("[创建预约] 找到专家用户 - expertUser: {}, role: {}, isExpert: {}", 
                expertUser.getUsername(), expertUser.getRole(), expertUser.isExpert());
        
        if (!expertUser.isExpert()) {
            throw new RuntimeException("指定用户不是专家，当前角色: " + expertUser.getRole());
        }

        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查预约时间是否冲突（重叠判断）
        LocalDateTime startTime = requestDTO.getAppointmentTime();
        LocalDateTime endTime = startTime.plusMinutes(requestDTO.getDuration());
        
        List<Appointment> appointments = appointmentRepository.findByExpertUserAndStatusIn(expertUser, List.of("pending", "confirmed"));
        boolean hasConflict = appointments.stream().anyMatch(a -> {
            LocalDateTime aStart = a.getAppointmentTime();
            LocalDateTime aEnd = aStart.plusMinutes(a.getDuration());
            return aStart.isBefore(endTime) && aEnd.isAfter(startTime);
        });
        if (hasConflict) {
            log.error("[预约冲突] expertUserId={}, startTime={}, endTime={}, 冲突预约数量={}, 详情={}",
                expertUser.getId(), startTime, endTime, appointments.size(),
                appointments.stream().map(a -> "id=" + a.getId() + ",status=" + a.getStatus()).toList());
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        // 计算dayOffset和periodIndex
        int dayOffset = (int) java.time.Duration.between(
            LocalDateTime.now().toLocalDate().atStartOfDay(),
            startTime.toLocalDate().atStartOfDay()
        ).toDays();
        int periodIndex = hourToPeriodIndex(startTime.getHour());
        log.info("[预约调试] 当前本地时间: {}，前端传入预约时间: {}，dayOffset: {}，periodIndex: {}", LocalDateTime.now(), startTime, dayOffset, periodIndex);
        if (periodIndex == -1) {
            throw new RuntimeException("预约时间不在可预约范围内");
        }
        
        // 根据expertUser找到对应的Expert记录
        Expert expert = expertRepository.findByUserId(expertUser.getId())
                .orElseThrow(() -> new RuntimeException("找不到专家记录，expertUserId: " + expertUser.getId()));
        log.info("[预约调试] 找到Expert记录 - expertId: {}, expertName: {}", expert.getId(), expert.getName());
        
        // 联动专家排班 - 使用Expert的ID
        boolean booked = expertScheduleService.bookSlot(expert.getId(), dayOffset, periodIndex);
        if (!booked) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setExpertUser(expertUser);
        appointment.setExpert(expert); // 设置Expert关联
        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
        appointment.setDescription(requestDTO.getDescription());
        appointment.setContactInfo(requestDTO.getContactInfo());
        appointment.setDuration(requestDTO.getDuration());
        appointment.setStatus("pending");

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 获取用户的预约列表
     */
    public Page<AppointmentDTO> getUserAppointments(User user, Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return appointments.map(this::convertToDTO);
    }

    /**
     * 获取专家的预约列表（通过Expert ID - 已废弃，建议使用getExpertAppointmentsByUserId）
     */
    @Deprecated
    public Page<AppointmentDTO> getExpertAppointments(Long expertId, Pageable pageable) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在"));
        
        // 如果专家有关联的用户，使用新的方法
        if (expert.getUserId() != null) {
            return getExpertAppointmentsByUserId(expert.getUserId(), pageable);
        }
        
        // 兼容性处理：创建一个临时的User对象
        throw new RuntimeException("此专家没有关联的用户账号，无法获取预约列表");
    }

    /**
     * 通过用户ID获取专家的预约列表（用于专家用户获取自己的预约）
     */
    public Page<AppointmentDTO> getExpertAppointmentsByUserId(Long userId, Pageable pageable) {
        // 验证用户是否为专家
        User expertUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!expertUser.isExpert()) {
            throw new RuntimeException("用户不是专家");
        }
        
        Page<Appointment> appointments = appointmentRepository.findByExpertUserOrderByCreatedAtDesc(expertUser, pageable);
        return appointments.map(this::convertToDTO);
    }

    /**
     * 获取预约详情
     */
    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        return convertToDTO(appointment);
    }

    /**
     * 确认预约
     */
    @Transactional
    public AppointmentDTO confirmAppointment(Long appointmentId, String expertReply) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        appointment.setStatus("confirmed");
        appointment.setExpertReply(expertReply);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 拒绝预约
     */
    @Transactional
    public AppointmentDTO rejectAppointment(Long appointmentId, String expertReply) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        appointment.setStatus("rejected");
        appointment.setExpertReply(expertReply);
        
        // 释放预约时间段 - 先通过expertUserId找到expertId
        Long expertUserId = appointment.getExpertUser().getId();
        Optional<Expert> expertOpt = expertRepository.findByUserId(expertUserId);
        if (expertOpt.isPresent()) {
            Long expertId = expertOpt.get().getId();
            int dayOffset = (int) java.time.Duration.between(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                appointment.getAppointmentTime().toLocalDate().atStartOfDay()
            ).toDays();
            int periodIndex = hourToPeriodIndex(appointment.getAppointmentTime().getHour());
            if (periodIndex != -1) {
                expertScheduleService.releaseSlot(expertId, dayOffset, periodIndex);
                log.info("释放预约时间段成功 - expertId: {}, dayOffset: {}, periodIndex: {}", 
                        expertId, dayOffset, periodIndex);
            }
        } else {
            log.warn("跳过释放排班时间段，找不到专家记录: expertUserId={}", expertUserId);
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 取消预约
     */
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        // 只有预约用户或管理员可以取消预约
        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限取消此预约");
        }
        
        // 释放预约时间段 - 先通过expertUserId找到expertId
        Long expertUserId = appointment.getExpertUser().getId();
        Optional<Expert> expertOpt = expertRepository.findByUserId(expertUserId);
        if (expertOpt.isPresent()) {
            Long expertId = expertOpt.get().getId();
            int dayOffset = (int) java.time.Duration.between(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                appointment.getAppointmentTime().toLocalDate().atStartOfDay()
            ).toDays();
            int periodIndex = hourToPeriodIndex(appointment.getAppointmentTime().getHour());
            if (periodIndex != -1) {
                expertScheduleService.releaseSlot(expertId, dayOffset, periodIndex);
                log.info("释放预约时间段成功 - expertId: {}, dayOffset: {}, periodIndex: {}", 
                        expertId, dayOffset, periodIndex);
            }
        } else {
            log.warn("跳过释放排班时间段，找不到专家记录: expertUserId={}", expertUserId);
        }
        
        appointment.setStatus("cancelled");
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 完成预约
     */
    @Transactional
    public AppointmentDTO completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        appointment.setStatus("completed");
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 评价预约
     */
    @Transactional
    public AppointmentDTO rateAppointment(Long appointmentId, Long userId, String userRating, Integer rating) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        // 只有预约用户可以评价
        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限评价此预约");
        }
        
        appointment.setUserRating(userRating);
        appointment.setRating(rating);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }

    /**
     * 获取待处理的预约数量（通过Expert ID - 已废弃）
     */
    @Deprecated
    public long getPendingAppointmentCount(Long expertId) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在"));
        
        // 如果专家有关联的用户，使用新的方法
        if (expert.getUserId() != null) {
            return getPendingAppointmentCountByUserId(expert.getUserId());
        }
        
        throw new RuntimeException("此专家没有关联的用户账号，无法获取待处理预约数量");
    }

    /**
     * 通过用户ID获取专家的待处理预约数量
     */
    public long getPendingAppointmentCountByUserId(Long userId) {
        User expertUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!expertUser.isExpert()) {
            throw new RuntimeException("用户不是专家");
        }
        
        return appointmentRepository.countByExpertUserAndStatus(expertUser, "pending");
    }

    /**
     * 转换为DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setExpertUserId(appointment.getExpertUser().getId());
        
        // 获取专家信息 - 先尝试从Expert表获取，如果没有则使用User信息
        String expertName = appointment.getExpertUser().getNickname();
        if (expertName == null || expertName.trim().isEmpty()) {
            expertName = appointment.getExpertUser().getUsername();
        }
        
        // 尝试从Expert表获取更详细的专家信息
        Optional<Expert> expertOpt = expertRepository.findByUserId(appointment.getExpertUser().getId());
        if (expertOpt.isPresent()) {
            Expert expert = expertOpt.get();
            expertName = expert.getName();
        }
        
        dto.setExpertName(expertName);
        dto.setUserName(appointment.getUser().getNickname() != null ? 
                       appointment.getUser().getNickname() : appointment.getUser().getUsername());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setDescription(appointment.getDescription());
        dto.setContactInfo(appointment.getContactInfo());
        dto.setDuration(appointment.getDuration());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        dto.setExpertReply(appointment.getExpertReply());
        dto.setUserRating(appointment.getUserRating());
        dto.setRating(appointment.getRating());
        return dto;
    }
} 