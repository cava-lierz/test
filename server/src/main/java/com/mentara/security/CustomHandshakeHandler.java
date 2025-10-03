package com.mentara.security;

import com.mentara.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtUtils jwtUtils;

    @Autowired
    public CustomHandshakeHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // 1. 优先从拦截器设置的属性中获取用户ID
        Long userId = (Long) attributes.get("userId");

        // 2. 如果未设置，尝试从 Spring Security 上下文获取
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
                userId = ((UserPrincipal) authentication.getPrincipal()).getId();
            }
        }

        // 3. 创建 Principal 对象（使用用户ID）
        if (userId != null) {
            return new StompPrincipal(userId.toString());
        }

        return null;
    }

    // 简单的 Principal 实现（使用用户ID）
    public static class StompPrincipal implements Principal {
        private final String id; // 存储用户ID

        public StompPrincipal(String id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return id; // 返回用户ID
        }

        // 添加获取数字ID的方法
        public Long getId() {
            return Long.parseLong(id);
        }
    }
}