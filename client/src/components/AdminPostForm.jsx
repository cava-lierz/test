import React, { useState, useEffect } from "react";
import { postAPI, postImageAPI } from "../services/api";

import "../styles/components.css";

const AdminPostForm = ({ onSuccess, onClose }) => {
  const [postMode, setPostMode] = useState("normal"); // normal 或 announcement
  const [content, setContent] = useState("");
  const [selectedMood, setSelectedMood] = useState("HAPPY");
  const [selectedTags, setSelectedTags] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [tags, setTags] = useState([]);
  const [title, setTitle] = useState("");
  const [uploadedImages, setUploadedImages] = useState([]);
  const [isUploading, setIsUploading] = useState(false);

  const moods = [
    { emoji: "😊", value: "HAPPY" },
    { emoji: "😢", value: "SAD" },
    { emoji: "😡", value: "ANGRY" },
    { emoji: "😰", value: "ANXIOUS" },
    { emoji: "😴", value: "TIRED" },
    { emoji: "🤗", value: "GRATEFUL" },
    { emoji: "💪", value: "MOTIVATED" },
    { emoji: "🎉", value: "EXCITED" },
    { emoji: "😕", value: "CONFUSED" },
    { emoji: "😌", value: "PEACEFUL" },
    { emoji: "😐", value: "CALM" },
  ];

  useEffect(() => {
    fetchTags();
  }, []);

  const fetchTags = async () => {
    try {
      const response = await postAPI.getTags();
      setTags(response || []);
    } catch (error) {
      console.error("获取标签失败:", error);
    }
  };

  const handleTagToggle = (tagId) => {
    setSelectedTags((prev) =>
      prev.includes(tagId)
        ? prev.filter((id) => id !== tagId)
        : [...prev, tagId]
    );
  };

  // 图片上传处理
  const handleImageUpload = async (event) => {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    // 检查图片数量限制（最多9张）
    if (uploadedImages.length + files.length > 9) {
      window.showToast && window.showToast("最多只能上传9张图片", "warning");
      return;
    }

    setIsUploading(true);

    try {
      const uploadPromises = files.map(async (file) => {
        // 检查文件类型
        if (!file.type.startsWith("image/")) {
          throw new Error("只能上传图片文件");
        }

        // 检查文件大小（限制为5MB）
        if (file.size > 5 * 1024 * 1024) {
          throw new Error("图片大小不能超过5MB");
        }

        const formData = new FormData();
        formData.append("file", file);

        const response = await postImageAPI.uploadPostImage(formData);
        return response.imageUrl;
      });

      const imageUrls = await Promise.all(uploadPromises);
      setUploadedImages((prev) => [...prev, ...imageUrls]);
    } catch (error) {
      console.error("图片上传失败:", error);
      window.showToast &&
        window.showToast(error.message || "图片上传失败，请重试", "error");
    } finally {
      setIsUploading(false);
      // 清空input的值，这样同一个文件可以再次选择
      event.target.value = "";
    }
  };

  // 删除图片
  const handleImageDelete = async (imageUrl) => {
    try {
      await postImageAPI.deletePostImage(imageUrl);
      setUploadedImages((prev) => prev.filter((url) => url !== imageUrl));
    } catch (error) {
      console.error("图片删除失败:", error);
      window.showToast && window.showToast("图片删除失败，请重试", "error");
    }
  };

  // 删除所有图片
  const deleteAllImages = async () => {
    if (uploadedImages.length === 0) return;

    try {
      await Promise.all(
        uploadedImages.map((imageUrl) => postImageAPI.deletePostImage(imageUrl))
      );
      setUploadedImages([]);
    } catch (error) {
      console.error("删除图片失败:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title.trim()) {
      window.showToast && window.showToast("请输入标题", "warning");
      return;
    }
    if (!content.trim()) {
      window.showToast && window.showToast("请输入帖子内容", "warning");
      return;
    }

    if (postMode === "normal" && selectedTags.length === 0) {
      window.showToast && window.showToast("请至少选择一个标签", "warning");
      return;
    }

    setIsSubmitting(true);
    try {
      const postData = {
        title: title.trim(),
        content: content.trim(),
        mood: postMode === "normal" ? selectedMood : null,
        tagIds: postMode === "normal" ? selectedTags : [],
        isAnnouncement: postMode === "announcement",
        imageUrls: uploadedImages,
      };

      await postAPI.createPost(postData);

      // 重置表单
      setTitle("");
      setContent("");
      setSelectedMood("HAPPY");
      setSelectedTags([]);
      setPostMode("normal");
      setUploadedImages([]);

      if (onSuccess) {
        onSuccess();
      }

      window.showToast &&
        window.showToast(
          postMode === "announcement" ? "公告发布成功！" : "帖子发布成功！",
          "success"
        );

      if (onClose) {
        onClose();
      }
    } catch (error) {
      console.error("发布失败:", error);
      window.showToast && window.showToast("发布失败，请稍后重试", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleModeChange = (mode) => {
    setPostMode(mode);
    // 切换模式时重置相关状态
    if (mode === "announcement") {
      setSelectedMood("HAPPY");
      setSelectedTags([]);
    }
  };

  const handleCancel = () => {
    if (content.trim() || uploadedImages.length > 0) {
      window.showConfirm &&
        window.showConfirm("确定要取消发布吗？未保存的内容将会丢失。", () => {
          deleteAllImages();
          if (onClose) {
            onClose();
          }
        });
    } else {
      if (onClose) {
        onClose();
      }
    }
  };

  return (
    <div className="admin-post-form">
      <div className="admin-post-form-header">
        <h3>管理员发帖</h3>
        <div className="post-mode-switcher">
          <button
            type="button"
            className={`mode-btn ${postMode === "normal" ? "active" : ""}`}
            onClick={() => handleModeChange("normal")}
          >
            📝 普通帖子
          </button>
          <button
            type="button"
            className={`mode-btn ${
              postMode === "announcement" ? "active" : ""
            }`}
            onClick={() => handleModeChange("announcement")}
          >
            📢 公告帖子
          </button>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="admin-post-form-content">
        {/* 标题输入 */}
        <div className="form-group">
          <label className="form-label">
            {postMode === "announcement" ? "公告标题" : "帖子标题"}
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder={
              postMode === "announcement"
                ? "请输入公告标题..."
                : "请输入帖子标题..."
            }
            className="post-title-input"
            maxLength={50}
          />
        </div>

        {/* 帖子内容 */}
        <div className="form-group">
          <label className="form-label">
            {postMode === "announcement" ? "公告内容" : "帖子内容"}
          </label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder={
              postMode === "announcement"
                ? "请输入公告内容..."
                : "分享你的想法..."
            }
            className={`content-textarea ${
              postMode === "announcement" ? "announcement-textarea" : ""
            }`}
            rows={postMode === "announcement" ? 6 : 4}
            maxLength={500}
          />
          <div className="char-count">{content.length}/500</div>

          {/* 图片上传和显示区域 */}
          <div className="image-upload-section">
            {/* 图片网格显示 */}
            <div className="image-grid">
              {uploadedImages.map((imageUrl, index) => (
                <div key={index} className="image-item">
                  <img src={imageUrl} alt={`上传的图片 ${index + 1}`} />
                  <button
                    type="button"
                    className="image-delete-btn"
                    onClick={() => handleImageDelete(imageUrl)}
                    title="删除图片"
                  >
                    ×
                  </button>
                </div>
              ))}

              {/* 图片上传按钮 */}
              {uploadedImages.length < 9 && (
                <div className="image-upload-btn">
                  <input
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleImageUpload}
                    disabled={isUploading}
                    id="admin-image-upload-input"
                    style={{ display: "none" }}
                  />
                  <label
                    htmlFor="admin-image-upload-input"
                    className="upload-trigger"
                  >
                    {isUploading ? (
                      <div className="upload-loading">
                        <div className="loading-spinner"></div>
                      </div>
                    ) : (
                      <span className="upload-icon">+</span>
                    )}
                  </label>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 心情选择 - 仅普通帖子 */}
        {postMode === "normal" && (
          <div className="form-group">
            <label className="form-label">心情</label>
            <div className="mood-selector">
              {moods.map((mood) => (
                <button
                  key={mood.value}
                  type="button"
                  className={`mood-btn ${
                    selectedMood === mood.value ? "selected" : ""
                  }`}
                  onClick={() => setSelectedMood(mood.value)}
                >
                  {mood.emoji}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* 标签选择 - 仅普通帖子 */}
        {postMode === "normal" && (
          <div className="form-group">
            <label className="form-label">标签 (至少选择一个)</label>
            <div className="tag-selector">
              {tags.map((tag) => (
                <button
                  key={tag.id}
                  type="button"
                  className={`tag-btn ${
                    selectedTags.includes(tag.id) ? "selected" : ""
                  }`}
                  style={{
                    borderColor: selectedTags.includes(tag.id)
                      ? tag.color
                      : "#ddd",
                    color: selectedTags.includes(tag.id) ? tag.color : "#666",
                  }}
                  onClick={() => handleTagToggle(tag.id)}
                >
                  #{tag.tag}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* 公告预览 */}
        {postMode === "announcement" && content && (
          <div className="form-group">
            <label className="form-label">公告预览</label>
            <div className="announcement-preview">
              <div className="announcement-header">
                <span className="announcement-badge">📢 官方公告</span>
                <span className="admin-badge">👑 管理员</span>
              </div>
              <div className="announcement-content">{content}</div>
            </div>
          </div>
        )}

        {/* 提交按钮 */}
        <div className="form-actions">
          {onClose && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleCancel}
              disabled={isSubmitting}
            >
              取消
            </button>
          )}
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? "发布中..."
              : postMode === "announcement"
              ? "发布公告"
              : "发布帖子"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AdminPostForm;
