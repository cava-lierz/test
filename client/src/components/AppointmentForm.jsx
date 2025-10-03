import React, { useState, useEffect } from 'react';
import { appointmentAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

// 8个时段（中午休息）- 定义在组件外部避免每次渲染时重新创建
const PERIODS = [
  "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"
];

// 显示用的时间段格式
const PERIOD_DISPLAY = [
  "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
  "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
];

// 对应后端的时间点（用于时间计算）
const PERIOD_HOURS = [8, 9, 10, 11, 14, 15, 16, 17];

export default function AppointmentForm({ expert, availableSlots, detailedSlots, onSuccess, onCancel }) {
  const { user } = useAuth();
  
  // 添加expert对象调试信息
  // 组件初始化
  
  const [formData, setFormData] = useState({
    expertUserId: expert.userId,
    appointmentTime: '',
    description: '',
    contactInfo: user?.email || '',
    duration: 55 // 默认55分钟
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedDay, setSelectedDay] = useState('');
  const [selectedPeriod, setSelectedPeriod] = useState('');

  // 生成可选天和时段（从今天开始）
  const days = Array.from({length: 14}, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() + i); // 不加1，从今天开始
    return date;
  });

  // 判断某个单元格是否为过去时间（修正版）
  const isPastSlot = (date, periodIdx) => {
    const now = new Date();
    // 今天0点
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    if (date < todayStart) return true;
    // 如果是今天，且时段早于当前小时
    if (
      date.getFullYear() === now.getFullYear() &&
      date.getMonth() === now.getMonth() &&
      date.getDate() === now.getDate()
    ) {
      const hour = PERIOD_HOURS[periodIdx];
      if (hour <= now.getHours()) return true;
    }
    return false;
  };

  // 选择后自动生成appointmentTime（本地时间字符串，包含秒数）
  useEffect(() => {
    if (selectedDay !== '' && selectedPeriod !== '') {
      const date = new Date();
      date.setDate(date.getDate() + Number(selectedDay)); // 不加1
      const hour = PERIOD_HOURS[Number(selectedPeriod)];
      const minute = 0;
      date.setHours(Number(hour), Number(minute), 0, 0);
      
      // 生成本地时间格式，包含秒数：YYYY-MM-DDTHH:mm:ss
      const pad = n => n.toString().padStart(2, '0');
      const localTimeStr = date.getFullYear() + '-' +
        pad(date.getMonth() + 1) + '-' +
        pad(date.getDate()) + 'T' +
        pad(date.getHours()) + ':' +
        pad(date.getMinutes()) + ':00';
      
      // 生成时间字符串
      
      setFormData(prev => ({
        ...prev,
        appointmentTime: localTimeStr
      }));
    }
  }, [selectedDay, selectedPeriod]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    setError('');

    try {
      // 数据验证
      if (!formData.appointmentTime) {
        throw new Error('请选择预约时间');
      }

      if (!formData.description?.trim()) {
        throw new Error('请填写预约描述');
      }

      if (!formData.contactInfo.trim()) {
        throw new Error('请填写联系方式');
      }

      // 检查预约时间是否在过去
      const appointmentTime = new Date(formData.appointmentTime);
      if (appointmentTime <= new Date()) {
        throw new Error('预约时间不能在过去');
      }

      // 添加详细调试日志
          // 准备提交预约数据

      // 确保expertUserId字段正确设置
      const submitData = {
        ...formData,
        expertUserId: expert.userId || expert.expertId, // 使用fallback
      };
      
      // 提交预约数据

      await appointmentAPI.createAppointment(submitData);
      onSuccess && onSuccess();
    } catch (err) {
      // 预约提交失败
      setError(err.message || '预约失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="appointment-form-overlay">
      <div className="appointment-form-modal">
        <div className="appointment-form-header">
          <h3>预约 {expert.name} 专家</h3>
          <button className="close-btn" onClick={onCancel}>&times;</button>
        </div>
        
        <form onSubmit={handleSubmit} className="appointment-form">
          <div className="form-group">
            <label>专家姓名：</label>
            <input 
              type="text" 
              value={expert.nickname} 
              disabled 
              className="form-control"
            />
          </div>

          <div className="form-group">
            <label>专业领域：</label>
            <input 
              type="text" 
              value={expert.specialty} 
              disabled 
              className="form-control"
            />
          </div>

          <div className="form-group">
            <label>请选择预约日期和时段：<span className="required">*</span></label>
            <div style={{overflowX: 'auto'}}>
              <table className="appointment-slot-table" style={{borderCollapse: 'collapse', width: '100%'}}>
                <thead>
                  <tr>
                    <th style={{minWidth: 80}}></th>
                    {days.map((date, i) => (
                      <th key={i} style={{padding: 4, fontSize: 12}}>
                        {date.toLocaleDateString()}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {PERIODS.map((period, j) => (
                    <tr key={j}>
                      <td style={{fontWeight: 500, fontSize: 13}}>{PERIOD_DISPLAY[j]}</td>
                      {days.map((date, i) => {
                        // 使用详细状态信息
                        const slotStatus = detailedSlots ? detailedSlots[i][j] : (availableSlots && availableSlots[i][j] ? 0 : 2);
                        const isPast = isPastSlot(date, j);
                        const canBook = slotStatus === 0 && !isPast; // 只有状态为0（空闲）且不是过去时间才可预约
                        const isSelected = selectedDay === String(i) && selectedPeriod === String(j);
                        
                        // 根据状态设置样式和显示
                        let cellStyle, cellContent, cellTitle;
                        
                        if (isPast) {
                          // 过去的时间
                          cellStyle = {
                            padding: 0,
                            textAlign: 'center',
                            border: '1px solid #d1d5db',
                            minWidth: 48,
                            background: '#f9fafb',
                            color: '#9ca3af',
                            cursor: 'not-allowed'
                          };
                          cellContent = <span style={{fontSize: 12}}>过期</span>;
                          cellTitle = "已过期";
                        } else if (isSelected) {
                          // 已选择
                          cellStyle = {
                            padding: 0,
                            textAlign: 'center',
                            border: '2px solid #2563eb',
                            minWidth: 48,
                            background: '#2563eb',
                            color: '#fff',
                            cursor: 'pointer',
                            fontWeight: 700,
                            boxShadow: '0 0 0 2px #2563eb33'
                          };
                          cellContent = '✔';
                          cellTitle = "已选择";
                        } else {
                          switch (slotStatus) {
                            case 0: // 空闲 (可预约)
                              cellStyle = {
                                padding: 0,
                                textAlign: 'center',
                                border: '1.5px solid #60a5fa',
                                minWidth: 48,
                                background: '#e0f2fe',
                                color: '#222',
                                cursor: 'pointer',
                                transition: 'all 0.15s'
                              };
                              cellContent = '';
                              cellTitle = "可预约";
                              break;
                            case 1: // 用户预约
                              cellStyle = {
                                padding: 0,
                                textAlign: 'center',
                                border: '1px solid #f59e0b',
                                minWidth: 48,
                                background: '#fef3c7',
                                color: '#92400e',
                                cursor: 'not-allowed'
                              };
                              cellContent = <span style={{fontSize: 12}}>已约</span>;
                              cellTitle = "已被预约";
                              break;
                            case 2: // 专家设置不可预约
                              cellStyle = {
                                padding: 0,
                                textAlign: 'center',
                                border: '1px solid #e5e7eb',
                                minWidth: 48,
                                background: 'repeating-linear-gradient(135deg, #f3f4f6, #f3f4f6 8px, #e5e7eb 8px, #e5e7eb 16px)',
                                color: '#6b7280',
                                cursor: 'not-allowed'
                              };
                              cellContent = <span style={{fontSize: 16, fontWeight: 700}}>×</span>;
                              cellTitle = "专家不可用";
                              break;
                            default:
                              cellStyle = {
                                padding: 0,
                                textAlign: 'center',
                                border: '1px solid #e5e7eb',
                                minWidth: 48,
                                background: '#f9fafb',
                                color: '#6b7280',
                                cursor: 'not-allowed'
                              };
                              cellContent = '?';
                              cellTitle = "未知状态";
                          }
                        }
                        
                        return (
                          <td key={i}
                              style={cellStyle}
                              title={cellTitle}
                              onClick={() => canBook && (setSelectedDay(String(i)), setSelectedPeriod(String(j)))}
                              onMouseOver={e => { 
                                if (canBook && !isSelected) {
                                  e.currentTarget.style.background = '#bae6fd';
                                  e.currentTarget.style.transform = 'scale(1.05)';
                                }
                              }}
                              onMouseOut={e => { 
                                if (canBook && !isSelected) {
                                  e.currentTarget.style.background = '#e0f2fe';
                                  e.currentTarget.style.transform = 'scale(1)';
                                }
                              }}
                          >
                            {cellContent}
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            
            {/* 时间表图例 */}
            <div style={{marginTop: 12, fontSize: 12, color: '#6b7280'}}>
              <div style={{display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center'}}>
                <span style={{color: '#374151', fontWeight: 500}}>图例：</span>
                <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                  <div style={{
                    width: 16, height: 16, border: '1.5px solid #60a5fa', 
                    background: '#e0f2fe'
                  }}></div>
                  <span>可预约</span>
                </div>
                <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                  <div style={{
                    width: 16, height: 16, border: '1px solid #f59e0b', 
                    background: '#fef3c7', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 8, color: '#92400e'
                  }}>约</div>
                  <span>已被预约</span>
                </div>
                <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                  <div style={{
                    width: 16, height: 16, border: '1px solid #e5e7eb',
                    background: 'repeating-linear-gradient(135deg, #f3f4f6, #f3f4f6 4px, #e5e7eb 4px, #e5e7eb 8px)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 10, color: '#6b7280'
                  }}>×</div>
                  <span>专家不可用</span>
                </div>
                <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                  <div style={{
                    width: 16, height: 16, border: '1px solid #d1d5db', 
                    background: '#f9fafb', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 7, color: '#9ca3af'
                  }}>过</div>
                  <span>已过期</span>
                </div>
              </div>
            </div>
            
            {selectedDay !== '' && selectedPeriod !== '' && (
              <div style={{marginTop: 8, color: '#2563eb', fontWeight: 500}}>
                已选择：
                {(() => {
                  const date = days[selectedDay];
                  const hour = PERIOD_HOURS[selectedPeriod];
                  const minute = 0;
                  const start = `${date.getFullYear()}/${(date.getMonth()+1).toString().padStart(2,'0')}/${date.getDate().toString().padStart(2,'0')} ${PERIOD_DISPLAY[selectedPeriod]}`;
                  // 55分钟制
                  const endDate = new Date(date.getTime());
                  endDate.setHours(Number(hour), Number(minute) + 55, 0, 0);
                  const endHour = endDate.getHours().toString().padStart(2, '0');
                  const endMinute = endDate.getMinutes().toString().padStart(2, '0');
                  const end = `${endHour}:${endMinute}`;
                  return `${start}-${end}`;
                })()}
              </div>
            )}
          </div>

          {/* 删除预约时长选择栏 */}

          <div className="form-group">
            <label>联系方式：<span className="required">*</span></label>
            <input 
              type="text" 
              name="contactInfo"
              value={formData.contactInfo}
              onChange={handleInputChange}
              placeholder="请输入您的邮箱或电话"
              className="form-control"
              required
            />
          </div>

          <div className="form-group">
            <label>预约描述：<span className="required">*</span></label>
            <textarea 
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              placeholder="请简要描述您需要咨询的问题或主题"
              className="form-control"
              rows="4"
              required
            />
          </div>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="form-actions">
            <button 
              type="button" 
              onClick={onCancel}
              className="btn btn-secondary"
              disabled={loading}
            >
              取消
            </button>
            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? '提交中...' : '确认预约'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
} 