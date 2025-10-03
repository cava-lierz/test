package com.mentara.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    
    /**
     * 上传文件到OSS
     * @param file 上传的文件
     * @param folder 文件夹名称（如：avatars, posts等）
     * @param fileName 文件名
     * @return 文件的访问URL
     */
    String uploadFile(MultipartFile file, String folder, String fileName);
    
    /**
     * 删除OSS中的文件
     * @param fileUrl 文件的URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 生成唯一的文件名
     * @param originalFileName 原始文件名
     * @param prefix 前缀（如：avatar_userId_）
     * @return 唯一文件名
     */
    String generateUniqueFileName(String originalFileName, String prefix);
    
    /**
     * 验证图片文件
     * @param file 上传的文件
     * @return 是否为有效图片
     */
    boolean isValidImageFile(MultipartFile file);
} 