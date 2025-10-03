import React, { useState } from "react";
import { createPortal } from "react-dom";
import { useBlock } from "../context/BlockContext";
import "../styles/components.css";

const BlockUserModal = ({ isOpen, onClose, userToBlock, onBlockSuccess }) => {
  const [reason, setReason] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { blockUser } = useBlock();

  const blockReasons = [
    "不当或冒犯性内容",
    "垃圾信息或广告",
    "骚扰行为",
    "虚假信息",
    "其他",
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!userToBlock) return;

    setIsSubmitting(true);
    try {
      await blockUser(userToBlock.id, reason);
      window.showToast && window.showToast("拉黑成功！", "success");
      onBlockSuccess && onBlockSuccess();
      onClose();
    } catch (error) {
      console.error("拉黑失败:", error);
      if (error.message.includes("已经拉黑")) {
        window.showToast && window.showToast("您已经拉黑过该用户", "warning");
      } else if (error.message.includes("不能拉黑管理员")) {
        window.showToast && window.showToast("不能拉黑管理员", "warning");
      } else if (error.message.includes("不能拉黑自己")) {
        window.showToast && window.showToast("不能拉黑自己", "warning");
      } else {
        window.showToast && window.showToast("拉黑失败，请稍后重试", "error");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setReason("");
    setIsSubmitting(false);
    onClose();
  };

  if (!isOpen || !userToBlock) return null;

  // 前端验证：如果是管理员或专家，显示提示并关闭弹窗
  if (userToBlock.role === "ADMIN" || userToBlock.role === "EXPERT") {
    window.showToast && window.showToast("不能拉黑管理员或专家", "warning");
    onClose();
    return null;
  }

  return createPortal(
    <div className="block-modal-overlay" onClick={handleClose}>
      <div className="block-modal" onClick={(e) => e.stopPropagation()}>
        <div className="block-modal-header">
          <h3>拉黑用户</h3>
          <button className="block-modal-close" onClick={handleClose}>
            ×
          </button>
        </div>

        <div className="block-modal-body">
          <div className="block-user-info">
            <img
              src={userToBlock.avatar || "https://i.pravatar.cc/150?u=default"}
              alt={userToBlock.nickname || userToBlock.username}
              className="block-user-avatar"
            />
            <div className="block-user-details">
              <span className="block-user-name">
                {userToBlock.nickname || userToBlock.username}
              </span>
              <span className="block-user-role">
                {userToBlock.role === "ADMIN"
                  ? "管理员"
                  : userToBlock.role === "EXPERT"
                  ? "专家"
                  : "用户"}
              </span>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="block-form">
            <div className="block-reason-section">
              <label className="block-form-label">拉黑原因（可选）</label>
              <div className="block-reason-options">
                {blockReasons.map((blockReason) => (
                  <button
                    key={blockReason}
                    type="button"
                    className={`block-reason-option ${
                      reason === blockReason ? "selected" : ""
                    }`}
                    onClick={() => setReason(blockReason)}
                  >
                    {blockReason}
                  </button>
                ))}
              </div>
            </div>

            <div className="block-custom-reason">
              <label className="block-form-label">自定义原因</label>
              <textarea
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="请输入拉黑原因..."
                className="block-reason-textarea"
                rows="3"
                maxLength="200"
              />
            </div>

            <div className="block-modal-actions">
              <button
                type="button"
                className="block-cancel-btn"
                onClick={handleClose}
                disabled={isSubmitting}
              >
                取消
              </button>
              <button
                type="submit"
                className="block-confirm-btn"
                disabled={isSubmitting}
              >
                {isSubmitting ? "拉黑中..." : "确认拉黑"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default BlockUserModal;
