package com.mentara.service.impl;

import com.mentara.dto.request.LoginRequest;
import com.mentara.dto.request.SignupRequest;
import com.mentara.dto.request.ResetPasswordRequest;
import com.mentara.dto.request.RefreshTokenRequest;
import com.mentara.dto.response.JwtResponse;
import com.mentara.dto.response.RefreshTokenResponse;
import com.mentara.entity.User;
import com.mentara.security.jwt.JwtUtils;
import com.mentara.service.AuthService;
import com.mentara.service.LlmSessionInitService;
import com.mentara.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private com.mentara.service.OssService ossService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LlmSessionInitService llmSessionInitService;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        String inputStudentId = loginRequest.getUsername();
        
        // 通过Service层查找用户
        Optional<User> userOpt = userService.findByStudentId(inputStudentId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("学号不存在！");
        }
        
        User targetUser = userOpt.get();
        
        // 检查用户是否被禁用
        if (targetUser.getIsDisabled() != null && targetUser.getIsDisabled()) {
            throw new RuntimeException("账户已被禁用，请联系管理员！");
        }
        
        // 通过Service层验证密码
        if (!userService.verifyPassword(loginRequest.getPassword(), targetUser.getUserAuth().getPassword())) {
            throw new RuntimeException("学号或密码错误！");
        }
        
        // 创建认证对象
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            targetUser.getUsername(),
            targetUser.getUserAuth().getPassword(),
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + targetUser.getRole().name()))
        );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 更新最后活跃时间
        userService.updateLastLoginTime(targetUser.getUsername());
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        String avatar = targetUser.getAvatar();

        String presigned = ossService.generatePresignedUrl(avatar, 3600L);
        avatar = presigned;

        return new JwtResponse(jwt,
                refreshToken,
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getNickname(),
                targetUser.getUserAuth().getEmail(),
                targetUser.getRole().name(),
                avatar);
    }

    @Override
    public User registerUser(SignupRequest signupRequest) {
        String inputStudentId = signupRequest.getUsername();
        
        // 检查学号是否已被未删除的用户使用
        if (userService.existsByStudentId(inputStudentId)) {
            throw new RuntimeException("学号已被注册！");
        }
        
        // 检查学号是否被已删除的用户使用
        if (userService.existsByUsernameIncludingDeleted(inputStudentId)) {
            throw new RuntimeException("学号已被使用，无法重新注册！");
        }

        // 通过Service层检查邮箱是否已存在
        if (userService.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // 通过Service层创建用户
        User savedUser = userService.createUser(
            inputStudentId,
            signupRequest.getEmail(),
            signupRequest.getPassword(),
            signupRequest.getNickname()
        );

        // 为新用户初始化默认LLM会话
        llmSessionInitService.initializeDefaultSessions(savedUser);

        return savedUser;
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest resetRequest) {
        // 验证新密码和确认密码是否一致
        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            throw new RuntimeException("新密码和确认密码不一致！");
        }

        // 通过Service层重置密码
        User updatedUser = userService.resetUserPassword(
            resetRequest.getStudentId(),
            resetRequest.getEmail(),
            resetRequest.getNewPassword()
        );
        
        if (updatedUser == null) {
            throw new RuntimeException("学号不存在或学号与邮箱不匹配！");
        }
        
        return true;
    }

    @Override
    public String generateTestJWT() {
        // 创建一个测试用户的认证对象 - 使用数据库中真实存在的管理员用户
        UserDetails testUser = new org.springframework.security.core.userdetails.User(
            "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
            "testpass",
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        Authentication testAuth = new UsernamePasswordAuthenticationToken(
            testUser, null, testUser.getAuthorities());
        
        return jwtUtils.generateJwtToken(testAuth);
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的refresh token");
        }
        String username = jwtUtils.getUserNameFromRefreshToken(refreshToken);
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getUserAuth().getPassword(),
            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        String newAccessToken = jwtUtils.generateJwtToken(authentication);
        String newRefreshToken = jwtUtils.generateRefreshToken(authentication);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }
} 