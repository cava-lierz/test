package com.mentara.util;

/**
 * 头像工具类
 * 用于生成随机默认头像
 */
public class AvatarUtils {
    
    private static final String AVATAR_BASE_URL = "https://mentara-dev-images.oss-cn-shanghai.aliyuncs.com/avatars/";
    private static final String DEFAULT_AVATAR_BASE_URL = "https://mentara-dev-images.oss-cn-shanghai.aliyuncs.com/avatars/default_avatars/default_avatar";
    private static final String DEFAULT_AVATAR_EXTENSION = ".jpg";
    private static final int TOTAL_DEFAULT_AVATARS = 30;
    
    /**
     * 根据输入字符串生成随机默认头像URL
     * @param input 输入字符串（如用户ID、学号等）
     * @return 默认头像URL
     */
    public static String generateRandomDefaultAvatar(String input) {
        if (input == null || input.isEmpty()) {
            input = String.valueOf(System.currentTimeMillis());
        }
        
        int avatarIndex = Math.abs(input.hashCode() % TOTAL_DEFAULT_AVATARS);
        return DEFAULT_AVATAR_BASE_URL + avatarIndex + DEFAULT_AVATAR_EXTENSION;
    }
    
    /**
     * 根据用户ID生成随机默认头像URL
     * @param userId 用户ID
     * @return 默认头像URL
     */
    public static String generateRandomDefaultAvatar(Long userId) {
        return generateRandomDefaultAvatar(userId.toString());
    }
    
    /**
     * 获取默认头像URL（当用户已删除时使用）
     * @return 默认头像URL
     */
    public static String getDeletedUserAvatar() {
        return AVATAR_BASE_URL + "deleted_avatar.jpg";
    }
} 