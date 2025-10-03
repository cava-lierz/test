import React, { useState, useEffect, useCallback } from "react";
import { appointmentAPI } from "../services/api";
import { useAuth } from "../context/AuthContext";

export default function MyAppointmentsPage() {
  const { user, isLoading } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showRatingForm, setShowRatingForm] = useState(null);
  const [ratingData, setRatingData] = useState({ rating: 5, userRating: "" });

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await appointmentAPI.getMyAppointments(currentPage, 10);
      setAppointments(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      setError("获取预约列表失败");
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    if (!user || isLoading) return;
    fetchAppointments();
  }, [user, isLoading, fetchAppointments]);

  const handleCancelAppointment = async (appointmentId) => {
    window.showConfirm &&
      window.showConfirm("确定要取消这个预约吗？", async () => {
        try {
          await appointmentAPI.cancelAppointment(appointmentId);
          fetchAppointments(); // 刷新列表
        } catch (err) {
          window.showToast &&
            window.showToast("取消预约失败：" + err.message, "error");
        }
      });
  };

  const handleRateAppointment = async (appointmentId) => {
    try {
      await appointmentAPI.rateAppointment(
        appointmentId,
        ratingData.userRating,
        ratingData.rating
      );
      setShowRatingForm(null);
      setRatingData({ rating: 5, userRating: "" });
      fetchAppointments(); // 刷新列表
    } catch (err) {
      window.showToast && window.showToast("评价失败：" + err.message, "error");
    }
  };

  const getStatusText = (status) => {
    const statusMap = {
      pending: "待确认",
      confirmed: "已确认",
      cancelled: "已取消",
      completed: "已完成",
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      pending: "#ff9800",
      confirmed: "#2196f3",
      cancelled: "#f44336",
      completed: "#4caf50",
    };
    return colorMap[status] || "#666";
  };

  const formatDateTime = (dateTimeStr) => {
    const date = new Date(dateTimeStr);
    return date.toLocaleString("zh-CN");
  };

  if (loading) {
    return <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>;
  }

  if (error) {
    return (
      <div style={{ color: "red", textAlign: "center", padding: 32 }}>
        {error}
      </div>
    );
  }

  return (
    <div className="my-appointments-page">
      <div className="profile-header">
        <div className="profile-header-bg"></div>
        <div className="profile-header-content">
          <div className="profile-header-main">
            <div className="profile-info">
              <h1 className="profile-name">我的预约</h1>
              <p className="profile-email">查看和管理您的专家预约</p>
            </div>
          </div>
        </div>
      </div>

      {appointments.length === 0 ? (
        <div style={{ textAlign: "center", padding: 32 }}>
          <p>暂无预约记录</p>
          <a href="/expert-appointment" className="btn btn-primary">
            立即预约
          </a>
        </div>
      ) : (
        <div className="appointments-list">
          {appointments.map((appointment) => (
            <div key={appointment.id} className="appointment-item">
              <div className="appointment-header">
                <div className="appointment-info">
                  <h3>{appointment.expertName}</h3>
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
                    <strong>专家回复：</strong>
                    {appointment.expertReply}
                  </p>
                )}
                {appointment.userRating && (
                  <p>
                    <strong>我的评价：</strong>
                    {appointment.userRating}
                  </p>
                )}
                {appointment.rating && (
                  <p>
                    <strong>评分：</strong>
                    {"⭐".repeat(appointment.rating)}
                  </p>
                )}
              </div>

              <div className="appointment-actions">
                {appointment.status === "pending" && (
                  <button
                    className="btn btn-danger"
                    onClick={() => handleCancelAppointment(appointment.id)}
                  >
                    取消预约
                  </button>
                )}

                {appointment.status === "confirmed" && (
                  <button
                    className="btn btn-danger"
                    onClick={() => {
                      if (
                        window.showConfirm &&
                        window.showConfirm("确定要取消这个已确认的预约吗？")
                      ) {
                        handleCancelAppointment(appointment.id);
                      }
                    }}
                  >
                    取消预约
                  </button>
                )}

                {appointment.status === "completed" &&
                  !appointment.userRating && (
                    <button
                      className="btn btn-primary"
                      onClick={() => setShowRatingForm(appointment.id)}
                    >
                      评价预约
                    </button>
                  )}
              </div>

              {/* 评价表单 */}
              {showRatingForm === appointment.id && (
                <div className="rating-form">
                  <h4>评价预约</h4>
                  <div className="form-group">
                    <label>评分：</label>
                    <select
                      value={ratingData.rating}
                      onChange={(e) =>
                        setRatingData((prev) => ({
                          ...prev,
                          rating: parseInt(e.target.value),
                        }))
                      }
                      className="form-control"
                    >
                      <option value={5}>⭐⭐⭐⭐⭐ 非常满意</option>
                      <option value={4}>⭐⭐⭐⭐ 满意</option>
                      <option value={3}>⭐⭐⭐ 一般</option>
                      <option value={2}>⭐⭐ 不满意</option>
                      <option value={1}>⭐ 非常不满意</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>评价内容：</label>
                    <textarea
                      value={ratingData.userRating}
                      onChange={(e) =>
                        setRatingData((prev) => ({
                          ...prev,
                          userRating: e.target.value,
                        }))
                      }
                      className="form-control"
                      rows="3"
                      placeholder="请分享您的咨询体验..."
                    />
                  </div>
                  <div className="form-actions">
                    <button
                      className="btn btn-secondary"
                      onClick={() => setShowRatingForm(null)}
                    >
                      取消
                    </button>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleRateAppointment(appointment.id)}
                    >
                      提交评价
                    </button>
                  </div>
                </div>
              )}
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
  );
}
