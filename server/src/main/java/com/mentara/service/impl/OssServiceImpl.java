package com.mentara.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.mentara.config.OssConfig;
import com.mentara.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    private static final Logger logger = LoggerFactory.getLogger(OssServiceImpl.class);

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName) {
        try {
            if (!isValidImageFile(file)) {
                throw new IllegalArgumentException("无效的图片文件格式或大小");
            }

            // 构建完整的文件路径
            String objectKey = folder + "/" + fileName;
            
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(), 
                objectKey, 
                file.getInputStream()
            );

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 构建并返回文件URL
            String fileUrl = ossConfig.getBaseUrl() + "/" + objectKey;
            
            logger.info("文件上传成功: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                // 从URL中提取对象键
                String objectKey = extractObjectKeyFromUrl(fileUrl);
                
                if (objectKey != null) {
                    // 删除文件
                    ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
                    logger.info("文件删除成功: {}", objectKey);
                } else {
                    logger.warn("无法从URL中提取对象键: {}", fileUrl);
                }
            }
        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateUniqueFileName(String originalFileName, String prefix) {
        String fileExtension = getFileExtension(originalFileName);
        return prefix + UUID.randomUUID().toString() + fileExtension;
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("文件大小超出限制: {} bytes, 最大允许: {} bytes", file.getSize(), MAX_FILE_SIZE);
            return false;
        }

        // 检查文件类型
        String contentType = file.getContentType();
        boolean isValidType = contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
        
        if (!isValidType) {
            logger.warn("不支持的文件类型: {}", contentType);
        }
        
        return isValidType;
    }

    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // 移除开头的斜杠
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            logger.error("解析文件URL失败: {}", fileUrl, e);
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
} 