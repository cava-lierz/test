import React, { useState, useRef, useCallback } from 'react';
import { createPortal } from 'react-dom';
import ReactCrop from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import '../styles/components.css';

const AvatarCropModal = ({ isOpen, onClose, imageSrc, onCropComplete }) => {
  const [crop, setCrop] = useState({
    unit: '%',
    width: 80,
    height: 80,
    x: 10,
    y: 10,
    aspect: 1 // 正方形裁剪
  });
  const [completedCrop, setCompletedCrop] = useState(null);
  const imgRef = useRef(null);

  const onLoad = useCallback((e) => {
    const img = e.target;
    if (img && img.complete && img.naturalWidth > 0) {
      imgRef.current = img;
      // 设置初始裁剪区域
      const initialCrop = {
        unit: 'px',
        width: Math.min(200, img.width * 0.8),
        height: Math.min(200, img.height * 0.8),
        x: Math.max(0, (img.width - Math.min(200, img.width * 0.8)) / 2),
        y: Math.max(0, (img.height - Math.min(200, img.height * 0.8)) / 2),
        aspect: 1
      };
      setCrop(initialCrop);
      setCompletedCrop(initialCrop);
      console.log('图片加载完成，设置初始裁剪区域:', initialCrop);
    }
  }, []);

  const onCropChange = (crop) => {
    setCrop(crop);
  };

  const onCropCompleteInternal = useCallback((crop) => {
    setCompletedCrop(crop);
  }, []);

  // 生成裁剪后的图片
  const getCroppedImg = (image, crop, fileName) => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    if (!ctx) {
      throw new Error('无法获取Canvas上下文');
    }

    // 计算实际的裁剪区域
    const scaleX = image.naturalWidth / image.width;
    const scaleY = image.naturalHeight / image.height;
    
    // 设置画布尺寸
    const pixelRatio = window.devicePixelRatio || 1;
    canvas.width = crop.width * pixelRatio;
    canvas.height = crop.height * pixelRatio;
    
    ctx.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
    ctx.imageSmoothingQuality = 'high';

    // 绘制裁剪的图像
    ctx.drawImage(
      image,
      crop.x * scaleX,
      crop.y * scaleY,
      crop.width * scaleX,
      crop.height * scaleY,
      0,
      0,
      crop.width,
      crop.height
    );

    return new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (!blob) {
            reject(new Error('Canvas转换为Blob失败'));
            return;
          }
          blob.name = fileName;
          resolve(blob);
        },
        'image/jpeg',
        0.9
      );
    });
  };

  const handleCropConfirm = async () => {
    // 使用 completedCrop 或 crop 作为裁剪区域
    const cropToUse = completedCrop || crop;
    
    if (!cropToUse || !imgRef.current) {
      console.error('缺少裁剪区域或图片引用');
      return;
    }

    // 确保图像是完整的 HTMLImageElement
    const img = imgRef.current;
    if (!img || img.tagName !== 'IMG' || !img.complete) {
      console.error('图像未正确加载');
      return;
    }

    try {
      console.log('开始裁剪，使用裁剪区域:', cropToUse);
      const croppedImageBlob = await getCroppedImg(
        img,
        cropToUse,
        'cropped-avatar.jpg'
      );
      
      // 创建一个File对象
      const croppedFile = new File([croppedImageBlob], 'cropped-avatar.jpg', {
        type: 'image/jpeg'
      });

      onCropComplete(croppedFile);
      handleClose();
    } catch (error) {
      console.error('裁剪图片时出错:', error);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handleClose = () => {
    setCompletedCrop(null);
    setCrop({
      unit: '%',
      width: 80,
      height: 80,
      x: 10,
      y: 10,
      aspect: 1
    });
    onClose();
  };

  // 每次打开模态框时重置状态
  React.useEffect(() => {
    if (isOpen) {
      setCompletedCrop(null);
      
      // 检查图片是否已经加载完成（处理缓存情况）
      setTimeout(() => {
        const img = imgRef.current;
        if (img && img.complete && img.naturalWidth > 0) {
          const initialCrop = {
            unit: 'px',
            width: Math.min(200, img.width * 0.8),
            height: Math.min(200, img.height * 0.8),
            x: Math.max(0, (img.width - Math.min(200, img.width * 0.8)) / 2),
            y: Math.max(0, (img.height - Math.min(200, img.height * 0.8)) / 2),
            aspect: 1
          };
          setCrop(initialCrop);
          setCompletedCrop(initialCrop);
          console.log('从缓存加载图片，设置初始裁剪区域:', initialCrop);
        }
      }, 100);
    }
  }, [isOpen, imageSrc]);

  if (!isOpen) return null;

  return createPortal(
    <div className="avatar-crop-modal-overlay" onClick={handleBackdropClick}>
      <div className="avatar-crop-modal">
        <div className="avatar-crop-header">
          <h3 className="avatar-crop-title">裁剪头像</h3>
          <button 
            className="avatar-crop-close-btn" 
            onClick={handleClose}
            aria-label="关闭"
          >
            ✕
          </button>
        </div>
        
        <div className="avatar-crop-content">
          <div className="crop-container">
            <ReactCrop
              crop={crop}
              onChange={onCropChange}
              onComplete={onCropCompleteInternal}
              aspect={1}
              minWidth={100}
              minHeight={100}
              keepSelection
              ruleOfThirds
            >
              <img
                ref={imgRef}
                alt="待裁剪的头像"
                src={imageSrc}
                onLoad={onLoad}
                className="crop-image"
              />
            </ReactCrop>
          </div>
          
          <div className="crop-tips">
            <p>🖱️ 拖拽边框调整裁剪区域</p>
            <p>📐 自动保持正方形比例</p>
            <p>✂️ 建议选择清晰的面部区域</p>
            <div className="crop-scroll-hint">
              💡 如果图片过大，可以在虚线框内滚动查看完整图片
            </div>
          </div>
        </div>
        
        <div className="avatar-crop-actions">
          <button 
            className="crop-cancel-btn" 
            onClick={handleClose}
          >
            取消
          </button>
          <button 
            className="crop-confirm-btn" 
            onClick={handleCropConfirm}
            disabled={!imgRef.current || !imageSrc}
            title={!imgRef.current ? '图片未加载' : !imageSrc ? '缺少图片源' : ''}
          >
            确认裁剪
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default AvatarCropModal; 