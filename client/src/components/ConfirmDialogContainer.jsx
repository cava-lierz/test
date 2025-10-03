import React, { useState, useCallback, useEffect } from "react";
import ConfirmModal from "./ConfirmModal";

const ConfirmDialogContainer = () => {
  const [confirmDialog, setConfirmDialog] = useState(null);

  const showConfirm = useCallback((message, onConfirm, onCancel) => {
    setConfirmDialog({
      message,
      onConfirm: () => {
        if (onConfirm) onConfirm();
        setConfirmDialog(null);
      },
      onCancel: () => {
        if (onCancel) onCancel();
        setConfirmDialog(null);
      },
    });
  }, []);

  // 将showConfirm方法暴露到全局，供其他组件使用
  useEffect(() => {
    window.showConfirm = showConfirm;
    return () => {
      delete window.showConfirm;
    };
  }, [showConfirm]);

  if (!confirmDialog) return null;

  return (
    <ConfirmModal
      title="确认"
      message={confirmDialog.message}
      onConfirm={confirmDialog.onConfirm}
      onCancel={confirmDialog.onCancel}
      confirmText="确认"
      cancelText="取消"
    />
  );
};

export default ConfirmDialogContainer;
