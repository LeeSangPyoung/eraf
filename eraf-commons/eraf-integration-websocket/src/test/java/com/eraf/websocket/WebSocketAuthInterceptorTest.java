package com.eraf.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuthInterceptor 단위 테스트")
class WebSocketAuthInterceptorTest {

    private static final String TOKEN_PARAM = "token";

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(TOKEN_PARAM);
    }

    // ──────────────────────────────────────────────
    // beforeHandshake - 토큰 없음
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("beforeHandshake - 토큰이 없으면 핸드셰이크를 거부한다")
    void beforeHandshake_rejectsWhenNoToken() {
        // given
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
    }

    // ──────────────────────────────────────────────
    // beforeHandshake - validator 없이 토큰 존재
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("beforeHandshake - 토큰이 있고 validator가 없으면 허용하고 token 속성을 설정한다")
    void beforeHandshake_withTokenAndNoValidator_returnsTrue() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer my-jwt-token");
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("token", "my-jwt-token");
    }

    // ──────────────────────────────────────────────
    // beforeHandshake - validator 성공
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("beforeHandshake - 유효한 토큰과 validator가 있으면 userId와 authenticated 속성을 설정한다")
    void beforeHandshake_withValidTokenAndValidator_returnsTrue() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token-123");
        when(request.getHeaders()).thenReturn(headers);

        WebSocketTokenValidator validator = mock(WebSocketTokenValidator.class);
        when(validator.validate("valid-token-123")).thenReturn("user-42");
        interceptor.setTokenValidator(validator);

        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes)
                .containsEntry("userId", "user-42")
                .containsEntry("authenticated", true);
    }

    // ──────────────────────────────────────────────
    // beforeHandshake - validator가 null userId 반환
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("beforeHandshake - validator가 null userId를 반환하면 토큰만 저장하고 허용한다")
    void beforeHandshake_validatorReturnsNullUserId_fallsThrough() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer some-token");
        when(request.getHeaders()).thenReturn(headers);

        WebSocketTokenValidator validator = mock(WebSocketTokenValidator.class);
        when(validator.validate("some-token")).thenReturn(null);
        interceptor.setTokenValidator(validator);

        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        // validator 반환값이 null이면 if (userId != null) 분기를 타지 않고
        // validator != null이므로 아래 토큰 저장 코드에 도달하지 않는다 -> false가 아닌 true
        // 소스 코드를 다시 확인: validator != null 블록을 통과 후 아래 코드로 내려감
        // -> attributes.put("token", token); return true;
        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("token", "some-token");
    }

    // ──────────────────────────────────────────────
    // beforeHandshake - validator 예외 발생
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("beforeHandshake - validator에서 예외가 발생하면 핸드셰이크를 거부한다")
    void beforeHandshake_validatorThrowsException_rejects() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer bad-token");
        when(request.getHeaders()).thenReturn(headers);

        WebSocketTokenValidator validator = mock(WebSocketTokenValidator.class);
        when(validator.validate("bad-token")).thenThrow(new RuntimeException("invalid token"));
        interceptor.setTokenValidator(validator);

        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isFalse();
    }

    // ──────────────────────────────────────────────
    // extractToken - Authorization Bearer 헤더
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("extractToken - Authorization Bearer 헤더에서 토큰을 추출한다")
    void extractToken_fromAuthorizationBearerHeader() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer extracted-bearer-token");
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then - 토큰이 추출되면 beforeHandshake가 true를 반환하고 token 속성이 설정됨
        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("token", "extracted-bearer-token");
    }

    // ──────────────────────────────────────────────
    // extractToken - Sec-WebSocket-Protocol 헤더
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("extractToken - Sec-WebSocket-Protocol 헤더에서 토큰을 추출한다")
    void extractToken_fromSecWebSocketProtocolHeader() {
        // given
        HttpHeaders headers = new HttpHeaders();
        headers.set("Sec-WebSocket-Protocol", "ws-protocol-token-abc");
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes).containsEntry("token", "ws-protocol-token-abc");
    }
}
