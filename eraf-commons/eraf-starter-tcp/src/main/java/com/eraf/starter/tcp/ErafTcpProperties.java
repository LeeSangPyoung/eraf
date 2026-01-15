package com.eraf.starter.tcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF TCP 설정
 */
@ConfigurationProperties(prefix = "eraf.tcp")
public class ErafTcpProperties {

    /**
     * 서버 호스트
     */
    private String host = "localhost";

    /**
     * 서버 포트
     */
    private int port = 8888;

    /**
     * 연결 타임아웃 (ms)
     */
    private int connectionTimeout = 10000;

    /**
     * 읽기 타임아웃 (ms)
     */
    private int readTimeout = 30000;

    /**
     * Keep Alive 활성화
     */
    private boolean keepAlive = true;

    /**
     * TCP NoDelay (Nagle 알고리즘 비활성화)
     */
    private boolean tcpNoDelay = true;

    /**
     * 자동 재연결 활성화
     */
    private boolean autoReconnect = true;

    /**
     * 재연결 간격 (ms)
     */
    private long reconnectInterval = 5000;

    /**
     * 최대 재연결 시도 횟수
     */
    private int maxReconnectAttempts = 10;

    /**
     * 버퍼 크기
     */
    private int bufferSize = 8192;

    /**
     * Worker 스레드 수
     */
    private int workerThreads = 0; // 0 = Netty 기본값

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

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(long reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public void setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
}
