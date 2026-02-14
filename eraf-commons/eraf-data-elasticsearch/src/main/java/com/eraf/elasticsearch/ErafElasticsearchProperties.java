package com.eraf.elasticsearch;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * ERAF Elasticsearch 설정
 */
@ConfigurationProperties(prefix = "eraf.elasticsearch")
public class ErafElasticsearchProperties {

    /**
     * 호스트 목록
     */
    private List<String> hosts = List.of("localhost:9200");

    /**
     * 사용자명
     */
    private String username;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 연결 타임아웃 (ms)
     */
    private int connectionTimeout = 5000;

    /**
     * 소켓 타임아웃 (ms)
     */
    private int socketTimeout = 30000;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * SSL/TLS 설정
     */
    private Ssl ssl = new Ssl();

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public static class Ssl {
        /**
         * SSL 활성화 여부
         */
        private boolean enabled = false;

        /**
         * TrustStore 경로
         */
        private String trustStorePath;

        /**
         * TrustStore 비밀번호
         */
        private String trustStorePassword;

        /**
         * TrustStore 타입 (JKS, PKCS12)
         */
        private String trustStoreType = "PKCS12";

        /**
         * 호스트명 검증 여부
         */
        private boolean verifyHostname = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTrustStorePath() { return trustStorePath; }
        public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }
        public String getTrustStorePassword() { return trustStorePassword; }
        public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
        public String getTrustStoreType() { return trustStoreType; }
        public void setTrustStoreType(String trustStoreType) { this.trustStoreType = trustStoreType; }
        public boolean isVerifyHostname() { return verifyHostname; }
        public void setVerifyHostname(boolean verifyHostname) { this.verifyHostname = verifyHostname; }
    }
}
