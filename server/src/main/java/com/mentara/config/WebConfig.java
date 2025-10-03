package com.mentara.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 已移除本地文件静态资源配置，现在使用阿里云OSS云存储
} 