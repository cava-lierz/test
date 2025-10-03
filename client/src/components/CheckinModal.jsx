import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import GlassCard from "./GlassCard";
import AnimatedButton from "./AnimatedButton";
import MoodChart from "./MoodChart";
import { moodAPI } from "../services/api";

export default function CheckinModal({ isOpen, onClose, onCheckin }) {
  const { user } = useAuth();
  const [rating, setRating] = useState(0);
  const [note, setNote] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [weekData, setWeekData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 获取本周心情数据
  useEffect(() => {
    const fetchWeekData = async () => {
      if (isOpen) {
        setLoading(true);
        try {
          const response = await moodAPI.getCurrentWeek();
          setWeekData(response || []);
        } catch (error) {
          console.error("获取心情数据失败:", error);
          setWeekData([]);
        } finally {
          setLoading(false);
        }
      } else {
        // 关闭弹窗时重置表单
        setRating(0);
        setNote("");
      }
    };

    fetchWeekData();
  }, [isOpen]);

  const handleSubmit = async () => {
    if (rating === 0) {
      window.showToast && window.showToast("请选择今天的心情评分");
      return;
    }

    setIsSubmitting(true);

    try {
      const checkinData = {
        rating: rating,
        note: note || null,
      };

      await moodAPI.checkin(checkinData);

      // 打卡成功后回调，传递本地的打卡数据
      onCheckin &&
        onCheckin({
          rating: rating,
          note: note,
        });

      // 重新获取本周数据
      const updatedWeekData = await moodAPI.getCurrentWeek();
      setWeekData(updatedWeekData || []);

      // 重置表单
      setRating(0);
      setNote("");

      onClose();
    } catch (error) {
      console.error("心情打卡失败:", error);
      window.showToast && window.showToast("打卡失败，请稍后重试");
    } finally {
      setIsSubmitting(false);
    }
  };

  const getRatingEmoji = (rating) => {
    const emojis = ["😢", "😔", "😐", "😊", "😄"];
    return emojis[rating - 1] || "😐";
  };

  const getRatingText = (rating) => {
    const texts = ["很差", "不好", "一般", "不错", "很好"];
    return texts[rating - 1] || "请选择";
  };

  const getRatingColor = (rating) => {
    const colors = ["#ff6b6b", "#ffa726", "#ffd54f", "#81c784", "#4caf50"];
    return colors[rating - 1] || "#e0e0e0";
  };

  if (!isOpen) return null;

  return (
    <div className="checkin-modal-overlay">
      <GlassCard className="checkin-modal-card">
        <div className="checkin-modal-content">
          {/* 标题 */}
          <div className="checkin-modal-header">
            <h2 className="checkin-modal-title">🌟 今日心情打卡</h2>
            <p className="checkin-modal-subtitle">
              记录一下今天的心情吧，{user.nickname || user.username}！
            </p>
          </div>

          {/* 心情评分 */}
          <div className="checkin-rating-section">
            <label className="checkin-rating-label">今天的心情如何？</label>
            <div className="checkin-rating-buttons">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setRating(star)}
                  className={`checkin-rating-button ${
                    rating === star ? "active" : ""
                  }`}
                  style={{
                    borderColor:
                      rating === star ? getRatingColor(star) : "#e0e0e0",
                    background:
                      rating === star
                        ? `${getRatingColor(star)}15`
                        : "transparent",
                  }}
                  onMouseOver={(e) => {
                    if (rating !== star) {
                      e.target.style.borderColor = getRatingColor(star);
                      e.target.style.background = `${getRatingColor(star)}10`;
                    }
                  }}
                  onMouseOut={(e) => {
                    if (rating !== star) {
                      e.target.style.borderColor = "#e0e0e0";
                      e.target.style.background = "transparent";
                    }
                  }}
                >
                  <span className="checkin-rating-star">⭐</span>
                  <span
                    className="checkin-rating-text"
                    style={{
                      color: rating === star ? getRatingColor(star) : "#666",
                      fontWeight: rating === star ? "600" : "400",
                    }}
                  >
                    {getRatingText(star)}
                  </span>
                </button>
              ))}
            </div>
            {rating > 0 && (
              <div
                className="checkin-rating-preview"
                style={{
                  background: `${getRatingColor(rating)}15`,
                  border: `1px solid ${getRatingColor(rating)}30`,
                }}
              >
                <span className="checkin-rating-preview-emoji">
                  {getRatingEmoji(rating)}
                </span>
                <span
                  className="checkin-rating-preview-text"
                  style={{ color: getRatingColor(rating) }}
                >
                  {getRatingText(rating)}
                </span>
              </div>
            )}
          </div>

          {/* 心情文字 */}
          <div className="checkin-text-section">
            <label className="checkin-text-label">想说的话（可选）</label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="分享今天的心情或想法..."
              className="checkin-text-input"
              onFocus={(e) => {
                e.target.style.borderColor = "#667eea";
                e.target.style.boxShadow = "0 0 0 3px rgba(102, 126, 234, 0.1)";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "#e2e8f0";
                e.target.style.boxShadow = "none";
              }}
            />
          </div>

          {/* 心情趋势图 */}
          <div className="checkin-chart-section">
            {loading ? (
              <div className="checkin-chart-loading">
                <div className="loading-spinner"></div>
                <p>加载心情数据中...</p>
              </div>
            ) : (
              <MoodChart weekData={weekData} />
            )}
          </div>

          {/* 按钮 */}
          <div className="checkin-modal-actions">
            <AnimatedButton
              variant="secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              稍后再说
            </AnimatedButton>
            <AnimatedButton
              onClick={handleSubmit}
              disabled={isSubmitting || rating === 0}
            >
              {isSubmitting ? (
                <>
                  打卡中
                  <div className="loading-spinner"></div>
                </>
              ) : (
                "完成打卡"
              )}
            </AnimatedButton>
          </div>
        </div>
      </GlassCard>
    </div>
  );
}
