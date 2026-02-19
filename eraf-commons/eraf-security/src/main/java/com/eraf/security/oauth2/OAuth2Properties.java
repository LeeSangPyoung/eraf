package com.eraf.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2/OIDC Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.security.oauth2")
public class OAuth2Properties {

    private boolean enabled = false;
    private String successUrl = "/";
    private String failureUrl = "/login?error";
    private Map<String, Provider> providers = new HashMap<>();
    private Client client = new Client();
    private ResourceServer resourceServer = new ResourceServer();

    public static class Provider {
        private String issuerUri;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String jwkSetUri;
        private String userNameAttribute = "sub";

        // Getters and Setters
        public String getIssuerUri() { return issuerUri; }
        public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
        public String getAuthorizationUri() { return authorizationUri; }
        public void setAuthorizationUri(String authorizationUri) { this.authorizationUri = authorizationUri; }
        public String getTokenUri() { return tokenUri; }
        public void setTokenUri(String tokenUri) { this.tokenUri = tokenUri; }
        public String getUserInfoUri() { return userInfoUri; }
        public void setUserInfoUri(String userInfoUri) { this.userInfoUri = userInfoUri; }
        public String getJwkSetUri() { return jwkSetUri; }
        public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
        public String getUserNameAttribute() { return userNameAttribute; }
        public void setUserNameAttribute(String userNameAttribute) { this.userNameAttribute = userNameAttribute; }
    }

    public static class Client {
        private Map<String, Registration> registration = new HashMap<>();

        public static class Registration {
            private String clientId;
            private String clientSecret;
            private String scope;
            private String redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
            private String authorizationGrantType = "authorization_code";

            // Getters and Setters
            public String getClientId() { return clientId; }
            public void setClientId(String clientId) { this.clientId = clientId; }
            public String getClientSecret() { return clientSecret; }
            public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
            public String getScope() { return scope; }
            public void setScope(String scope) { this.scope = scope; }
            public String getRedirectUri() { return redirectUri; }
            public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
            public String getAuthorizationGrantType() { return authorizationGrantType; }
            public void setAuthorizationGrantType(String authorizationGrantType) { this.authorizationGrantType = authorizationGrantType; }
        }

        public Map<String, Registration> getRegistration() { return registration; }
        public void setRegistration(Map<String, Registration> registration) { this.registration = registration; }
    }

    public static class ResourceServer {
        private boolean enabled = false;
        private String jwkSetUri;
        private String issuerUri;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getJwkSetUri() { return jwkSetUri; }
        public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
        public String getIssuerUri() { return issuerUri; }
        public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
    }

    // Main Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSuccessUrl() { return successUrl; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
    public String getFailureUrl() { return failureUrl; }
    public void setFailureUrl(String failureUrl) { this.failureUrl = failureUrl; }
    public Map<String, Provider> getProviders() { return providers; }
    public void setProviders(Map<String, Provider> providers) { this.providers = providers; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public ResourceServer getResourceServer() { return resourceServer; }
    public void setResourceServer(ResourceServer resourceServer) { this.resourceServer = resourceServer; }
}
