package com.eraf.gateway.oauth2.exception;

import java.util.List;

/**
 * 권한 범위 부족 예외
 */
public class InsufficientScopeException extends OAuth2Exception {

    private final List<String> requiredScopes;
    private final List<String> actualScopes;

    public InsufficientScopeException(List<String> requiredScopes, List<String> actualScopes) {
        super(OAuth2ErrorCode.OAUTH2_INSUFFICIENT_SCOPE,
                "Required scopes: " + requiredScopes + ", Actual scopes: " + actualScopes);
        this.requiredScopes = requiredScopes;
        this.actualScopes = actualScopes;
    }

    public List<String> getRequiredScopes() {
        return requiredScopes;
    }

    public List<String> getActualScopes() {
        return actualScopes;
    }
}
