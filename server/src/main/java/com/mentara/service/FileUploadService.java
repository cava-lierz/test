package com.mentara.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * 上传头像文件
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 存储在数据库中的对象键（key），例如: avatars/xxx.jpg
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 上传帖子图片
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 存储在数据库中的对象键（key），例如: posts/xxx.jpg
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