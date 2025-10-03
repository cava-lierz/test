package com.mentara.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * 专家预约时间表实体 - 重新设计使用绝对日期存储
 */
@Entity
@Getter
@Setter
@Table(name = "expert_schedules")
public class ExpertSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联专家 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false, unique = true)
    private Expert expert;

    /**
     * 每个日期对应8个时段的状态
     * 时段为：08:00、09:00、10:00、11:00、14:00、15:00、16:00、17:00
     * （中午12:00-14:00为休息时间，不开放预约）
     * 
     * 状态码：
     * 0 = 空闲（可预约）
     * 1 = 用户预约（已被预约）
     * 2 = 专家设置不可预约
     * 
     * 存储格式：序列化为JSON字符串
     * 格式: {"2024-01-15":"0,0,2,0,0,0,2,0","2024-01-16":"0,1,0,0,0,0,0,0",...}
     */
    @Lob
    @Column(nullable = false, length = 4096)
    private String scheduleJson = "{}";

    /** 内存中的日期到时段状态映射 */
    @Transient
    private Map<LocalDate, int[]> dateToSlots = new HashMap<>();

    @PostLoad
    protected void loadSchedule() {
        // 反序列化scheduleJson为dateToSlots
        try {
            if (scheduleJson == null || scheduleJson.trim().equals("{}") || scheduleJson.trim().isEmpty()) {
                dateToSlots = new HashMap<>();
                return;
            }
            
            // 简单解析JSON格式的字符串
            String content = scheduleJson.substring(1, scheduleJson.length() - 1); // 移除大括号
            
            // 如果内容为空，直接返回
            if (content.trim().isEmpty()) {
                dateToSlots = new HashMap<>();
                return;
            }
            
            String[] entries = content.split("\",\"");
            
            for (String entry : entries) {
                String[] parts = entry.split("\":\"");
                if (parts.length == 2) {
                    String dateStr = parts[0].replace("\"", "");
                    String slotsStr = parts[1].replace("\"", "");
                    
                    LocalDate date = LocalDate.parse(dateStr);
                    String[] slotValues = slotsStr.split(",");
                    int[] slots = new int[8];
                    
                    for (int i = 0; i < 8 && i < slotValues.length; i++) {
                        try {
                            slots[i] = Integer.parseInt(slotValues[i]);
                            // 验证状态值范围
                            if (slots[i] < 0 || slots[i] > 2) {
                                slots[i] = 0; // 默认为空闲
                            }
                        } catch (NumberFormatException e) {
                            slots[i] = 0; // 默认为空闲
                        }
                    }
                    
                    dateToSlots.put(date, slots);
                }
            }
        } catch (Exception e) {
            // 解析失败，初始化为空
            dateToSlots = new HashMap<>();
        }
    }

    @PrePersist
    @PreUpdate
    private void saveSchedule() {
        // 序列化dateToSlots为scheduleJson
        if (dateToSlots.isEmpty()) {
            scheduleJson = "{}";
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        
        for (Map.Entry<LocalDate, int[]> entry : dateToSlots.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey().toString()).append("\":\"");
            int[] slots = entry.getValue();
            for (int i = 0; i < 8; i++) {
                sb.append(slots[i]);
                if (i < 7) sb.append(",");
            }
            sb.append("\"");
        }
        
        sb.append("}");
        scheduleJson = sb.toString();
    }

    /**
     * 获取指定日期的时段状态
     */
    public int[] getSlotsForDate(LocalDate date) {
        return dateToSlots.getOrDefault(date, getDefaultSlots());
    }

    /**
     * 设置指定日期的时段状态
     */
    public void setSlotsForDate(LocalDate date, int[] slots) {
        dateToSlots.put(date, Arrays.copyOf(slots, 8));
    }

    /**
     * 设置指定日期和时段的状态
     * @param date 日期
     * @param slotIndex 时段索引 (0-7)
     * @param status 状态 (0=空闲, 1=已预约, 2=专家不可用)
     */
    public void setSlotStatus(LocalDate date, int slotIndex, int status) {
        int[] slots = getSlotsForDate(date);
        if (slotIndex >= 0 && slotIndex < 8 && status >= 0 && status <= 2) {
            slots[slotIndex] = status;
            setSlotsForDate(date, slots);
        }
    }

    /**
     * 获取指定日期和时段的状态
     */
    public int getSlotStatus(LocalDate date, int slotIndex) {
        int[] slots = getSlotsForDate(date);
        if (slotIndex >= 0 && slotIndex < 8) {
            return slots[slotIndex];
        }
        return 0; // 默认空闲
    }

    /**
     * 检查指定时段是否可预约 (状态为0)
     */
    public boolean isSlotAvailable(LocalDate date, int slotIndex) {
        return getSlotStatus(date, slotIndex) == 0;
    }

    /**
     * 设置专家排班偏好 (在状态0和2之间切换)
     */
    public void setExpertAvailability(LocalDate date, int slotIndex, boolean available) {
        int currentStatus = getSlotStatus(date, slotIndex);
        if (currentStatus != 1) { // 不能修改已预约的时段
            setSlotStatus(date, slotIndex, available ? 0 : 2);
        }
    }

    /**
     * 预约时段 (将状态从0改为1)
     */
    public boolean bookSlot(LocalDate date, int slotIndex) {
        if (isSlotAvailable(date, slotIndex)) {
            setSlotStatus(date, slotIndex, 1);
            return true;
        }
        return false;
    }

    /**
     * 取消预约 (将状态从1改为0)
     */
    public boolean cancelBooking(LocalDate date, int slotIndex) {
        if (getSlotStatus(date, slotIndex) == 1) {
            setSlotStatus(date, slotIndex, 0);
            return true;
        }
        return false;
    }

    /**
     * 获取默认时段（全部空闲）
     */
    private int[] getDefaultSlots() {
        int[] slots = new int[8];
        Arrays.fill(slots, 0); // 默认全部空闲
        return slots;
    }

    /**
     * 清理过期数据（清理指定日期之前的数据）
     */
    public void cleanupOldData(LocalDate beforeDate) {
        dateToSlots.entrySet().removeIf(entry -> entry.getKey().isBefore(beforeDate));
    }

    /**
     * 确保未来N天有默认排班数据
     */
    public void ensureFutureDays(int days) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            if (!dateToSlots.containsKey(date)) {
                setSlotsForDate(date, getDefaultSlots());
            }
        }
    }

    /**
     * 测试专用方法：手动触发序列化
     */
    public void triggerSerialization() {
        saveSchedule();
    }

    /**
     * 测试专用方法：手动触发反序列化
     */
    public void triggerDeserialization() {
        loadSchedule();
    }
} 