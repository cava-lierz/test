package com.mentara.service.impl;

import com.mentara.service.OssService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileUploadServiceImplTest {
    @Mock
    private OssService ossService;
    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadAvatar_shouldReturnUrl() {
        MultipartFile file = mock(MultipartFile.class);
        Long userId = 1L;
        String fileName = "avatar_1_xxx.jpg";
        String url = "http://oss.com/avatars/" + fileName;
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(ossService.generateUniqueFileName(anyString(), anyString())).thenReturn(fileName);
        when(ossService.uploadFile(file, "avatars", fileName)).thenReturn(url);
        String result = fileUploadService.uploadAvatar(file, userId);
        assertEquals(url, result);
    }
} 