import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { moods, postService } from "../services/postService";
import { postAPI, postImageAPI } from "../services/api";
import { getUserAvatarUrl } from "../utils/avatarUtils";

import AdminPostForm from "../components/AdminPostForm";

export default function PostPage() {
  const navigate = useNavigate();
  const { user, showConfirm, showSuccess, isAdmin } = useAuth();
  const [content, setContent] = useState("");
  const [mood, setMood] = useState("HAPPY");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [availableTags, setAvailableTags] = useState([]);
  const [tags, setTags] = useState([]);
  const [title, setTitle] = useState("");
  const [uploadedImages, setUploadedImages] = useState([]);
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    postAPI
      .getTags()
      .then(setAvailableTags)
      .catch(() => setAvailableTags([]));
  }, []);

  const handleTagToggle = (tagObj) => {
    setTags((prev) =>
      prev.some((t) => t.id === tagObj.id)
        ? prev.filter((t) => t.id !== tagObj.id)
        : [...prev, tagObj]
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
      showConfirm(error.message || "图片上传失败，请重试", () => {});
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
      showConfirm("图片删除失败，请重试", () => {});
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
      window.showToast && window.showToast("请输入内容", "warning");
      return;
    }
    if (tags.length === 0) {
      window.showToast && window.showToast("请选择至少一个标签", "warning");
      return;
    }
    setIsSubmitting(true);

    try {
      const postData = {
        title,
        content,
        mood,
        tagIds: tags.map((t) => t.id),
        imageUrls: uploadedImages,
      };
      await postService.createPost(postData);
      showSuccess("发布成功！");
      navigate("/community");
    } catch (error) {
      console.error("Failed to create post:", error);
      // Show an error message to the user
      showConfirm("发布失败，请稍后重试。", () => {});
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (content.trim() || uploadedImages.length > 0) {
      showConfirm("确定要取消发布吗？未保存的内容将会丢失。", async () => {
        await deleteAllImages();
        navigate("/community");
      });
    } else {
      navigate("/community");
    }
  };

  // 如果是管理员，显示管理员发帖界面
  if (isAdmin()) {
    return <AdminPostForm />;
  }

  return (
    <>
      <div className="post-form-container">
        <div className="post-form-card">
          {/* 头部 */}
          <div className="post-form-header">
            <img
              src={getUserAvatarUrl(user)}
              alt={user.nickname || user.username}
              className="post-form-avatar"
            />
            <div>
              <h1 className="post-form-title">分享你的想法</h1>
              <p className="post-form-subtitle">
                今天感觉怎么样？和大家分享一下吧
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="post-form">
            {/* 标题输入 */}
            <div className="title-input-section">
              <label className="form-label">帖子标题</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="请输入标题..."
                className="post-title-input"
                maxLength={50}
              />
            </div>

            {/* 心情选择 */}
            <div className="mood-selector-section">
              <label className="form-label">选择心情</label>
              <div className="mood-selector">
                {moods.map((moodOption) => (
                  <button
                    key={moodOption.value}
                    type="button"
                    onClick={() => setMood(moodOption.value)}
                    className={`mood-option ${
                      mood === moodOption.value ? "selected" : ""
                    }`}
                  >
                    <span className="mood-option-emoji">
                      {moodOption.emoji}
                    </span>
                    <span className="mood-option-label">
                      {moodOption.label}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* 标签选择 */}
            <div className="tag-selector-section">
              <label className="form-label">
                选择标签{" "}
                <span style={{ fontSize: "12px", color: "#888" }}>
                  (可多选)
                </span>
              </label>
              <div className="tag-selector">
                {availableTags.map((tag) => (
                  <button
                    key={tag.id}
                    type="button"
                    className={`post-tag-btn tag-color-${tag.tag}${
                      tags.some((t) => t.id === tag.id) ? " selected" : ""
                    }`}
                    style={{ borderColor: tag.color, color: tag.color }}
                    onClick={() => handleTagToggle(tag)}
                  >
                    #{tag.tag}
                  </button>
                ))}
              </div>
              {/* 已选自定义标签展示（非预设标签） */}
              <div
                style={{
                  marginTop: 6,
                  display: "flex",
                  gap: 6,
                  flexWrap: "wrap",
                }}
              >
                {tags
                  .filter((tag) => !availableTags.some((t) => t.id === tag.id))
                  .map((tag) => (
                    <span
                      key={tag.id}
                      className="post-tag-btn selected"
                      style={{
                        background: "#f3e8ff",
                        color: "#5e60ce",
                        borderColor: "#c3aed6",
                      }}
                    >
                      #{tag.tag}
                      <span
                        style={{ marginLeft: 4, cursor: "pointer" }}
                        onClick={() =>
                          setTags(tags.filter((t) => t.id !== tag.id))
                        }
                      >
                        ×
                      </span>
                    </span>
                  ))}
              </div>
            </div>

            {/* 内容输入 */}
            <div className="content-input-section">
              <label className="form-label">分享内容</label>
              <div className="content-input-wrapper">
                <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="写下你的想法、感受或者想要分享的事情..."
                  className="post-textarea"
                  rows="8"
                />

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
                          id="image-upload-input"
                          style={{ display: "none" }}
                        />
                        <label
                          htmlFor="image-upload-input"
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
            </div>

            {/* 操作按钮 */}
            <div className="post-form-actions">
              <button
                type="button"
                onClick={handleCancel}
                className="post-cancel-button"
              >
                取消
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="post-submit-button"
              >
                {isSubmitting ? (
                  <>
                    发布中
                    <div className="loading-spinner"></div>
                  </>
                ) : (
                  "发布动态"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
