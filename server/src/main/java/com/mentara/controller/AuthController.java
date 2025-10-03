package com.mentara.controller;

import com.mentara.dto.request.LoginRequest;
import com.mentara.dto.request.SignupRequest;
import com.mentara.dto.request.ResetPasswordRequest;
import com.mentara.dto.request.RefreshTokenRequest;
import com.mentara.dto.response.JwtResponse;
import com.mentara.dto.response.MessageResponse;
import com.mentara.dto.response.RefreshTokenResponse;
import com.mentara.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: 学号或密码错误！"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: 注册失败，请检查输入信息！"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        try {
            authService.resetPassword(resetRequest);
            return ResponseEntity.ok(new MessageResponse("密码重置成功！"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: 密码重置失败，请检查输入信息！"));
        }
    }

    @PostMapping("/test-jwt")
    public ResponseEntity<?> generateTestJWT() {
        try {
            String testJwt = authService.generateTestJWT();
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("testToken", testJwt);
                put("message", "Test JWT generated successfully");
            }});
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error generating test JWT: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: 刷新token失败！"));
        }
    }
}