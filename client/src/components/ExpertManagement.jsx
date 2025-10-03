import React, { useState } from "react";
import { expertAPI } from "../services/api";
import GenericModal from "./GenericModal";

export default function ExpertManagement({ experts, onExpertsChange }) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedExpert, setSelectedExpert] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    specialty: "",
    contact: "",
    status: "offline",
  });
  const [loading, setLoading] = useState(false);

  // 重置表单数据
  const resetForm = () => {
    setFormData({
      name: "",
      specialty: "",
      contact: "",
      status: "offline",
    });
  };

  // 打开添加专家模态框
  const handleAddExpert = () => {
    resetForm();
    setShowAddModal(true);
  };

  // 打开编辑专家模态框
  const handleEditExpert = (expert) => {
    setSelectedExpert(expert);
    setFormData({
      name: expert.expertName,
      specialty: expert.specialty || "",
      contact: expert.contact || "",
      status: expert.status || "offline",
    });
    setShowEditModal(true);
  };

  // 打开删除专家模态框
  const handleDeleteExpert = (expert) => {
    setSelectedExpert(expert);
    setShowDeleteModal(true);
  };

  // 处理表单输入变化
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 提交添加专家
  const handleSubmitAdd = async () => {
    if (!formData.name.trim()) {
      window.showToast && window.showToast("请输入专家姓名", "warning");
      return;
    }

    setLoading(true);
    try {
      await expertAPI.addExpert(formData);
      setShowAddModal(false);
      resetForm();
      onExpertsChange();
    } catch (error) {
      window.showToast &&
        window.showToast("添加专家失败: " + error.message, "error");
    } finally {
      setLoading(false);
    }
  };

  // 提交编辑专家
  const handleSubmitEdit = async () => {
    if (!formData.name.trim()) {
      window.showToast && window.showToast("请输入专家姓名", "warning");
      return;
    }

    setLoading(true);
    try {
      await expertAPI.updateExpert(selectedExpert.expertId, formData);
      setShowEditModal(false);
      setSelectedExpert(null);
      resetForm();
      onExpertsChange();
    } catch (error) {
      window.showToast &&
        window.showToast("更新专家失败: " + error.message, "error");
    } finally {
      setLoading(false);
    }
  };

  // 确认删除专家
  const handleConfirmDelete = async () => {
    setLoading(true);
    try {
      await expertAPI.deleteExpert(selectedExpert.expertId);
      setShowDeleteModal(false);
      setSelectedExpert(null);
      onExpertsChange();
    } catch (error) {
      window.showToast &&
        window.showToast("删除专家失败: " + error.message, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="expert-management">
      {/* 添加专家按钮 */}
      <div className="expert-management-header">
        <button
          className="admin-action-btn admin-action-btn-primary"
          onClick={handleAddExpert}
        >
          ➕ 添加专家
        </button>
      </div>

      {/* 专家列表 */}
      <div className="expert-list">
        {experts.map((expert) => (
          <div key={expert.userId} className="admin-expert-item">
            <div className="admin-expert-header">
              <div className="admin-expert-info">
                <span className="admin-expert-name">{expert.expertName}</span>
                <span className="admin-expert-specialty">
                  {expert.specialty}
                </span>
              </div>
              <span
                className="admin-expert-status"
                style={{
                  color: expert.status === "online" ? "#4caf50" : "#f44336",
                }}
              >
                {expert.status === "online" ? "🟢 在线" : "🔴 离线"}
              </span>
            </div>
            <div className="admin-expert-contact">
              <p>
                <strong>联系方式：</strong>
                {expert.contact}
              </p>
            </div>
            <div className="admin-expert-actions">
              <button
                className="admin-action-btn admin-action-btn-edit"
                onClick={() => handleEditExpert(expert)}
                disabled={!expert.hasExpertDetails}
              >
                编辑
              </button>
              <button
                className="admin-action-btn admin-action-btn-delete"
                onClick={() => handleDeleteExpert(expert)}
                disabled={!expert.hasExpertDetails}
              >
                删除
              </button>
              <button className="admin-action-btn admin-action-btn-contact">
                联系专家
              </button>
              <button className="admin-action-btn admin-action-btn-schedule">
                预约咨询
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* 添加专家模态框 */}
      <GenericModal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
      >
        <div className="admin-modal-content">
          <div className="admin-modal-header">
            <h3>添加心理专家</h3>
          </div>
          <div className="admin-modal-body">
            <div className="form-group">
              <label>专家姓名 *</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="请输入专家姓名"
                required
              />
            </div>
            <div className="form-group">
              <label>专业领域</label>
              <input
                type="text"
                name="specialty"
                value={formData.specialty}
                onChange={handleInputChange}
                placeholder="如：抑郁症治疗、焦虑症咨询等"
              />
            </div>
            <div className="form-group">
              <label>联系方式</label>
              <input
                type="text"
                name="contact"
                value={formData.contact}
                onChange={handleInputChange}
                placeholder="邮箱或电话号码"
              />
            </div>
            <div className="form-group">
              <label>在线状态</label>
              <select
                name="status"
                value={formData.status}
                onChange={handleInputChange}
              >
                <option value="online">在线</option>
                <option value="offline">离线</option>
              </select>
            </div>
          </div>
          <div className="admin-modal-actions">
            <button
              className="admin-action-btn admin-action-btn-cancel"
              onClick={() => setShowAddModal(false)}
              disabled={loading}
            >
              取消
            </button>
            <button
              className="admin-action-btn admin-action-btn-primary"
              onClick={handleSubmitAdd}
              disabled={loading}
            >
              {loading ? "添加中..." : "添加专家"}
            </button>
          </div>
        </div>
      </GenericModal>

      {/* 编辑专家模态框 */}
      <GenericModal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
      >
        <div className="admin-modal-content">
          <div className="admin-modal-header">
            <h3>编辑心理专家</h3>
          </div>
          <div className="admin-modal-body">
            <div className="form-group">
              <label>专家姓名 *</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="请输入专家姓名"
                required
              />
            </div>
            <div className="form-group">
              <label>专业领域</label>
              <input
                type="text"
                name="specialty"
                value={formData.specialty}
                onChange={handleInputChange}
                placeholder="如：抑郁症治疗、焦虑症咨询等"
              />
            </div>
            <div className="form-group">
              <label>联系方式</label>
              <input
                type="text"
                name="contact"
                value={formData.contact}
                onChange={handleInputChange}
                placeholder="邮箱或电话号码"
              />
            </div>
            <div className="form-group">
              <label>在线状态</label>
              <select
                name="status"
                value={formData.status}
                onChange={handleInputChange}
              >
                <option value="online">在线</option>
                <option value="offline">离线</option>
              </select>
            </div>
          </div>
          <div className="admin-modal-actions">
            <button
              className="admin-action-btn admin-action-btn-cancel"
              onClick={() => setShowEditModal(false)}
              disabled={loading}
            >
              取消
            </button>
            <button
              className="admin-action-btn admin-action-btn-primary"
              onClick={handleSubmitEdit}
              disabled={loading}
            >
              {loading ? "更新中..." : "更新专家"}
            </button>
          </div>
        </div>
      </GenericModal>

      {/* 删除确认模态框 */}
      <GenericModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
      >
        <div className="admin-modal-content">
          <div className="admin-modal-header">
            <h3>删除确认</h3>
          </div>
          <div className="admin-modal-body">
            <p>
              确定要删除专家 <strong>{selectedExpert?.name}</strong> 吗？
            </p>
            <p>此操作不可撤销。</p>
          </div>
          <div className="admin-modal-actions">
            <button
              className="admin-action-btn admin-action-btn-cancel"
              onClick={() => setShowDeleteModal(false)}
              disabled={loading}
            >
              取消
            </button>
            <button
              className="admin-action-btn admin-action-btn-delete"
              onClick={handleConfirmDelete}
              disabled={loading}
            >
              {loading ? "删除中..." : "确认删除"}
            </button>
          </div>
        </div>
      </GenericModal>
    </div>
  );
}
