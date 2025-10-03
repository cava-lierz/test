package com.mentara.service.impl;

import com.mentara.service.FileUploadService;
import com.mentara.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private OssService ossService;

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 生成唯一文件名
        String fileName = ossService.generateUniqueFileName(
            file.getOriginalFilename(), 
            "avatar_" + userId + "_"
        );
        // 使用阿里云OSS上传
        return ossService.uploadFile(file, "avatars", fileName);
    }

    @Override
    public String uploadPostImage(MultipartFile file, Long userId) {
        // 生成唯一文件名
        String fileName = ossService.generateUniqueFileName(
            file.getOriginalFilename(), 
            "post_" + userId + "_"
        );
        // 使用阿里云OSS上传
        return ossService.uploadFile(file, "posts", fileName);
    }

    @Override
    public void deleteAvatar(String fileUrl) {
        // 使用阿里云OSS删除
        ossService.deleteFile(fileUrl);
    }

    @Override
    public void deletePostImage(String fileUrl) {
        // 使用阿里云OSS删除
        ossService.deleteFile(fileUrl);
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        // 使用OSS服务进行文件验证
        return ossService.isValidImageFile(file);
    }
} 