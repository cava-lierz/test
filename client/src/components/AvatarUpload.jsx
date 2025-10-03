import React, { useState, useRef, useEffect } from "react";
import { uploadAvatar, deleteAvatar, getCurrentUserId } from "../services/api";
import { AUTH_CONFIG } from "../utils/constants";
import { buildAvatarUrl } from "../utils/avatarUtils";
import AvatarPreviewModal from "./AvatarPreviewModal";
import AvatarCropModal from "./AvatarCropModal";

const AvatarUpload = ({
  onAvatarChange,
  currentAvatar,
  className = "",
  onPreviewChange,
}) => {
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState("");
  const [previewUrl, setPreviewUrl] = useState(null);
  const [showAvatarPreview, setShowAvatarPreview] = useState(false);
  const [showCropModal, setShowCropModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [originalImageUrl, setOriginalImageUrl] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 重置之前的状态
    setSelectedFile(null);
    setOriginalImageUrl(null);

    // 验证文件类型
    const validTypes = [
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/gif",
      "image/webp",
    ];
    if (!validTypes.includes(file.type)) {
      setError("请选择有效的图片文件 (JPEG, PNG, GIF, WebP)");
      return;
    }

    // 验证文件大小 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError("文件大小不能超过 5MB");
      return;
    }

    setError("");
    setSelectedFile(file);

    // 创建图片URL用于裁剪和预览
    const reader = new FileReader();
    reader.onload = (e) => {
      setOriginalImageUrl(e.target.result);
      // 在裁剪之前先显示原始图片的预览
      setPreviewUrl(e.target.result);
      setShowCropModal(true);
    };
    reader.readAsDataURL(file);
  };

  // 处理裁剪完成
  const handleCropComplete = (croppedFile) => {
    // 创建裁剪后的预览
    const reader = new FileReader();
    reader.onload = (e) => {
      setPreviewUrl(e.target.result);
    };
    reader.readAsDataURL(croppedFile);

    // 保存裁剪后的文件用于上传
    setSelectedFile(croppedFile);
    setShowCropModal(false);
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError("请先选择并裁剪图片文件");
      return;
    }

    setIsUploading(true);
    setError("");

    try {
      // 检查用户是否已登录
      const userId = getCurrentUserId();
      const token = localStorage.getItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`);

      if (!userId || !token) {
        setError("请先登录后再上传头像");
        return;
      }

      // 调试信息：检查token状态
      console.log("Debug - User ID:", userId);
      console.log("Debug - Token exists:", !!token);
      console.log("Debug - Token length:", token ? token.length : 0);

      const formData = new FormData();
      formData.append("file", selectedFile);

      const response = await uploadAvatar(formData);

      if (response.success) {
        setPreviewUrl(null);
        setSelectedFile(null);
        setOriginalImageUrl(null);
        fileInputRef.current.value = "";
        if (onAvatarChange) {
          onAvatarChange(response.avatarUrl);
        }
        // 显示成功消息
        window.showToast && window.showToast("头像上传成功！", "success");
      } else {
        setError(response.message || "上传失败");
      }
    } catch (err) {
      console.error("Avatar upload error:", err);
      if (err.message.includes("401") || err.message.includes("Unauthorized")) {
        setError("登录已过期，请重新登录");
      } else {
        setError(err.message || "上传失败，请重试");
      }
    } finally {
      setIsUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!currentAvatar) {
      setError("当前没有头像可删除");
      return;
    }

    window.showConfirm &&
      window.showConfirm("确定要删除当前头像吗？", async () => {
        setIsUploading(true);
        setError("");

        try {
          const response = await deleteAvatar();

          if (response.success) {
            if (onAvatarChange) {
              onAvatarChange(null);
            }
            window.showToast && window.showToast("头像删除成功！", "success");
          } else {
            setError(response.message || "删除失败");
          }
        } catch (err) {
          setError(err.message || "删除失败，请重试");
        } finally {
          setIsUploading(false);
        }
      });
  };

  const getAvatarUrl = () => {
    if (previewUrl) return previewUrl;
    return buildAvatarUrl(currentAvatar, "https://i.pravatar.cc/150?u=default");
  };

  // 当预览URL变化时通知父组件
  useEffect(() => {
    if (onPreviewChange) {
      onPreviewChange(previewUrl);
    }
  }, [previewUrl, onPreviewChange]);

  const handleAvatarClick = () => {
    setShowAvatarPreview(true);
  };

  return (
    <div className={`avatar-upload ${className}`}>
      <div className="avatar-preview">
        <img
          src={getAvatarUrl()}
          alt="用户头像"
          className="avatar-image avatar-clickable"
          onClick={handleAvatarClick}
          title="点击查看头像"
        />
        {previewUrl && (
          <div className="preview-overlay">
            <span>预览</span>
          </div>
        )}
      </div>

      <div className="avatar-controls">
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileSelect}
          accept="image/*"
          style={{ display: "none" }}
        />

        <button
          type="button"
          onClick={() => {
            if (fileInputRef.current) {
              fileInputRef.current.value = "";
              fileInputRef.current.click();
            }
          }}
          className="btn-select"
          disabled={isUploading}
        >
          {previewUrl ? "重新选择" : "选择图片"}
        </button>

        {previewUrl && (
          <button
            type="button"
            onClick={handleUpload}
            className="btn-upload"
            disabled={isUploading}
          >
            {isUploading ? "上传中..." : "上传头像"}
          </button>
        )}

        {previewUrl && (
          <button
            type="button"
            onClick={() => setShowCropModal(true)}
            className="btn-crop"
            disabled={isUploading || !originalImageUrl}
          >
            重新裁剪
          </button>
        )}

        {currentAvatar && !previewUrl && (
          <button
            type="button"
            onClick={handleDelete}
            className="btn-delete"
            disabled={isUploading}
          >
            {isUploading ? "删除中..." : "删除头像"}
          </button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="upload-tips" style={{ color: "#888" }}>
        <p>支持格式：JPEG, PNG, GIF, WebP</p>
        <p>文件大小：不超过 5MB</p>
      </div>

      {/* 头像预览模态框 */}
      <AvatarPreviewModal
        isOpen={showAvatarPreview}
        onClose={() => setShowAvatarPreview(false)}
        avatarUrl={getAvatarUrl()}
        userName="我的头像"
      />

      {/* 头像裁剪模态框 */}
      <AvatarCropModal
        isOpen={showCropModal}
        onClose={() => setShowCropModal(false)}
        imageSrc={originalImageUrl}
        onCropComplete={handleCropComplete}
      />
    </div>
  );
};

export default AvatarUpload;
