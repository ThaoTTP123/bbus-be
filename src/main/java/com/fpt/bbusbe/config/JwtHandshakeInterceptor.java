package com.fpt.bbusbe.config;

import com.fpt.bbusbe.model.enums.TokenType;
import com.fpt.bbusbe.service.JwtService;
import com.fpt.bbusbe.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    private final UserService userService;

    public JwtHandshakeInterceptor(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        final String authHeader = request.getHeaders().getFirst(AUTHORIZATION);
        if (StringUtils.hasLength(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("token: {}...", token.substring(0, 15));
            String phone = "";
            try {
                phone = jwtService.extractPhone(token, TokenType.ACCESS_TOKEN);
                log.info("phone: {}", phone);
                userService.findByUsername(phone);
            } catch (AccessDeniedException e) {
                log.info(e.getMessage());
                response.setStatusCode(HttpStatusCode.valueOf(HttpServletResponse.SC_FORBIDDEN));
                return false; // Deny handshake
            }
        }
        return false; // Deny handshake
    }

    private boolean isAuthorized(String phone, String busId) {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}

