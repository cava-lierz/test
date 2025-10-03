package com.mentara.security.jwt;

import com.mentara.security.jwt.JwtUtils;
import com.mentara.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Autowired
    public JwtHandshakeInterceptor(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // 1. 先尝试从 header 获取
        HttpHeaders headers = request.getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. 如果 header 没有，再从 URL 查询参数获取
        if (token == null) {
            URI uri = request.getURI();
            String query = uri.getQuery(); // 例如 "token=eyJhbGciOi..."
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("token")) {
                        token = pair[1];
                        break;
                    }
                }
            }
        }

        if (token == null) {
            logger.warn("WebSocket 连接缺少 JWT 令牌");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 3. 验证 JWT
        if (!jwtUtils.validateJwtToken(token)) {
            logger.warn("WebSocket JWT 令牌验证失败");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 3. 提取用户名并获取用户ID
        String username = jwtUtils.getUserNameFromJwtToken(token);
        Long userId = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("用户不存在: " + username)).getId();

        if (userId == null) {
            logger.warn("无法找到用户ID: {}", username);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 4. 将用户ID存入属性
        attributes.put("userId", userId);
        logger.info("WebSocket 握手成功: 用户ID {}", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后操作
    }
}