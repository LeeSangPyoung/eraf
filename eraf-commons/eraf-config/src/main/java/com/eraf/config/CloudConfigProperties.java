package com.eraf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cloud Config Properties
 * Spring Cloud Config, Consul, Vault 통합 설정
 */
@ConfigurationProperties(prefix = "eraf.config.cloud")
public class CloudConfigProperties {

    /**
     * Cloud Config 활성화 여부
     */
    private boolean enabled = false;

    /**
     * Config Server URI (Spring Cloud Config)
     */
    private String uri;

    /**
     * Application 이름
     */
    private String name;

    /**
     * Profile (dev, prod 등)
     */
    private String profile = "default";

    /**
     * Label (git branch/tag)
     */
    private String label = "main";

    /**
     * 시작 시 Config Server 연결 실패 시 실패 여부
     */
    private boolean failFast = false;

    /**
     * Config Server 인증 사용자명
     */
    private String username;

    /**
     * Config Server 인증 비밀번호
     *
     * <p><strong>보안 권장사항:</strong> 평문 비밀번호 대신 암호화된 값 사용을 권장합니다.</p>
     *
     * <p>암호화 방법:</p>
     * <pre>
     * 1. Spring Cloud Config 암호화 사용:
     *    password: '{cipher}AQA...(암호화된 값)'
     *
     * 2. Vault 사용:
     *    vault.enabled: true
     *    vault.token: your-vault-token
     *    (Vault에서 자동으로 비밀번호 가져옴)
     *
     * 3. 환경 변수 사용:
     *    password: ${CONFIG_PASSWORD}
     * </pre>
     */
    private String password;

    /**
     * Refresh 설정
     */
    private RefreshConfig refresh = new RefreshConfig();

    /**
     * Consul 설정
     */
    private ConsulConfig consul = new ConsulConfig();

    /**
     * Vault 설정
     */
    private VaultConfig vault = new VaultConfig();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
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

    public RefreshConfig getRefresh() {
        return refresh;
    }

    public void setRefresh(RefreshConfig refresh) {
        this.refresh = refresh;
    }

    public ConsulConfig getConsul() {
        return consul;
    }

    public void setConsul(ConsulConfig consul) {
        this.consul = consul;
    }

    public VaultConfig getVault() {
        return vault;
    }

    public void setVault(VaultConfig vault) {
        this.vault = vault;
    }

    /**
     * Refresh 설정
     */
    public static class RefreshConfig {
        private boolean enabled = true;
        private long intervalSeconds = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(long intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }
    }

    /**
     * Consul 설정
     */
    public static class ConsulConfig {
        private boolean enabled = false;
        private String host = "localhost";
        private int port = 8500;
        private String prefix = "config";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    /**
     * Vault 설정
     */
    public static class VaultConfig {
        private boolean enabled = false;
        private String uri = "http://localhost:8200";

        /**
         * Vault 인증 토큰
         *
         * <p><strong>보안 경고:</strong> 토큰을 설정 파일에 평문으로 저장하지 마세요!</p>
         *
         * <p>권장 방법:</p>
         * <pre>
         * 1. 환경 변수 사용:
         *    token: ${VAULT_TOKEN}
         *
         * 2. Kubernetes Secret:
         *    토큰을 Secret으로 관리하고 환경 변수로 주입
         *
         * 3. AppRole 인증:
         *    role-id와 secret-id를 사용하여 토큰 자동 획득
         * </pre>
         */
        private String token;

        private String backend = "secret";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            this.backend = backend;
        }
    }
}
