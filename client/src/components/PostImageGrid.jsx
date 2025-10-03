import React, { useState } from 'react';

export default function PostImageGrid({ imageUrls }) {
  const [previewImage, setPreviewImage] = useState(null);

  if (!imageUrls || imageUrls.length === 0) {
    return null;
  }

  // 根据图片数量确定布局类型
  const getGridClass = (count) => {
    if (count === 1) return 'image-grid-single';
    if (count === 2) return 'image-grid-double';
    if (count === 3) return 'image-grid-triple';
    if (count === 4) return 'image-grid-quad';
    return 'image-grid-multi'; // 5-9张图片
  };

  const handleImageClick = (imageUrl, e) => {
    e.stopPropagation(); // 阻止事件冒泡到帖子卡片
    setPreviewImage(imageUrl);
  };

  const handlePreviewClose = (e) => {
    e.stopPropagation();
    setPreviewImage(null);
  };

  return (
    <>
      <div className={`post-image-grid ${getGridClass(imageUrls.length)}`}>
        {imageUrls.map((imageUrl, index) => (
          <div 
            key={index} 
            className="post-image-item"
            onClick={(e) => handleImageClick(imageUrl, e)}
          >
            <img 
              src={imageUrl} 
              alt={`帖子图片 ${index + 1}`} 
              className="post-image"
            />
            {/* 如果是第9张图片且还有更多图片，显示数量覆盖层 */}
            {index === 8 && imageUrls.length > 9 && (
              <div className="image-overlay">
                <span className="image-count">+{imageUrls.length - 9}</span>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 图片预览模态框 */}
      {previewImage && (
        <div className="image-preview-modal" onClick={handlePreviewClose}>
          <div className="image-preview-container">
            <img src={previewImage} alt="预览图片" className="preview-image" />
            <button 
              className="preview-close-btn" 
              onClick={handlePreviewClose}
              type="button"
            >
              ×
            </button>
          </div>
        </div>
      )}
    </>
  );
} 