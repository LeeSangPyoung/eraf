package com.eraf.security.oauth2;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 사용자 정보 서비스
 *
 * 현재 인증된 사용자의 OAuth2 토큰 및 사용자 정보를 제공합니다.
 */
public class OAuth2UserInfoService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2UserInfoService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * 현재 사용자의 Access Token 조회
     */
    public String getCurrentAccessToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            return null;
        }

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(),
                oauth2Token.getName());

        return client != null ? client.getAccessToken().getTokenValue() : null;
    }

    /**
     * 현재 사용자의 OAuth2 속성 조회
     */
    public Map<String, Object> getCurrentUserAttributes() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            return Collections.emptyMap();
        }

        return oauth2Token.getPrincipal().getAttributes();
    }

    /**
     * 현재 사용자의 이름 조회
     */
    public String getCurrentUserName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            return null;
        }
        return oauth2Token.getPrincipal().getAttribute("name");
    }

    /**
     * 현재 사용자의 이메일 조회
     */
    public String getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            return null;
        }
        return oauth2Token.getPrincipal().getAttribute("email");
    }
}
