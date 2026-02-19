package com.eraf.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * OAuth2/OIDC 로그인 성공 핸들러
 *
 * <p>OAuth2 로그인 성공 시 {@link OAuth2LoginSuccessEvent}를 발행하고,
 * 설정된 URL로 리다이렉트합니다.</p>
 *
 * <h3>이벤트 처리 예시</h3>
 * <pre>
 * {@literal @}EventListener
 * public void onOAuth2LoginSuccess(ErafOAuth2LoginSuccessHandler.OAuth2LoginSuccessEvent event) {
 *     Authentication auth = event.getAuthentication();
 *     // 사용자 정보 저장, 로그 기록 등
 * }
 * </pre>
 */
public class ErafOAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(ErafOAuth2LoginSuccessHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public ErafOAuth2LoginSuccessHandler(ApplicationEventPublisher eventPublisher, String defaultTargetUrl) {
        this.eventPublisher = eventPublisher;
        setDefaultTargetUrl(defaultTargetUrl);
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws ServletException, IOException {
        log.info("OAuth2 login success: principal={}", authentication.getName());
        eventPublisher.publishEvent(new OAuth2LoginSuccessEvent(authentication));
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * OAuth2 로그인 성공 이벤트
     */
    public static class OAuth2LoginSuccessEvent {
        private final Authentication authentication;

        public OAuth2LoginSuccessEvent(Authentication authentication) {
            this.authentication = authentication;
        }

        public Authentication getAuthentication() {
            return authentication;
        }

        public String getPrincipalName() {
            return authentication.getName();
        }
    }
}
