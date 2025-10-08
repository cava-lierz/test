package com.mentara.service.impl;

import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.mentara.config.OssConfig;
import com.mentara.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Date;

@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private ObsClient obsClient;

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

            // 上传文件到OBS
            obsClient.putObject(ossConfig.getBucketName(), objectKey, file.getInputStream());
            // 返回对象的key，后续从key生成临时URL
            return objectKey;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            if (objectKey != null && !objectKey.isEmpty()) {
                obsClient.deleteObject(ossConfig.getBucketName(), objectKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, long expiresInSeconds) {
        try {
            Date expiryDate = new Date(System.currentTimeMillis() + expiresInSeconds * 1000);
            return obsClient.createSignedUrl(HttpMethodEnum.GET, ossConfig.getBucketName(), objectKey, null, expiryDate, null, null);
        } catch (Exception e) {
            throw new RuntimeException("生成临时URL失败: " + e.getMessage(), e);
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
            return false;
        }

        // 检查文件类型
        String contentType = file.getContentType();
        boolean isValidType = contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());

        return isValidType;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}