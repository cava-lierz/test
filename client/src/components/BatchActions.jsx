import React, { useState } from "react";

const BatchActions = ({
  selectedUsers,
  onBatchAction,
  actionText,
  actionType = "secondary",
  totalUsers,
}) => {
  const [isSelectAll, setIsSelectAll] = useState(false);

  const handleSelectAll = () => {
    setIsSelectAll(!isSelectAll);
  };

  const handleBatchAction = () => {
    if (selectedUsers.length === 0) {
      window.showToast && window.showToast("请先选择要操作的用户", "warning");
      return;
    }

    const message = `确定要${actionText}选中的 ${selectedUsers.length} 个用户吗？`;
    window.showConfirm &&
      window.showConfirm(message, () => {
        onBatchAction(selectedUsers);
      });
  };

  if (totalUsers === 0) return null;

  return (
    <div className="batch-actions">
      <div className="batch-actions-left">
        <label className="select-all-checkbox">
          <input
            type="checkbox"
            checked={isSelectAll}
            onChange={handleSelectAll}
          />
          <span>全选</span>
        </label>
        <span className="selected-count">
          已选择 {selectedUsers.length} / {totalUsers} 个用户
        </span>
      </div>

      {selectedUsers.length > 0 && (
        <div className="batch-actions-right">
          <button
            className={`btn btn-${actionType}`}
            onClick={handleBatchAction}
          >
            批量{actionText} ({selectedUsers.length})
          </button>
        </div>
      )}
    </div>
  );
};

export default BatchActions;
