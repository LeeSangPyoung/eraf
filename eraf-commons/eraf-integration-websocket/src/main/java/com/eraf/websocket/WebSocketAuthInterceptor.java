package com.eraf.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 인터셉터
 *
 * 연결 시 토큰 기반 인증을 수행합니다.
 * 쿼리 파라미터 또는 헤더에서 토큰을 추출합니다.
 */
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final String tokenParam;
    private WebSocketTokenValidator tokenValidator;

    public WebSocketAuthInterceptor(String tokenParam) {
        this.tokenParam = tokenParam;
    }

    /**
     * 토큰 검증기 설정
     */
    public void setTokenValidator(WebSocketTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);

        if (token == null || token.isEmpty()) {
            log.warn("WebSocket handshake rejected: no token provided");
            return false;
        }

        if (tokenValidator != null) {
            try {
                String userId = tokenValidator.validate(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    attributes.put("authenticated", true);
                    log.debug("WebSocket handshake authenticated: userId={}", userId);
                    return true;
                }
            } catch (Exception e) {
                log.warn("WebSocket handshake token validation failed: {}", e.getMessage());
                return false;
            }
        }

        // validator가 없으면 토큰 존재만 확인
        attributes.put("token", token);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. 쿼리 파라미터에서 추출
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter(tokenParam);
            if (token != null && !token.isEmpty()) {
                return token;
            }
        }

        // 2. Authorization 헤더에서 추출
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 3. Sec-WebSocket-Protocol 헤더에서 추출 (브라우저 WebSocket에서 활용)
        String protocol = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        if (protocol != null && !protocol.isEmpty()) {
            return protocol;
        }

        return null;
    }
}
