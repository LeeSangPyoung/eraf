package com.eraf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Config 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.config")
public class ErafConfigProperties {

    /**
     * Config Server 설정
     */
    private Server server = new Server();

    /**
     * Config Client 설정
     */
    private Client client = new Client();

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

    public static class Server {
        /**
         * Git 저장소 URI
         */
        private String gitUri = "https://github.com/example/config-repo";

        /**
         * Git 브랜치
         */
        private String gitBranch = "main";

        /**
         * Git 사용자명
         */
        private String gitUsername;

        /**
         * Git 비밀번호
         */
        private String gitPassword;

        /**
         * 로컬 파일 시스템 경로
         */
        private String nativeSearchLocations = "classpath:/config";

        /**
         * 기본 프로파일
         */
        private String defaultLabel = "main";

        /**
         * Clone on startup
         */
        private boolean cloneOnStart = true;

        public String getGitUri() {
            return gitUri;
        }

        public void setGitUri(String gitUri) {
            this.gitUri = gitUri;
        }

        public String getGitBranch() {
            return gitBranch;
        }

        public void setGitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
        }

        public String getGitUsername() {
            return gitUsername;
        }

        public void setGitUsername(String gitUsername) {
            this.gitUsername = gitUsername;
        }

        public String getGitPassword() {
            return gitPassword;
        }

        public void setGitPassword(String gitPassword) {
            this.gitPassword = gitPassword;
        }

        public String getNativeSearchLocations() {
            return nativeSearchLocations;
        }

        public void setNativeSearchLocations(String nativeSearchLocations) {
            this.nativeSearchLocations = nativeSearchLocations;
        }

        public String getDefaultLabel() {
            return defaultLabel;
        }

        public void setDefaultLabel(String defaultLabel) {
            this.defaultLabel = defaultLabel;
        }

        public boolean isCloneOnStart() {
            return cloneOnStart;
        }

        public void setCloneOnStart(boolean cloneOnStart) {
            this.cloneOnStart = cloneOnStart;
        }
    }

    public static class Client {
        /**
         * Config Server URI
         */
        private String uri = "http://localhost:8888";

        /**
         * 애플리케이션 이름
         */
        private String name = "application";

        /**
         * 프로파일
         */
        private String profile = "default";

        /**
         * Label (Git 브랜치)
         */
        private String label = "main";

        /**
         * Fail fast
         */
        private boolean failFast = false;

        /**
         * 재시도 횟수
         */
        private int maxRetries = 3;

        /**
         * 재시도 간격 (ms)
         */
        private long retryInterval = 1000;

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

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
        }
    }
}
