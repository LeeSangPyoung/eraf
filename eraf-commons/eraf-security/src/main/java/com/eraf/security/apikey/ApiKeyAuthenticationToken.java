package com.eraf.security.apikey;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * API Key 인증 토큰
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;
    private final String clientName;

    /**
     * 인증 전 토큰 생성
     */
    public ApiKeyAuthenticationToken(String apiKey) {
        super(null);
        this.apiKey = apiKey;
        this.clientName = null;
        setAuthenticated(false);
    }

    /**
     * 인증 완료 토큰 생성
     */
    public ApiKeyAuthenticationToken(String apiKey, String clientName, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        this.clientName = clientName;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return clientName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getClientName() {
        return clientName;
    }
}
