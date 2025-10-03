import React, { useState, useEffect } from "react";
import { expertAPI, expertScheduleAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";
import AppointmentForm from "../components/AppointmentForm";

export default function ExpertAppointmentPage() {
  const { user, isLoading } = useAuth();
  const [experts, setExperts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAppointmentForm, setShowAppointmentForm] = useState(false);
  const [selectedExpert, setSelectedExpert] = useState(null);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [availableSlots, setAvailableSlots] = useState(null);
  const [detailedSlots, setDetailedSlots] = useState(null);

  // 只在组件挂载时执行一次
  useEffect(() => {
    if (hasLoaded) return;

    const fetchExperts = async () => {
      if (!user || isLoading) {
        return;
      }

      // 开始获取专家列表
      setLoading(true);
      setError(null);
      setHasLoaded(true);

      try {
        const data = await expertAPI.getExpertUsers();
        // 获取专家用户列表成功
        setExperts(Array.isArray(data) ? data : data.content || []);
      } catch (err) {
        // 获取专家列表失败
        setError("获取专家列表失败");
      } finally {
        console.log("ExpertAppointmentPage - 设置loading为false");
        setLoading(false);
      }
    };

    fetchExperts();
  }, [hasLoaded, user, isLoading]);

  const handleAppointmentClick = async (expert) => {
    setSelectedExpert(expert);
    setShowAppointmentForm(true);
    // 获取详细时间表状态（包含预约占用信息）
    try {
      console.log("获取专家详细时间表状态 - expertUserId:", expert.userId);
      const detailedSlots = await expertScheduleAPI.getDetailedSlotsByUserId(
        expert.userId
      );
      console.log("获取到详细时间表状态:", detailedSlots);
      setDetailedSlots(detailedSlots);
      // 为了向后兼容，也生成简单的可预约状态
      const simpleSlots = detailedSlots.map(
        (daySlots) => daySlots.map((status) => status === 0) // 只有状态为0（空闲）才可预约
      );
      setAvailableSlots(simpleSlots);
    } catch (e) {
      console.error("获取详细时间表状态失败:", e);
      // 如果详细状态获取失败，尝试获取简单状态
      try {
        console.log("尝试获取简单时间表状态...");
        const simpleSlots = await expertScheduleAPI.getAvailableSlotsByUserId(
          expert.userId
        );
        setAvailableSlots(simpleSlots);
        // 生成对应的详细状态（只有可用/不可用）
        const fallbackDetailedSlots = simpleSlots.map(
          (daySlots) => daySlots.map((available) => (available ? 0 : 2)) // 可用=0(空闲), 不可用=2(专家设置不可预约)
        );
        setDetailedSlots(fallbackDetailedSlots);
      } catch (fallbackError) {
        console.error("获取简单时间表状态也失败:", fallbackError);
        setAvailableSlots(null);
        setDetailedSlots(null);
      }
    }
  };

  const handleAppointmentSuccess = () => {
    setShowAppointmentForm(false);
    setSelectedExpert(null);
    window.showToast &&
      window.showToast("预约提交成功！专家会尽快与您联系确认。", "success");
  };

  const handleAppointmentCancel = () => {
    setShowAppointmentForm(false);
    setSelectedExpert(null);
  };

  return (
    <div className="expert-appointment-page">
      <div className="profile-header">
        <div className="profile-header-bg"></div>
        <div className="profile-header-content">
          <div className="profile-header-main">
            <div className="profile-info">
              <h1 className="profile-name">心理专家预约</h1>
              <p className="profile-email">
                选择专业心理咨询师，获得专业心理支持
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 快速导航 */}
      <div
        className="quick-nav"
        style={{ padding: "20px", textAlign: "center" }}
      >
        <a
          href="/my-appointments"
          className="btn btn-secondary"
          style={{ marginRight: "10px" }}
        >
          我的预约
        </a>
        <span style={{ color: "#666" }}>查看您的预约记录和状态</span>
      </div>

      {loading ? (
        <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>
      ) : error ? (
        <div style={{ color: "red", textAlign: "center", padding: 32 }}>
          {error}
        </div>
      ) : experts.length === 0 ? (
        <div style={{ textAlign: "center", padding: 32 }}>暂无专家信息</div>
      ) : (
        <div className="expert-list">
          {experts.map((expert) => (
            <div key={expert.userId} className="user-expert-item">
              <div className="user-expert-header">
                <div className="user-expert-info">
                  <span className="user-expert-name">{expert.expertName}</span>
                  <span className="user-expert-specialty">
                    {expert.specialty}
                  </span>
                </div>
                <span
                  className="user-expert-status"
                  style={{
                    color: expert.status === "online" ? "#4caf50" : "#f44336",
                  }}
                >
                  {expert.status === "online" ? "🟢 在线" : "🔴 离线"}
                </span>
              </div>
              <div className="user-expert-contact">
                <p>
                  <strong>联系方式：</strong>
                  {expert.contact || "暂无"}
                </p>
              </div>
              <div className="user-expert-actions">
                <button
                  className="user-action-btn user-action-btn-appointment"
                  onClick={() => handleAppointmentClick(expert)}
                >
                  预约咨询
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 预约表单弹窗 */}
      {showAppointmentForm && selectedExpert && (
        <AppointmentForm
          expert={selectedExpert}
          availableSlots={availableSlots}
          detailedSlots={detailedSlots}
          onSuccess={handleAppointmentSuccess}
          onCancel={handleAppointmentCancel}
        />
      )}
    </div>
  );
}
