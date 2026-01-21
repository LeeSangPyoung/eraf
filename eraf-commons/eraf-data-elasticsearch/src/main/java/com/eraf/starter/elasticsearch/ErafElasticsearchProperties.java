package com.eraf.starter.elasticsearch;

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
}
