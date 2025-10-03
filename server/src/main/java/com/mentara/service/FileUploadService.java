package com.mentara.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * 上传头像文件
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 上传帖子图片
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String uploadPostImage(MultipartFile file, Long userId);
    
    /**
     * 删除头像文件
     * @param fileUrl 文件名或URL
     */
    void deleteAvatar(String fileUrl);
    
    /**
     * 删除帖子图片
     * @param fileUrl 文件URL
     */
    void deletePostImage(String fileUrl);
    
    /**
     * 验证文件类型
     * @param file 上传的文件
     * @return 是否有效
     */
    boolean isValidImageFile(MultipartFile file);
} 