package com.eraf.websocket;

/**
 * WebSocket 토큰 검증 인터페이스
 *
 * 애플리케이션에서 구현하여 토큰 검증 로직을 제공합니다.
 *
 * 예시:
 * <pre>
 * @Component
 * public class JwtWebSocketTokenValidator implements WebSocketTokenValidator {
 *     private final JwtTokenProvider jwtTokenProvider;
 *
 *     public String validate(String token) {
 *         return jwtTokenProvider.getUserIdFromToken(token);
 *     }
 * }
 * </pre>
 */
public interface WebSocketTokenValidator {
    /**
     * 토큰 검증 및 사용자 ID 반환
     * @param token 인증 토큰
     * @return 사용자 ID (null이면 인증 실패)
     */
    String validate(String token);
}
