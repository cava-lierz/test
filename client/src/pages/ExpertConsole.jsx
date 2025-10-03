import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { expertAPI, appointmentAPI, expertScheduleAPI } from "../services/api";
import GenericModal from "../components/GenericModal";
import { getUserAvatarUrl } from "../utils/avatarUtils";
import { AUTH_CONFIG } from "../utils/constants";

export default function ExpertConsole() {
  const { user, handleLogout, showConfirm, isLoading, updateUser } = useAuth();
  const [activeTab, setActiveTab] = useState("appointments");

  // 时间段定义（与后端保持一致）
  const PERIODS = [
    "08:00-09:00",
    "09:00-10:00",
    "10:00-11:00",
    "11:00-12:00",
    "14:00-15:00",
    "15:00-16:00",
    "16:00-17:00",
    "17:00-18:00",
  ];

  // 对应后端的时间点（用于时间计算）
  const PERIOD_HOURS = [8, 9, 10, 11, 14, 15, 16, 17];

  // 判断某个时间段是否已过期（参考用户端逻辑）
  const isPastSlot = (dayOffset, periodIdx) => {
    const now = new Date();
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + dayOffset);

    // 添加调试信息
    if (dayOffset === 0 && periodIdx === 0) {
      console.log("时间段判断调试:", {
        now: now.toISOString(),
        targetDate: targetDate.toISOString(),
        dayOffset,
        periodIdx,
        hour: PERIOD_HOURS[periodIdx],
      });
    }

    // 今天0点
    const todayStart = new Date(
      now.getFullYear(),
      now.getMonth(),
      now.getDate()
    );
    if (targetDate < todayStart) return true;

    // 如果是今天，且时段早于当前小时
    if (
      targetDate.getFullYear() === now.getFullYear() &&
      targetDate.getMonth() === now.getMonth() &&
      targetDate.getDate() === now.getDate()
    ) {
      const hour = PERIOD_HOURS[periodIdx];
      if (hour <= now.getHours()) return true;
    }
    return false;
  };
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [scheduleData, setScheduleData] = useState(null);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [detailedScheduleData, setDetailedScheduleData] = useState(null);
  const [showScheduleManagement, setShowScheduleManagement] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [replyText, setReplyText] = useState("");
  const [pendingAction, setPendingAction] = useState(null); // 'confirm', 'reject', 'reply'
  const [editingSchedule, setEditingSchedule] = useState(null); // 编辑中的排班状态
  const [isEditingMode, setIsEditingMode] = useState(false); // 是否在编辑模式

  // 专家个人信息相关状态
  const [expertProfile, setExpertProfile] = useState(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [showEditProfile, setShowEditProfile] = useState(false);
  const [editProfileData, setEditProfileData] = useState({
    name: "",
    specialty: "",
    contact: "",
    status: "online",
  });

  // 刷新用户信息（包括头像）
  const refreshUserInfo = useCallback(async () => {
    try {
      if (!user?.id) return;

      // 从localStorage获取最新的用户信息
      const userInfo = localStorage.getItem(
        `${AUTH_CONFIG.USER_INFO_KEY}_${user.id}`
      );
      if (userInfo) {
        const parsedUser = JSON.parse(userInfo);
        // 检查头像是否有变化
        if (parsedUser.avatar !== user.avatar) {
          // 检测到头像变化，更新用户信息
          updateUser(parsedUser);
        }
      }
    } catch (error) {
      // 刷新用户信息失败
    }
  }, [user?.id, user?.avatar, updateUser]);

  const fetchExpertProfile = useCallback(async () => {
    setProfileLoading(true);
    try {
      const profile = await expertAPI.getMyProfile();
      setExpertProfile(profile);
      setEditProfileData({
        name: profile.name || "",
        specialty: profile.specialty || "",
        contact: profile.contact || "",
        status: profile.status || "online",
      });
    } catch (error) {
      console.error("获取专家信息失败:", error);
    } finally {
      setProfileLoading(false);
    }
  }, []);

  // 获取专家个人信息
  useEffect(() => {
    if (!user || isLoading) return;
    fetchExpertProfile();
  }, [user, isLoading, fetchExpertProfile]);

  // 监听用户信息变化（包括头像），自动刷新显示
  useEffect(() => {
    // 当user对象中的avatar字段发生变化时，组件会自动重新渲染
    // 这确保了头像的实时同步
  }, [user?.avatar]);

  // 监听页面可见性变化，当页面重新可见时刷新用户信息
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === "visible" && user) {
        refreshUserInfo();
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [user, refreshUserInfo]);

  // 监听头像更新事件
  useEffect(() => {
    const handleAvatarUpdate = (event) => {
      // 收到头像更新事件
      // 直接使用事件中的更新用户信息
      if (event.detail && event.detail.user) {
        updateUser(event.detail.user);
      } else {
        refreshUserInfo();
      }
    };

    // 监听自定义头像更新事件
    window.addEventListener("avatar-updated", handleAvatarUpdate);

    return () => {
      window.removeEventListener("avatar-updated", handleAvatarUpdate);
    };
  }, [refreshUserInfo, updateUser]);

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    try {
      const response = await appointmentAPI.getExpertAppointments(
        currentPage,
        10
      );
      setAppointments(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      console.error("获取预约列表失败:", error);
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  // 获取专家预约列表
  useEffect(() => {
    if (!user || isLoading) return;
    fetchAppointments();
  }, [user, isLoading, currentPage, fetchAppointments]);

  const fetchSchedule = useCallback(async () => {
    setScheduleLoading(true);
    try {
      const schedule = await expertScheduleAPI.getMyExpertSchedule();
      setScheduleData(schedule);

      // 同时获取详细的排班状态
      if (user && user.id) {
        try {
          console.log("获取专家详细排班状态 - userId:", user.id);
          const detailedSlots =
            await expertScheduleAPI.getDetailedSlotsByUserId(user.id);
          console.log("获取到详细排班状态:", detailedSlots);
          console.log(
            "状态定义: 0=空闲(可预约), 1=用户预约(已被预约), 2=专家设置不可预约"
          );

          // 打印第一天的状态作为示例
          if (detailedSlots && detailedSlots.length > 0) {
            console.log("第一天的状态示例:", detailedSlots[0]);
          }

          setDetailedScheduleData(detailedSlots);
        } catch (detailError) {
          console.error("获取详细排班状态失败:", detailError);
          setDetailedScheduleData(null);
        }
      }
    } catch (error) {
      console.error("获取排班信息失败:", error);
      setScheduleData(null);
      setDetailedScheduleData(null);
    } finally {
      setScheduleLoading(false);
    }
  }, [user]);

  // 获取专家排班信息
  useEffect(() => {
    if (activeTab === "schedule" && user && !isLoading) {
      fetchSchedule();
    }
  }, [activeTab, user, isLoading, fetchSchedule]);

  // 处理预约
  const handleAppointmentAction = async (appointmentId, action, reply) => {
    try {
      if (action === "confirm") {
        await appointmentAPI.confirmAppointment(appointmentId, reply);
      } else if (action === "reject") {
        await appointmentAPI.rejectAppointment(appointmentId, reply);
      } else if (action === "complete") {
        await appointmentAPI.completeAppointment(appointmentId);
      } else if (action === "cancel") {
        await appointmentAPI.cancelAppointment(appointmentId);
      }
      fetchAppointments();

      // 如果操作涉及状态变更（拒绝、取消），重新获取时间表数据
      if (action === "reject" || action === "cancel") {
        console.log("预约状态变更，重新获取时间表数据");
        await fetchSchedule();
      }

      setSelectedAppointment(null);
      setReplyText("");
      setPendingAction(null);
    } catch (error) {
      window.showToast &&
        window.showToast("操作失败：" + error.message, "error");
    }
  };

  // 发送回复
  const handleSendReply = async (appointmentId, reply) => {
    try {
      // 对于纯回复功能，我们只更新回复内容，不改变状态
      // 可以使用现有的confirm API，后端需要支持只更新回复而不改变状态
      await appointmentAPI.confirmAppointment(appointmentId, reply);
      fetchAppointments();
      setSelectedAppointment(null);
      setReplyText("");
      setPendingAction(null);
    } catch (error) {
      window.showToast &&
        window.showToast("发送回复失败：" + error.message, "error");
    }
  };

  // 关闭模态框
  const closeModal = () => {
    setSelectedAppointment(null);
    setReplyText("");
    setPendingAction(null);
  };

  // 更新排班（立即保存）
  const handleScheduleUpdate = async (dayOffset, periodIndex, available) => {
    try {
      await expertScheduleAPI.updateMySchedule(
        dayOffset,
        periodIndex,
        available
      );
      fetchSchedule();
    } catch (error) {
      window.showToast &&
        window.showToast("更新排班失败：" + error.message, "error");
    }
  };

  // 开始编辑排班（直接从详细状态开始）
  const startEditingSchedule = () => {
    if (detailedScheduleData) {
      // 从详细状态数据创建可编辑的排班数据
      // 保持与detailedScheduleData相同的格式：[dayIndex][slotIndex]
      const editableSchedule = Array.from({ length: 14 }, (_, dayIndex) =>
        Array.from({ length: 8 }, (_, slotIndex) => {
          const status = detailedScheduleData[dayIndex][slotIndex];
          return status;
        })
      );
      setEditingSchedule(editableSchedule);
      setIsEditingMode(true);
    }
  };

  // 在编辑模式下更新排班（允许在空闲和不可预约之间切换）
  const handleEditingScheduleUpdate = (dayOffset, periodIndex) => {
    if (editingSchedule) {
      const currentStatus = editingSchedule[dayOffset][periodIndex];
      // 允许在状态0（空闲/可预约）和状态2（专家设置不可预约）之间切换
      if (currentStatus === 0) {
        // 从空闲改为不可预约
        const newSchedule = editingSchedule.map((row) => [...row]);
        newSchedule[dayOffset][periodIndex] = 2;
        setEditingSchedule(newSchedule);
      } else if (currentStatus === 2) {
        // 从不可预约改为空闲
        const newSchedule = editingSchedule.map((row) => [...row]);
        newSchedule[dayOffset][periodIndex] = 0;
        setEditingSchedule(newSchedule);
      }
      // 状态1（已被预约）不允许修改
    }
  };

  // 保存编辑的排班
  const saveEditingSchedule = async () => {
    if (!editingSchedule || !detailedScheduleData) return;

    try {
      setScheduleLoading(true);

      // 找出变更的时间段并准备批量更新请求
      const updates = [];
      for (let dayOffset = 0; dayOffset < editingSchedule.length; dayOffset++) {
        for (
          let periodIndex = 0;
          periodIndex < editingSchedule[dayOffset].length;
          periodIndex++
        ) {
          const newStatus = editingSchedule[dayOffset][periodIndex];
          const oldStatus = detailedScheduleData[dayOffset][periodIndex];

          // 处理状态变更：0->2（空闲变不可预约）或 2->0（不可预约变空闲）
          if ((oldStatus === 0 && newStatus === 2) || (oldStatus === 2 && newStatus === 0)) {
            updates.push({
              dayOffset: dayOffset,
              periodIndex: periodIndex,
              available: newStatus === 0, // 0表示可预约，2表示不可预约
            });
          }
        }
      }

      // 使用批量更新接口
      if (updates.length > 0) {
        const result = await expertScheduleAPI.batchUpdateMySchedule(updates);
        console.log("批量更新结果:", result);
      }

      // 重新获取数据
      await fetchSchedule();

      // 退出编辑模式并回到初始状态
      setIsEditingMode(false);
      setEditingSchedule(null);
      setShowScheduleManagement(false);

      window.showToast &&
        window.showToast(
          `排班设置保存成功！更新了${updates.length}个时间段。`,
          "success"
        );
    } catch (error) {
      window.showToast &&
        window.showToast("保存排班失败：" + error.message, "error");
    } finally {
      setScheduleLoading(false);
    }
  };

  // 取消编辑排班
  const cancelEditingSchedule = () => {
    setIsEditingMode(false);
    setEditingSchedule(null);
  };

  // 更新专家个人信息
  const handleUpdateProfile = async () => {
    try {
      const updatedProfile = await expertAPI.updateMyProfile(editProfileData);
      setExpertProfile(updatedProfile);
      setShowEditProfile(false);
      window.showToast && window.showToast("个人信息更新成功！", "success");
    } catch (error) {
      window.showToast &&
        window.showToast("更新失败：" + error.message, "error");
    }
  };

  const getStatusText = (status) => {
    const statusMap = {
      pending: "待确认",
      confirmed: "已确认",
      rejected: "已拒绝",
      cancelled: "已取消",
      completed: "已完成",
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      pending: "#ff9800",
      confirmed: "#2196f3",
      rejected: "#f44336",
      cancelled: "#9e9e9e",
      completed: "#4caf50",
    };
    return colorMap[status] || "#666";
  };

  const getExpertStatusText = (status) => {
    const statusMap = {
      online: "在线",
      offline: "离线",
      busy: "忙碌",
    };
    return statusMap[status] || status;
  };

  const getExpertStatusColor = (status) => {
    const colorMap = {
      online: "#4caf50",
      offline: "#9e9e9e",
      busy: "#ff9800",
    };
    return colorMap[status] || "#666";
  };

  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    return date.toLocaleString("zh-CN");
  };

  const handleLogoutClick = () => {
    showConfirm("确定要退出登录吗？", () => {
      handleLogout();
    });
  };

  const isAppointmentTimeEnded = (appointmentTime, duration) => {
    const appointmentEndTime = new Date(appointmentTime);
    appointmentEndTime.setMinutes(appointmentEndTime.getMinutes() + duration);
    return new Date() >= appointmentEndTime;
  };

  if ((loading && appointments.length === 0) || profileLoading) {
    return <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>;
  }

  return (
    <div className="expert-console">
      {/* 头部 - 显示专家详细信息 */}
      <div className="expert-header">
        <div className="expert-header-content">
          <div className="expert-info">
            <img
              src={getUserAvatarUrl(user)}
              alt="专家头像"
              className="expert-avatar"
              title="点击刷新头像"
              onClick={refreshUserInfo}
              style={{ cursor: "pointer" }}
            />
            <div className="expert-details">
              <h1>{expertProfile?.name || user?.name || "心理专家"}</h1>
              <div className="expert-meta">
                <p className="expert-specialty">
                  <strong>专业领域：</strong>
                  {expertProfile?.specialty || "暂未设置"}
                </p>
                <p className="expert-contact">
                  <strong>联系方式：</strong>
                  {expertProfile?.contact || "暂未设置"}
                </p>
                <p className="expert-status">
                  <strong>在线状态：</strong>
                  <span
                    style={{
                      color: getExpertStatusColor(expertProfile?.status),
                      fontWeight: "bold",
                    }}
                  >
                    {getExpertStatusText(expertProfile?.status)}
                  </span>
                </p>
              </div>
            </div>
          </div>
          <div className="expert-actions">
            <button
              className="btn btn-primary"
              onClick={() => setShowEditProfile(true)}
              style={{ marginRight: "10px" }}
            >
              编辑信息
            </button>
            <button className="btn btn-secondary" onClick={handleLogoutClick}>
              退出登录
            </button>
          </div>
        </div>
      </div>

      {/* 标签页 */}
      <div className="expert-tabs">
        <button
          className={`tab-button ${
            activeTab === "appointments" ? "active" : ""
          }`}
          onClick={() => setActiveTab("appointments")}
        >
          预约管理
        </button>
        <button
          className={`tab-button ${activeTab === "schedule" ? "active" : ""}`}
          onClick={() => setActiveTab("schedule")}
        >
          排班设置
        </button>
      </div>

      {/* 预约管理标签页 */}
      {activeTab === "appointments" && (
        <div className="appointments-tab">
          <div className="tab-header">
            <h2>预约管理</h2>
            <p>处理用户的心理咨询预约请求</p>
          </div>

          {appointments.length === 0 ? (
            <div style={{ textAlign: "center", padding: 32 }}>
              <p>暂无预约记录</p>
            </div>
          ) : (
            <div className="appointments-list">
              {appointments.map((appointment) => (
                <div key={appointment.id} className="appointment-item">
                  <div className="appointment-header">
                    <div className="appointment-info">
                      <h3>{appointment.userName}</h3>
                      <span
                        className="appointment-status"
                        style={{ color: getStatusColor(appointment.status) }}
                      >
                        {getStatusText(appointment.status)}
                      </span>
                    </div>
                    <div className="appointment-time">
                      <p>
                        <strong>预约时间：</strong>
                        {formatDateTime(appointment.appointmentTime)}
                      </p>
                      <p>
                        <strong>时长：</strong>
                        {appointment.duration}分钟
                      </p>
                    </div>
                  </div>

                  <div className="appointment-details">
                    <p>
                      <strong>联系方式：</strong>
                      {appointment.contactInfo}
                    </p>
                    <p>
                      <strong>预约描述：</strong>
                      {appointment.description}
                    </p>
                    {appointment.expertReply && (
                      <p>
                        <strong>我的回复：</strong>
                        {appointment.expertReply}
                      </p>
                    )}
                  </div>

                  <div className="appointment-actions">
                    {appointment.status === "pending" && (
                      <>
                        <button
                          className="btn btn-primary"
                          onClick={() => {
                            setSelectedAppointment(appointment);
                            setPendingAction("confirm");
                            setReplyText("");
                          }}
                        >
                          接受预约
                        </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => {
                            setSelectedAppointment(appointment);
                            setPendingAction("reject");
                            setReplyText("");
                          }}
                        >
                          拒绝预约
                        </button>
                      </>
                    )}

                    {appointment.status === "confirmed" && (
                      <>
                        <button
                          className="btn btn-success"
                          disabled={
                            !isAppointmentTimeEnded(
                              appointment.appointmentTime,
                              appointment.duration
                            )
                          }
                          onClick={() =>
                            handleAppointmentAction(appointment.id, "complete")
                          }
                          title={
                            !isAppointmentTimeEnded(
                              appointment.appointmentTime,
                              appointment.duration
                            )
                              ? "预约时间未结束，无法完成"
                              : ""
                          }
                        >
                          完成咨询
                        </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => {
                            if (
                              window.showConfirm &&
                              window.showConfirm(
                                "确定要取消这个预约吗？用户将收到通知，时间段将重新变为可预约状态。"
                              )
                            ) {
                              handleAppointmentAction(appointment.id, "cancel");
                            }
                          }}
                        >
                          取消预约
                        </button>
                        <button
                          className="btn btn-secondary"
                          onClick={() => {
                            setSelectedAppointment(appointment);
                            setPendingAction("reply");
                            setReplyText("");
                          }}
                        >
                          发送回复
                        </button>
                      </>
                    )}

                    {(appointment.status === "rejected" ||
                      appointment.status === "cancelled" ||
                      appointment.status === "completed") && (
                      <button
                        className="btn btn-secondary"
                        onClick={() => {
                          setSelectedAppointment(appointment);
                          setPendingAction("reply");
                          setReplyText("");
                        }}
                      >
                        发送回复
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* 分页 */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                disabled={currentPage === 0}
                onClick={() => setCurrentPage((prev) => prev - 1)}
              >
                上一页
              </button>
              <span className="page-info">
                第 {currentPage + 1} 页，共 {totalPages} 页
              </span>
              <button
                className="btn btn-secondary"
                disabled={currentPage >= totalPages - 1}
                onClick={() => setCurrentPage((prev) => prev + 1)}
              >
                下一页
              </button>
            </div>
          )}
        </div>
      )}

      {/* 排班设置标签页 */}
      {activeTab === "schedule" && (
        <div className="schedule-tab">
          <div className="tab-header">
            <h2>排班设置</h2>
            <p>管理您的可预约时间段</p>
            <div style={{ marginTop: 12 }}>
              {!isEditingMode ? (
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    setShowScheduleManagement(true);
                    startEditingSchedule();
                  }}
                  disabled={!detailedScheduleData}
                >
                  修改个人空闲时间
                </button>
              ) : (
                <button
                  className="btn btn-success"
                  onClick={saveEditingSchedule}
                  disabled={scheduleLoading}
                >
                  {scheduleLoading ? "保存中..." : "完成编辑"}
                </button>
              )}
            </div>
          </div>

          {scheduleLoading ? (
            <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>
          ) : showScheduleManagement || isEditingMode ? (
            // 时间表界面
            (
              isEditingMode
                ? editingSchedule && detailedScheduleData
                : detailedScheduleData
            ) ? (
              <div>
                <div
                  style={{
                    marginBottom: 16,
                    padding: 12,
                    background: isEditingMode ? "#fff7ed" : "#f8fafc",
                    borderRadius: 8,
                    border: `1px solid ${
                      isEditingMode ? "#f97316" : "#e2e8f0"
                    }`,
                  }}
                >
                  <h4
                    style={{
                      margin: 0,
                      marginBottom: 8,
                      color: isEditingMode ? "#ea580c" : "#334155",
                    }}
                  >
                    {isEditingMode ? "编辑个人空闲时间" : "时间表概览"}
                  </h4>
                  <p
                    style={{
                      margin: 0,
                      fontSize: 14,
                      color: isEditingMode ? "#c2410c" : "#64748b",
                    }}
                  >
                    {isEditingMode
                      ? '点击"可预约"的时间段可将其设为"不可预约"，点击"不可预约"的时间段可将其设为"可预约"。已被预约的时间段无法修改。'
                      : '查看您的完整时间表状态，包括预约占用情况。点击"修改个人空闲时间"进行编辑。'}
                  </p>
                </div>

                <div
                  style={{ marginBottom: 16, fontSize: 12, color: "#6b7280" }}
                >
                  <div
                    style={{
                      marginBottom: 8,
                      padding: 8,
                      background: "#f8fafc",
                      borderRadius: 4,
                      border: "1px solid #e2e8f0",
                    }}
                  >
                    <div
                      style={{
                        fontWeight: 500,
                        color: "#334155",
                        marginBottom: 4,
                      }}
                    >
                      状态定义说明：
                    </div>
                    <div style={{ fontSize: 11, color: "#64748b" }}>
                      状态0=空闲(可预约) | 状态1=用户预约(已被预约) |
                      状态2=专家设置不可预约
                    </div>
                  </div>
                  <div
                    style={{
                      display: "flex",
                      flexWrap: "wrap",
                      gap: "12px",
                      alignItems: "center",
                    }}
                  >
                    <span style={{ color: "#374151", fontWeight: 500 }}>
                      图例：
                    </span>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <div
                        style={{
                          width: 16,
                          height: 16,
                          background: "#dcfce7",
                          border: "1px solid #16a34a",
                          borderRadius: 2,
                        }}
                      ></div>
                      <span>
                        ✓ 状态0 -{" "}
                        {isEditingMode ? "可预约（可编辑）" : "可预约"}
                      </span>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <div
                        style={{
                          width: 16,
                          height: 16,
                          background: "#fef3c7",
                          border: "1px solid #f59e0b",
                          borderRadius: 2,
                        }}
                      ></div>
                      <span>
                        约 状态1 -{" "}
                        {isEditingMode ? "已被预约（不可编辑）" : "已被预约"}
                      </span>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <div
                        style={{
                          width: 16,
                          height: 16,
                          background: "#fecaca",
                          border: "1px solid #ef4444",
                          borderRadius: 2,
                        }}
                      ></div>
                      <span>✗ 状态2 - 您设为不可用</span>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "4px",
                      }}
                    >
                      <div
                        style={{
                          width: 16,
                          height: 16,
                          background: "#f3f4f6",
                          border: "1px solid #9ca3af",
                          borderRadius: 2,
                        }}
                      ></div>
                      <span>
                        过期 - {isEditingMode ? "已过期（不可编辑）" : "已过期"}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="schedule-grid">
                  <div className="schedule-header">
                    <div className="time-column">时间</div>
                    {Array.from({ length: 14 }, (_, i) => (
                      <div key={i} className="day-column">
                        {new Date(
                          Date.now() + i * 24 * 60 * 60 * 1000
                        ).toLocaleDateString("zh-CN", {
                          month: "short",
                          day: "numeric",
                        })}
                      </div>
                    ))}
                  </div>

                  {Array.from({ length: 8 }, (_, slotIndex) => (
                    <div key={slotIndex} className="schedule-row">
                      <div className="time-column">{PERIODS[slotIndex]}</div>
                      {Array.from({ length: 14 }, (_, dayIndex) => {
                        const status = isEditingMode
                          ? editingSchedule[dayIndex][slotIndex]
                          : detailedScheduleData[dayIndex][slotIndex];

                        // 检查是否为过期时间段
                        const isPast = isPastSlot(dayIndex, slotIndex);

                        let cellClass,
                          cellContent,
                          cellTitle,
                          isClickable = false;

                        if (isPast) {
                          // 过期时间段
                          cellClass = "schedule-cell expired";
                          cellContent = "过期";
                          cellTitle = "已过期";
                          isClickable = false;
                        } else {
                          // 根据状态处理
                          switch (status) {
                            case 0: // 空闲（可预约）
                              cellClass = `schedule-cell available-free ${
                                isEditingMode ? "editable" : ""
                              }`;
                              cellContent = "✓";
                              cellTitle = isEditingMode
                                ? "可预约 - 点击设为不可用"
                                : "可预约";
                              isClickable = isEditingMode;
                              break;
                            case 1: // 用户预约（已被预约）
                              cellClass = "schedule-cell booked";
                              cellContent = "约";
                              cellTitle = isEditingMode
                                ? "已被预约 - 无法修改"
                                : "已被预约";
                              break;
                            case 2: // 专家设置不可预约
                              cellClass = `schedule-cell unavailable-expert ${
                                isEditingMode ? "editable" : ""
                              }`;
                              cellContent = "✗";
                              cellTitle = isEditingMode
                                ? "您设为不可用 - 点击设为可预约"
                                : "您设为不可用";
                              isClickable = isEditingMode;
                              break;
                            default:
                              cellClass = "schedule-cell unknown";
                              cellContent = "?";
                              cellTitle = "未知状态";
                          }
                        }

                        return (
                          <div
                            key={dayIndex}
                            className={cellClass}
                            onClick={() =>
                              isEditingMode && isClickable
                                ? handleEditingScheduleUpdate(
                                    dayIndex,
                                    slotIndex
                                  )
                                : null
                            }
                            title={cellTitle}
                            style={{
                              cursor:
                                isEditingMode && isClickable
                                  ? "pointer"
                                  : isEditingMode
                                  ? "not-allowed"
                                  : "default",
                              opacity: 1,
                            }}
                          >
                            {cellContent}
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div style={{ textAlign: "center", padding: 32 }}>
                无法加载排班信息
              </div>
            )
          ) : // 时间表概览界面（显示预约占用状态）
          detailedScheduleData ? (
            <div>
              <div
                style={{
                  marginBottom: 16,
                  padding: 12,
                  background: "#f8fafc",
                  borderRadius: 8,
                  border: "1px solid #e2e8f0",
                }}
              >
                <h4 style={{ margin: 0, marginBottom: 8, color: "#334155" }}>
                  时间表概览
                </h4>
                <p style={{ margin: 0, fontSize: 14, color: "#64748b" }}>
                  查看您的完整时间表状态，包括预约占用情况
                </p>
              </div>

              {/* 图例 */}
              <div style={{ marginBottom: 16, fontSize: 12, color: "#6b7280" }}>
                <div
                  style={{
                    display: "flex",
                    flexWrap: "wrap",
                    gap: "12px",
                    alignItems: "center",
                  }}
                >
                  <span style={{ color: "#374151", fontWeight: 500 }}>
                    图例：
                  </span>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "4px",
                    }}
                  >
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        background: "#dcfce7",
                        border: "1px solid #16a34a",
                        borderRadius: 2,
                      }}
                    ></div>
                    <span>可预约</span>
                  </div>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "4px",
                    }}
                  >
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        background: "#fef3c7",
                        border: "1px solid #f59e0b",
                        borderRadius: 2,
                      }}
                    ></div>
                    <span>已被预约</span>
                  </div>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "4px",
                    }}
                  >
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        background: "#fecaca",
                        border: "1px solid #ef4444",
                        borderRadius: 2,
                      }}
                    ></div>
                    <span>您设为不可用</span>
                  </div>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "4px",
                    }}
                  >
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        background: "#f3f4f6",
                        border: "1px solid #9ca3af",
                        borderRadius: 2,
                      }}
                    ></div>
                    <span>已过期</span>
                  </div>
                </div>
              </div>

              <div className="schedule-grid">
                <div className="schedule-header">
                  <div className="time-column">时间</div>
                  {Array.from({ length: 14 }, (_, i) => (
                    <div key={i} className="day-column">
                      {new Date(
                        Date.now() + i * 24 * 60 * 60 * 1000
                      ).toLocaleDateString("zh-CN", {
                        month: "short",
                        day: "numeric",
                      })}
                    </div>
                  ))}
                </div>

                {Array.from({ length: 8 }, (_, slotIndex) => (
                  <div key={slotIndex} className="schedule-row">
                    <div className="time-column">{PERIODS[slotIndex]}</div>
                    {Array.from({ length: 14 }, (_, dayIndex) => {
                      const status = detailedScheduleData[dayIndex][slotIndex];
                      // 检查是否为过期时间段
                      const isPast = isPastSlot(dayIndex, slotIndex);

                      let cellClass, cellContent, cellTitle;

                      if (isPast) {
                        // 过期时间段
                        cellClass = "schedule-cell expired";
                        cellContent = "过期";
                        cellTitle = "已过期";
                      } else {
                        // 根据状态处理
                        switch (status) {
                          case 0: // 空闲（可预约）
                            cellClass = "schedule-cell available-free";
                            cellContent = "✓";
                            cellTitle = "可预约";
                            break;
                          case 1: // 用户预约（已被预约）
                            cellClass = "schedule-cell booked";
                            cellContent = "约";
                            cellTitle = "已被预约";
                            break;
                          case 2: // 专家设置不可预约
                            cellClass = "schedule-cell unavailable-expert";
                            cellContent = "✗";
                            cellTitle = "您设为不可用";
                            break;
                          default:
                            cellClass = "schedule-cell unknown";
                            cellContent = "?";
                            cellTitle = "未知状态";
                        }
                      }

                      return (
                        <div
                          key={dayIndex}
                          className={cellClass}
                          title={cellTitle}
                        >
                          {cellContent}
                        </div>
                      );
                    })}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div style={{ textAlign: "center", padding: 32 }}>
              无法加载时间表数据
            </div>
          )}
        </div>
      )}

      {/* 编辑个人信息弹窗 */}
      {showEditProfile && (
        <GenericModal
          isOpen={showEditProfile}
          onClose={() => setShowEditProfile(false)}
          title="编辑个人信息"
        >
          <div className="edit-profile-form">
            <div className="form-group">
              <label>专家姓名：</label>
              <input
                type="text"
                value={editProfileData.name}
                onChange={(e) =>
                  setEditProfileData({
                    ...editProfileData,
                    name: e.target.value,
                  })
                }
                className="form-control"
                placeholder="请输入您的姓名"
              />
            </div>

            <div className="form-group">
              <label>专业领域：</label>
              <input
                type="text"
                value={editProfileData.specialty}
                onChange={(e) =>
                  setEditProfileData({
                    ...editProfileData,
                    specialty: e.target.value,
                  })
                }
                className="form-control"
                placeholder="请输入您的专业领域"
              />
            </div>

            <div className="form-group">
              <label>联系方式：</label>
              <input
                type="text"
                value={editProfileData.contact}
                onChange={(e) =>
                  setEditProfileData({
                    ...editProfileData,
                    contact: e.target.value,
                  })
                }
                className="form-control"
                placeholder="请输入您的联系方式"
              />
            </div>

            <div className="form-group">
              <label>在线状态：</label>
              <select
                value={editProfileData.status}
                onChange={(e) =>
                  setEditProfileData({
                    ...editProfileData,
                    status: e.target.value,
                  })
                }
                className="form-control"
              >
                <option value="online">在线</option>
                <option value="offline">离线</option>
                <option value="busy">忙碌</option>
              </select>
            </div>

            <div className="form-actions">
              <button
                className="btn btn-secondary"
                onClick={() => setShowEditProfile(false)}
              >
                取消
              </button>
              <button className="btn btn-primary" onClick={handleUpdateProfile}>
                保存
              </button>
            </div>
          </div>
        </GenericModal>
      )}

      {/* 回复预约弹窗 */}
      {selectedAppointment && (
        <GenericModal
          isOpen={!!selectedAppointment}
          onClose={closeModal}
          title={
            pendingAction === "confirm"
              ? "接受预约"
              : pendingAction === "reject"
              ? "拒绝预约"
              : "发送回复"
          }
        >
          <div className="reply-form">
            <div className="appointment-info">
              <p>
                <strong>预约用户：</strong>
                {selectedAppointment.userName}
              </p>
              <p>
                <strong>预约时间：</strong>
                {formatDateTime(selectedAppointment.appointmentTime)}
              </p>
              <p>
                <strong>预约状态：</strong>
                <span
                  style={{ color: getStatusColor(selectedAppointment.status) }}
                >
                  {getStatusText(selectedAppointment.status)}
                </span>
              </p>
            </div>
            <div className="form-group">
              <label>
                {pendingAction === "confirm"
                  ? "接受预约回复："
                  : pendingAction === "reject"
                  ? "拒绝原因："
                  : "回复内容："}
              </label>
              <textarea
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                className="form-control"
                rows="4"
                placeholder={
                  pendingAction === "confirm"
                    ? "请输入接受预约的回复..."
                    : pendingAction === "reject"
                    ? "请输入拒绝原因..."
                    : "请输入回复内容..."
                }
              />
            </div>
            <div className="form-actions">
              <button className="btn btn-secondary" onClick={closeModal}>
                取消
              </button>
              <button
                className="btn btn-primary"
                onClick={() => {
                  if (
                    pendingAction === "confirm" ||
                    pendingAction === "reject"
                  ) {
                    handleAppointmentAction(
                      selectedAppointment.id,
                      pendingAction,
                      replyText
                    );
                  } else {
                    handleSendReply(selectedAppointment.id, replyText);
                  }
                }}
              >
                发送
              </button>
            </div>
          </div>
        </GenericModal>
      )}
    </div>
  );
}
