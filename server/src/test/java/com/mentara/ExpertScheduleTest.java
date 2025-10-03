package com.mentara;

import com.mentara.entity.ExpertSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * 专家排班系统测试
 * 验证新的三状态存储设计是否正确工作
 * 状态码: 0=空闲, 1=用户预约, 2=专家设置不可预约
 */
public class ExpertScheduleTest {
    
    private ExpertSchedule schedule;
    
    @BeforeEach
    void setUp() {
        schedule = new ExpertSchedule();
        schedule.setScheduleJson("{}");
        schedule.triggerDeserialization(); // 测试专用方法
    }
    
    @Test
    void testInitialEmptySchedule() {
        // 测试初始空排班
        LocalDate today = LocalDate.now();
        int[] slots = schedule.getSlotsForDate(today);
        
        // 默认应该全部为空闲状态 (0)
        for (int slot : slots) {
            assertEquals(0, slot, "默认时段应该全部为空闲状态");
        }
    }
    
    @Test
    void testSetAndGetSlots() {
        // 测试设置和获取时段
        LocalDate testDate = LocalDate.now().plusDays(3);
        
        // 设置某个时段为专家不可预约
        schedule.setSlotStatus(testDate, 2, 2);
        
        int[] slots = schedule.getSlotsForDate(testDate);
        assertEquals(2, slots[2], "设置的时段应该为专家不可预约状态");
        
        // 其他时段应该仍然为空闲状态
        for (int i = 0; i < 8; i++) {
            if (i != 2) {
                assertEquals(0, slots[i], "其他时段应该仍然为空闲状态");
            }
        }
    }
    
    @Test
    void testJsonSerialization() {
        // 测试JSON序列化和反序列化
        LocalDate date1 = LocalDate.now();
        LocalDate date2 = LocalDate.now().plusDays(1);
        
        // 设置一些时段
        schedule.setSlotStatus(date1, 0, 2); // 专家不可预约
        schedule.setSlotStatus(date1, 3, 1); // 用户预约
        schedule.setSlotStatus(date2, 1, 2); // 专家不可预约
        
        // 触发序列化
        schedule.triggerSerialization();
        String json = schedule.getScheduleJson();
        
        // 验证JSON不为空
        assertNotNull(json);
        assertFalse(json.equals("{}"));
        
        // 创建新实例并反序列化
        ExpertSchedule newSchedule = new ExpertSchedule();
        newSchedule.setScheduleJson(json);
        newSchedule.triggerDeserialization();
        
        // 验证数据一致
        assertEquals(2, newSchedule.getSlotsForDate(date1)[0]);
        assertEquals(1, newSchedule.getSlotsForDate(date1)[3]);
        assertEquals(2, newSchedule.getSlotsForDate(date2)[1]);
        assertEquals(0, newSchedule.getSlotsForDate(date1)[1]);
        assertEquals(0, newSchedule.getSlotsForDate(date2)[0]);
    }
    
    @Test
    void testEnsureFutureDays() {
        // 测试确保未来天数有数据
        schedule.ensureFutureDays(7);
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            int[] slots = schedule.getSlotsForDate(date);
            
            // 所有时段应该为空闲状态
            for (int slot : slots) {
                assertEquals(0, slot, "未来天数的时段应该默认为空闲状态");
            }
        }
    }
    
    @Test
    void testCleanupOldData() {
        // 测试清理过期数据
        LocalDate oldDate = LocalDate.now().minusDays(10);
        LocalDate futureDate = LocalDate.now().plusDays(5);
        
        // 设置一些过期和未来的数据
        schedule.setSlotStatus(oldDate, 0, 2);
        schedule.setSlotStatus(futureDate, 0, 2);
        
        // 清理7天前的数据
        schedule.cleanupOldData(LocalDate.now().minusDays(7));
        
        // 过期数据应该被清理，未来数据应该保留
        assertEquals(0, schedule.getSlotsForDate(oldDate)[0], "过期数据应该被清理，恢复默认值");
        assertEquals(2, schedule.getSlotsForDate(futureDate)[0], "未来数据应该保留");
    }
    
    @Test
    void testNoCyclicOverwrite() {
        // 测试不会出现循环覆盖问题（这是旧系统的主要问题）
        LocalDate today = LocalDate.now();
        
        // 设置今天的时段
        schedule.setSlotStatus(today, 0, 2);
        
        // 设置15天后的时段（在旧系统中会覆盖第1天）
        LocalDate day15 = today.plusDays(15);
        schedule.setSlotStatus(day15, 0, 1);
        
        // 验证两个日期的数据都独立存在
        assertEquals(2, schedule.getSlotsForDate(today)[0], "今天的设置应该保持");
        assertEquals(1, schedule.getSlotsForDate(day15)[0], "第15天的设置应该保持");
        
        // 验证其他时段不受影响
        assertEquals(0, schedule.getSlotsForDate(today)[1], "今天的其他时段应该为空闲");
        assertEquals(0, schedule.getSlotsForDate(day15)[1], "第15天的其他时段应该为空闲");
    }
    
    @Test
    void testDateCalculationConsistency() {
        // 测试日期计算的一致性（验证不会出现时段错位问题）
        LocalDate baseDate = LocalDate.now();
        
        // 设置不同天数的相同时段
        for (int day = 0; day < 14; day++) {
            LocalDate date = baseDate.plusDays(day);
            schedule.setSlotStatus(date, 2, 2); // 都设置第2个时段为专家不可预约
        }
        
        // 验证所有天的第2个时段都不可用，其他时段可用
        for (int day = 0; day < 14; day++) {
            LocalDate date = baseDate.plusDays(day);
            int[] slots = schedule.getSlotsForDate(date);
            
            assertEquals(2, slots[2], "第" + day + "天的第2个时段应该为专家不可预约");
            
            for (int slot = 0; slot < 8; slot++) {
                if (slot != 2) {
                    assertEquals(0, slots[slot], "第" + day + "天的第" + slot + "个时段应该为空闲");
                }
            }
        }
    }
    
    @Test
    void testBookingOperations() {
        // 测试预约操作
        LocalDate testDate = LocalDate.now().plusDays(1);
        
        // 预约时段
        schedule.bookSlot(testDate, 3);
        assertEquals(1, schedule.getSlotsForDate(testDate)[3], "时段应该被标记为已预约");
        
        // 取消预约
        schedule.cancelBooking(testDate, 3);
        assertEquals(0, schedule.getSlotsForDate(testDate)[3], "时段应该恢复为空闲状态");
    }
    
    @Test
    void testExpertAvailabilityOperations() {
        // 测试专家可用性操作
        LocalDate testDate = LocalDate.now().plusDays(2);
        
        // 设置专家不可预约
        schedule.setExpertAvailability(testDate, 5, false);
        assertEquals(2, schedule.getSlotsForDate(testDate)[5], "时段应该被标记为专家不可预约");
        
        // 设置专家可预约
        schedule.setExpertAvailability(testDate, 5, true);
        assertEquals(0, schedule.getSlotsForDate(testDate)[5], "时段应该恢复为空闲状态");
    }
} 