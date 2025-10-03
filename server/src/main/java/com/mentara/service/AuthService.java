package com.mentara.service;

import com.mentara.dto.request.LoginRequest;
import com.mentara.dto.request.SignupRequest;
import com.mentara.dto.request.ResetPasswordRequest;
import com.mentara.dto.request.RefreshTokenRequest;
import com.mentara.dto.response.JwtResponse;
import com.mentara.dto.response.RefreshTokenResponse;
import com.mentara.entity.User;

public interface AuthService {
    
    /**
     * 用户登录
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);
    
    /**
     * 用户注册
     */
    User registerUser(SignupRequest signupRequest);
    
    /**
     * 重置密码
     */
    boolean resetPassword(ResetPasswordRequest resetRequest);
    
    /**
     * 生成测试JWT
     */
    String generateTestJWT();
    
    /**
     * 刷新token
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
} 