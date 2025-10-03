package com.mentara.service.impl;

import com.mentara.entity.Appointment;
import com.mentara.entity.Expert;
import com.mentara.entity.ExpertSchedule;
import com.mentara.entity.User;
import com.mentara.repository.AppointmentRepository;
import com.mentara.repository.ExpertRepository;
import com.mentara.repository.ExpertScheduleRepository;
import com.mentara.repository.UserRepository;
import com.mentara.service.ExpertScheduleService;
import com.mentara.service.ExpertService;
import com.mentara.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpertScheduleServiceImpl implements ExpertScheduleService {
    private static final Logger log = LoggerFactory.getLogger(ExpertScheduleServiceImpl.class);
    @Autowired
    private ExpertScheduleRepository expertScheduleRepository;
    @Autowired
    private ExpertRepository expertRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ExpertService expertService;
    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * 检查当前用户是否为指定专家的所有者
     */
    public boolean isExpertOwner(Long expertId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                    .getId();
            return userId.equals(expertId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取专家的排班信息（适配前端格式）
     * 注意：为了兼容现有前端，这里将状态转换为boolean
     * 0=空闲 和 2=专家不可用 都转换为对应的boolean值
     * 1=已预约 的时段在这个接口中视为不可用(false)
     */
    @Override
    public Map<String, Object> getExpertSchedule(Long expertId) {
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        
        // 构建前端期望的数据格式：[8时段][14天]
        boolean[][] transposedSlots = new boolean[8][14];
        LocalDate today = LocalDate.now();
        
        for (int day = 0; day < 14; day++) {
            LocalDate currentDate = today.plusDays(day);
            int[] daySlots = schedule.getSlotsForDate(currentDate);
            
            for (int slot = 0; slot < 8; slot++) {
                // 将状态转换为boolean：0(空闲)=true, 1(已预约)=false, 2(专家不可用)=false
                transposedSlots[slot][day] = (daySlots[slot] == 0);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", transposedSlots);
        result.put("timeSlots", new String[]{"08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"});
        result.put("baseDate", today);  // 现在baseDate就是今天
        
        return result;
    }

    /**
     * 通过用户ID获取专家的排班信息
     */
    @Override
    public Map<String, Object> getExpertScheduleByUserId(Long userId) {
        Expert expert = getOrCreateExpertByUserId(userId);
        return getExpertSchedule(expert.getId());
    }

    /**
     * 通过用户ID更新专家的排班信息
     */
    @Override
    public boolean updateExpertAvailabilityByUserId(Long userId, int dayOffset, int periodIndex, boolean available) {
        Expert expert = getOrCreateExpertByUserId(userId);
        return updateExpertAvailability(expert.getId(), dayOffset, periodIndex, available);
    }

    /**
     * 更新专家的排班偏好 (在状态0和2之间切换)
     */
    @Override
    @Transactional
    public boolean updateExpertAvailability(Long expertId, int dayOffset, int periodIndex, boolean available) {
        if (dayOffset < 0 || dayOffset >= 14 || periodIndex < 0 || periodIndex >= 8) {
            log.warn("更新排班参数非法：dayOffset={}, periodIndex={}", dayOffset, periodIndex);
            return false;
        }
        
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
        
        // 记录更新前的状态
        int beforeStatus = schedule.getSlotStatus(targetDate, periodIndex);
        log.info("更新前排班状态：expertId={}, 日期={}, 时段={}, 更新前状态={}", 
                expertId, targetDate, periodIndex, beforeStatus);
        
        // 设置专家排班偏好（只在状态0和2之间切换，不影响已预约状态1）
        schedule.setExpertAvailability(targetDate, periodIndex, available);
        
        // 手动触发序列化（确保数据被正确序列化）
        schedule.triggerSerialization();
        
        // 保存到数据库
        ExpertSchedule savedSchedule = expertScheduleRepository.save(schedule);
        
        // 验证保存后的状态
        int afterStatus = savedSchedule.getSlotStatus(targetDate, periodIndex);
        log.info("更新后排班状态：expertId={}, 日期={}, 时段={}, 更新后状态={}, JSON={}",
                expertId, targetDate, periodIndex, afterStatus, savedSchedule.getScheduleJson());
        
        return true;
    }

    /**
     * 批量更新专家的排班偏好
     */
    @Override
    @Transactional
    public int batchUpdateExpertAvailabilityByUserId(Long userId, java.util.List<com.mentara.controller.ExpertScheduleController.ScheduleUpdateRequest> updates) {
        Expert expert = getOrCreateExpertByUserId(userId);
        ExpertSchedule schedule = getOrCreateSchedule(expert.getId());
        
        int successCount = 0;
        
        for (com.mentara.controller.ExpertScheduleController.ScheduleUpdateRequest update : updates) {
            if (update.getDayOffset() < 0 || update.getDayOffset() >= 14 || 
                update.getPeriodIndex() < 0 || update.getPeriodIndex() >= 8) {
                log.warn("批量更新排班参数非法：dayOffset={}, periodIndex={}", 
                        update.getDayOffset(), update.getPeriodIndex());
                continue;
            }
            
            java.time.LocalDate targetDate = java.time.LocalDate.now().plusDays(update.getDayOffset());
            
            // 记录更新前的状态
            int beforeStatus = schedule.getSlotStatus(targetDate, update.getPeriodIndex());
            log.info("批量更新前排班状态：expertId={}, 日期={}, 时段={}, 更新前状态={}", 
                    expert.getId(), targetDate, update.getPeriodIndex(), beforeStatus);
            
            // 设置专家排班偏好（只在状态0和2之间切换，不影响已预约状态1）
            schedule.setExpertAvailability(targetDate, update.getPeriodIndex(), update.isAvailable());
            successCount++;
        }
        
        // 统一保存一次
        if (successCount > 0) {
            schedule.triggerSerialization();
            expertScheduleRepository.save(schedule);
            log.info("批量更新排班完成：expertId={}, 成功更新{}个时间段", expert.getId(), successCount);
        }
        
        return successCount;
    }

    /**
     * 预约时段 (将状态从0改为1)
     */
    @Override
    @Transactional
    public boolean bookSlotDirectly(Long expertId, int dayOffset, int periodIndex) {
        if (dayOffset < 0 || dayOffset >= 14 || periodIndex < 0 || periodIndex >= 8) {
            log.warn("预约参数非法：dayOffset={}, periodIndex={}", dayOffset, periodIndex);
            return false;
        }
        
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
        
        boolean success = schedule.bookSlot(targetDate, periodIndex);
        if (success) {
            schedule.triggerSerialization();
            expertScheduleRepository.save(schedule);
            log.info("预约成功：expertId={}, 日期={}, 时段={}", expertId, targetDate, periodIndex);
        } else {
            log.warn("预约失败，时段不可用：expertId={}, 日期={}, 时段={}, 当前状态={}", 
                    expertId, targetDate, periodIndex, schedule.getSlotStatus(targetDate, periodIndex));
        }
        
        return success;
    }

    /**
     * 取消预约 (将状态从1改为0)
     */
    @Override
    @Transactional
    public boolean cancelSlotDirectly(Long expertId, int dayOffset, int periodIndex) {
        if (dayOffset < 0 || dayOffset >= 14 || periodIndex < 0 || periodIndex >= 8) {
            log.warn("取消预约参数非法：dayOffset={}, periodIndex={}", dayOffset, periodIndex);
            return false;
        }
        
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        LocalDate targetDate = LocalDate.now().plusDays(dayOffset);
        
        boolean success = schedule.cancelBooking(targetDate, periodIndex);
        if (success) {
            schedule.triggerSerialization();
            expertScheduleRepository.save(schedule);
            log.info("取消预约成功：expertId={}, 日期={}, 时段={}", expertId, targetDate, periodIndex);
        } else {
            log.warn("取消预约失败，时段状态不正确：expertId={}, 日期={}, 时段={}, 当前状态={}", 
                    expertId, targetDate, periodIndex, schedule.getSlotStatus(targetDate, periodIndex));
        }
        
        return success;
    }

    @Override
    @Transactional
    public ExpertSchedule getOrCreateSchedule(Long expertId) {
        Optional<ExpertSchedule> optional = expertScheduleRepository.findByExpertId(expertId);
        if (optional.isPresent()) {
            ExpertSchedule schedule = optional.get();
            // 确保未来14天有数据，清理7天前的旧数据
            schedule.ensureFutureDays(14);
            schedule.cleanupOldData(LocalDate.now().minusDays(7));
            expertScheduleRepository.save(schedule);
            return schedule;
        }
        
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在: " + expertId));
        
        ExpertSchedule schedule = new ExpertSchedule();
        schedule.setExpert(expert);
        
        // 初始化未来14天的排班（全部可用）
        schedule.ensureFutureDays(14);
        return expertScheduleRepository.save(schedule);
    }

    /**
     * 不再需要刷新逻辑，因为我们使用绝对日期
     */
    @Override
    @Transactional
    public void refreshScheduleIfNeeded(Long expertId) {
        // 新设计中不需要刷新，数据自动按日期管理
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        // 只需要确保数据完整性：清理旧数据，确保未来数据
        schedule.cleanupOldData(LocalDate.now().minusDays(7));
        schedule.ensureFutureDays(14);
        expertScheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public boolean bookSlot(Long expertId, int dayOffset, int periodIndex) {
        // 直接使用新的 bookSlotDirectly 方法
        return bookSlotDirectly(expertId, dayOffset, periodIndex);
    }

    @Override
    @Transactional
    public void releaseSlot(Long expertId, int dayOffset, int periodIndex) {
        // 使用新的 cancelSlotDirectly 方法
        cancelSlotDirectly(expertId, dayOffset, periodIndex);
    }

    @Override
    public boolean[][] getAvailableSlots(Long expertId) {
        ExpertSchedule schedule = getOrCreateSchedule(expertId);
        boolean[][] slots = new boolean[14][8];
        LocalDate today = LocalDate.now();
        
        for (int day = 0; day < 14; day++) {
            LocalDate currentDate = today.plusDays(day);
            int[] daySlots = schedule.getSlotsForDate(currentDate);
            for (int period = 0; period < 8; period++) {
                // 只有状态为0（空闲）的时段才算可预约
                slots[day][period] = (daySlots[period] == 0);
            }
        }
        
        return slots;
    }

    @Override
    public boolean[][] getAvailableSlotsByUserId(Long userId) {
        Expert expert = getOrCreateExpertByUserId(userId);
        return getAvailableSlots(expert.getId());
    }

    /**
     * 获取或创建专家记录
     */
    private Expert getOrCreateExpertByUserId(Long userId) {
        Optional<Expert> expertOpt = expertRepository.findByUserId(userId);
        
        if (expertOpt.isPresent()) {
            return expertOpt.get();
        }
        
        // 创建新的专家记录
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.isExpert()) {
            throw new RuntimeException("用户不是专家");
        }
        
        Expert expert = new Expert();
        expert.setUserId(userId);
        expert.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        expert.setSpecialty("心理咨询");
        expert.setStatus("online");
        return expertRepository.save(expert);
    }

    /**
     * 每天凌晨自动清理过期排班数据
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldSchedules() {
        log.info("开始清理过期排班数据");
        LocalDate cutoffDate = LocalDate.now().minusDays(7);
        
        for (Expert expert : expertRepository.findAll()) {
            try {
                ExpertSchedule schedule = getOrCreateSchedule(expert.getId());
                schedule.cleanupOldData(cutoffDate);
                schedule.ensureFutureDays(14);  // 确保未来14天有数据
                expertScheduleRepository.save(schedule);
            } catch (Exception e) {
                log.error("清理专家{}排班数据失败", expert.getId(), e);
            }
        }
        
        log.info("清理过期排班数据完成");
    }

    /**
     * 数据迁移：将旧的boolean格式数据迁移到新的int状态格式
     * 运行一次后可以删除此方法
     */
    @Transactional
    public void migrateScheduleDataFormat() {
        log.info("开始数据迁移：boolean格式 -> int状态格式");
        
        List<ExpertSchedule> allSchedules = expertScheduleRepository.findAll();
        int migratedCount = 0;
        
        for (ExpertSchedule schedule : allSchedules) {
            try {
                String originalJson = schedule.getScheduleJson();
                
                // 检查是否已经是新格式（包含数字0,1,2而不是只有0,1）
                if (originalJson.contains("\"2\"") || originalJson.contains(",2,") || originalJson.contains(",2\"")) {
                    log.debug("排班数据已经是新格式，跳过：{}", schedule.getId());
                    continue;
                }
                
                // 手动反序列化并转换
                schedule.triggerDeserialization();
                Map<LocalDate, int[]> newDateToSlots = new HashMap<>();
                
                // 这里假设旧数据在getDefaultSlots()中仍返回boolean[]
                // 我们需要特殊处理来迁移数据
                LocalDate today = LocalDate.now();
                for (int day = -7; day < 21; day++) { // 迁移过去7天到未来21天的数据
                    LocalDate date = today.plusDays(day);
                    try {
                        int[] oldSlots = schedule.getSlotsForDate(date);
                        int[] newSlots = new int[8];
                        for (int i = 0; i < 8; i++) {
                            // 将boolean转换为int状态：true->0(空闲), false->2(专家不可用)
                            newSlots[i] = (oldSlots[i] == 1) ? 0 : 2;
                        }
                        newDateToSlots.put(date, newSlots);
                    } catch (Exception e) {
                        log.debug("处理日期{}时出现问题，使用默认值", date);
                        int[] defaultSlots = new int[8];
                        Arrays.fill(defaultSlots, 0); // 默认全部空闲
                        newDateToSlots.put(date, defaultSlots);
                    }
                }
                
                // 更新内存中的映射
                schedule.getClass().getDeclaredField("dateToSlots").setAccessible(true);
                schedule.getClass().getDeclaredField("dateToSlots").set(schedule, newDateToSlots);
                
                // 触发序列化并保存
                schedule.triggerSerialization();
                expertScheduleRepository.save(schedule);
                
                migratedCount++;
                log.info("成功迁移排班数据：expertId={}, 新JSON={}", 
                        schedule.getExpert().getId(), schedule.getScheduleJson());
                        
            } catch (Exception e) {
                log.error("迁移排班数据失败：scheduleId={}", schedule.getId(), e);
            }
        }
        
        log.info("数据迁移完成，共迁移{}条记录", migratedCount);
    }

    /**
     * 将小时转换为periodIndex
     * 时段定义：08:00, 09:00, 10:00, 11:00, 14:00, 15:00, 16:00, 17:00
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

    @Override
    public int[][] getDetailedSlots(Long expertId) {
        try {
            log.info("获取详细时间表状态 - expertId: {}", expertId);
            
            // 直接从数据库重新获取最新的排班数据
            ExpertSchedule schedule = expertScheduleRepository.findByExpertId(expertId)
                    .orElse(null);
            
            if (schedule == null) {
                // 如果没有排班记录，创建一个默认的
                schedule = getOrCreateSchedule(expertId);
            } else {
                // 手动触发反序列化，确保获取最新数据
                schedule.triggerDeserialization();
                log.info("重新加载排班数据 - expertId: {}, JSON: {}", expertId, schedule.getScheduleJson());
            }
            
            int[][] slots = new int[14][8]; // 0=空闲(可预约), 1=用户预约(已被预约), 2=专家设置不可预约
            LocalDate today = LocalDate.now();
            
            // 获取专家用户
            Expert expert = expertRepository.findById(expertId)
                    .orElseThrow(() -> new RuntimeException("专家不存在"));
            log.info("找到专家记录 - expertId: {}, userId: {}", expertId, expert.getUserId());
            
            if (expert.getUserId() == null) {
                log.warn("专家没有关联用户ID，只返回基础排班状态");
                // 直接返回存储的状态
                for (int day = 0; day < 14; day++) {
                    LocalDate currentDate = today.plusDays(day);
                    int[] daySlots = schedule.getSlotsForDate(currentDate);
                    System.arraycopy(daySlots, 0, slots[day], 0, 8);
                }
                return slots;
            }
            
            // 获取该专家的所有有效预约（pending和confirmed状态）
            List<Appointment> activeAppointments = appointmentRepository.findByExpertAndStatusIn(
                expert, List.of("pending", "confirmed")
            );
            
            log.info("找到{}个有效预约", activeAppointments.size());
            
            // 初始化时间表状态 - 从schedule表读取基础状态
            for (int day = 0; day < 14; day++) {
                LocalDate currentDate = today.plusDays(day);
                int[] daySlots = schedule.getSlotsForDate(currentDate);
                System.arraycopy(daySlots, 0, slots[day], 0, 8);
            }
            
            // 根据有效预约更新状态
            for (Appointment appointment : activeAppointments) {
                LocalDateTime appointmentTime = appointment.getAppointmentTime();
                LocalDate appointmentDate = appointmentTime.toLocalDate();
                
                // 计算dayOffset
                int dayOffset = (int) java.time.Duration.between(
                    today.atStartOfDay(),
                    appointmentDate.atStartOfDay()
                ).toDays();
                
                // 检查是否在未来14天内
                if (dayOffset >= 0 && dayOffset < 14) {
                    int periodIndex = hourToPeriodIndex(appointmentTime.getHour());
                    if (periodIndex >= 0 && periodIndex < 8) {
                        // 将状态设置为1（已被预约）
                        slots[dayOffset][periodIndex] = 1;
                        log.debug("设置预约状态 - dayOffset: {}, periodIndex: {}, appointmentId: {}", 
                                dayOffset, periodIndex, appointment.getId());
                    }
                }
            }
            
            log.info("详细时间表状态计算完成");
            return slots;
            
        } catch (Exception e) {
            log.error("获取详细时间表状态失败 - expertId: {}", expertId, e);
            throw new RuntimeException("获取详细时间表状态失败: " + e.getMessage());
        }
    }

    @Override
    public int[][] getDetailedSlotsByUserId(Long userId) {
        try {
            log.info("通过用户ID获取详细时间表状态 - userId: {}", userId);
            Expert expert = getOrCreateExpertByUserId(userId);
            log.info("找到或创建专家记录 - expertId: {}", expert.getId());
            return getDetailedSlots(expert.getId());
        } catch (Exception e) {
            log.error("通过用户ID获取详细时间表状态失败 - userId: {}", userId, e);
            throw new RuntimeException("获取详细时间表状态失败: " + e.getMessage());
        }
    }
} 