import React, { useState } from "react";
import { createPortal } from "react-dom";
import "../styles/components.css";

const ReportModal = ({ isOpen, onClose, onSubmit, postId }) => {
  const [selectedReason, setSelectedReason] = useState("");
  const [customReason, setCustomReason] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const predefinedReasons = [
    "不当内容",
    "垃圾信息",
    "骚扰行为",
    "虚假信息",
    "暴力内容",
    "其他原因",
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedReason) {
      window.showToast && window.showToast("请选择举报原因", "warning");
      return;
    }

    const reason =
      selectedReason === "其他原因" ? customReason : selectedReason;

    if (selectedReason === "其他原因" && !customReason.trim()) {
      window.showToast && window.showToast("请填写具体的举报原因", "warning");
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(postId, reason);
      handleClose();
    } catch (error) {
      // 错误处理由父组件负责
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setSelectedReason("");
    setCustomReason("");
    onClose();
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="modal-overlay" onClick={handleClose}>
      <div
        className="modal-content report-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h3>举报内容</h3>
          <button className="modal-close-btn" onClick={handleClose}>
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="report-form">
          <div className="form-section">
            <label className="form-label">请选择举报原因：</label>
            <div className="reason-options">
              {predefinedReasons.map((reason) => (
                <label key={reason} className="reason-option">
                  <input
                    type="radio"
                    name="reason"
                    value={reason}
                    checked={selectedReason === reason}
                    onChange={(e) => setSelectedReason(e.target.value)}
                  />
                  <span className="reason-text">{reason}</span>
                </label>
              ))}
            </div>
          </div>

          {selectedReason === "其他原因" && (
            <div className="form-section">
              <label className="form-label">请详细说明：</label>
              <textarea
                className="custom-reason-textarea"
                value={customReason}
                onChange={(e) => setCustomReason(e.target.value)}
                placeholder="请详细描述举报原因..."
                maxLength={200}
                rows={3}
              />
              <div className="char-count">{customReason.length}/200</div>
            </div>
          )}

          <div className="modal-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              disabled={isSubmitting}
            >
              取消
            </button>
            <button
              type="submit"
              className="btn btn-danger"
              disabled={isSubmitting}
            >
              {isSubmitting ? "提交中..." : "提交举报"}
            </button>
          </div>
        </form>
      </div>
    </div>,
    document.body
  );
};

export default ReportModal;
