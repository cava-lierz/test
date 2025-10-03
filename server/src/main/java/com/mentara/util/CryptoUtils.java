package com.mentara.util;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类
 * 用于学号SHA-256加密
 */
@Component
public class CryptoUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * 使用SHA-256对学号进行加密
     * @param studentId 原始学号
     * @return 加密后的学号哈希值
     */
    public static String hashStudentId(String studentId) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(studentId.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
    
    /**
     * 验证学号哈希值是否匹配
     * @param studentId 原始学号
     * @param hashedStudentId 存储的哈希值
     * @return 是否匹配
     */
    public static boolean verifyStudentId(String studentId, String hashedStudentId) {
        return hashStudentId(studentId).equals(hashedStudentId);
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 