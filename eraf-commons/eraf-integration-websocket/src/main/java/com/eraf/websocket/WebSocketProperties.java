package com.eraf.websocket;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.websocket")
public class WebSocketProperties {

    private boolean enabled = true;
    private String endpoint = "/ws";
    private String[] allowedOrigins = {"*"};
    private String appDestinationPrefix = "/app";
    private String[] brokerPrefixes = {"/topic", "/queue"};
    private Auth auth = new Auth();

    public static class Auth {
        private boolean enabled = false;
        private String tokenParam = "token";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTokenParam() { return tokenParam; }
        public void setTokenParam(String tokenParam) { this.tokenParam = tokenParam; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String[] getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(String[] allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    public String getAppDestinationPrefix() { return appDestinationPrefix; }
    public void setAppDestinationPrefix(String appDestinationPrefix) { this.appDestinationPrefix = appDestinationPrefix; }
    public String[] getBrokerPrefixes() { return brokerPrefixes; }
    public void setBrokerPrefixes(String[] brokerPrefixes) { this.brokerPrefixes = brokerPrefixes; }
    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }
}
