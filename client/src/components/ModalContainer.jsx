import React from "react";
import { useAuth } from "../context/AuthContext";
import SuccessModal from "./SuccessModal";
import ConfirmModal from "./ConfirmModal";

export default function ModalContainer() {
  const {
    showSuccessModal,
    showConfirmModal,
    successMessage,
    confirmConfig,
    setShowSuccessModal,
    setShowConfirmModal,
  } = useAuth();

  return (
    <>
      {/* 成功提示模态框 */}
      {showSuccessModal && (
        <SuccessModal
          message={successMessage}
          onClose={() => setShowSuccessModal(false)}
        />
      )}

      {/* 确认对话框 */}
      {showConfirmModal && (
        <ConfirmModal
          title="确认"
          message={confirmConfig.message}
          onConfirm={() => {
            if (confirmConfig.onConfirm) {
              confirmConfig.onConfirm();
            }
            setShowConfirmModal(false);
          }}
          onCancel={() => {
            if (confirmConfig.onCancel) {
              confirmConfig.onCancel();
            }
            setShowConfirmModal(false);
          }}
          confirmText="确认"
          cancelText="取消"
        />
      )}
    </>
  );
}
