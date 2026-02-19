package com.eraf.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
 * OAuth2/OIDC 로그인 실패 핸들러
 *
 * <p>OAuth2 로그인 실패 시 {@link OAuth2LoginFailureEvent}를 발행하고,
 * 설정된 실패 URL로 리다이렉트합니다.</p>
 *
 * <h3>이벤트 처리 예시</h3>
 * <pre>
 * {@literal @}EventListener
 * public void onOAuth2LoginFailure(ErafOAuth2LoginFailureHandler.OAuth2LoginFailureEvent event) {
 *     log.warn("OAuth2 login failed: {}", event.getErrorMessage());
 * }
 * </pre>
 */
public class ErafOAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(ErafOAuth2LoginFailureHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public ErafOAuth2LoginFailureHandler(ApplicationEventPublisher eventPublisher, String defaultFailureUrl) {
        super(defaultFailureUrl);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {
        log.warn("OAuth2 login failed: {}", exception.getMessage());
        eventPublisher.publishEvent(new OAuth2LoginFailureEvent(exception));
        super.onAuthenticationFailure(request, response, exception);
    }

    /**
     * OAuth2 로그인 실패 이벤트
     */
    public static class OAuth2LoginFailureEvent {
        private final AuthenticationException exception;

        public OAuth2LoginFailureEvent(AuthenticationException exception) {
            this.exception = exception;
        }

        public AuthenticationException getException() {
            return exception;
        }

        public String getErrorMessage() {
            return exception.getMessage();
        }
    }
}
