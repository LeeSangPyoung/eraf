package com.eraf.grpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF gRPC 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.grpc")
public class ErafGrpcProperties {

    private Server server = new Server();
    private Client client = new Client();
    private Tls tls = new Tls();

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Tls getTls() {
        return tls;
    }

    public void setTls(Tls tls) {
        this.tls = tls;
    }

    public static class Server {
        /**
         * gRPC 서버 포트
         */
        private int port = 9090;

        /**
         * 최대 메시지 크기 (bytes)
         */
        private int maxMessageSize = 4 * 1024 * 1024; // 4MB

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getMaxMessageSize() {
            return maxMessageSize;
        }

        public void setMaxMessageSize(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
        }
    }

    public static class Client {
        /**
         * 기본 타임아웃 (milliseconds)
         */
        private long defaultTimeout = 30000;

        public long getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(long defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }
    }

    public static class Tls {
        /**
         * TLS 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 인증서 체인 파일 경로 (PEM)
         */
        private String certChainPath;

        /**
         * 개인키 파일 경로 (PEM)
         */
        private String privateKeyPath;

        /**
         * 신뢰 인증서 파일 경로 (PEM, CA certificate)
         */
        private String trustCertPath;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCertChainPath() {
            return certChainPath;
        }

        public void setCertChainPath(String certChainPath) {
            this.certChainPath = certChainPath;
        }

        public String getPrivateKeyPath() {
            return privateKeyPath;
        }

        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        public String getTrustCertPath() {
            return trustCertPath;
        }

        public void setTrustCertPath(String trustCertPath) {
            this.trustCertPath = trustCertPath;
        }
    }
}
