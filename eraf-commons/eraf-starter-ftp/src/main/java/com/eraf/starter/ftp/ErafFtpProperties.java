package com.eraf.starter.ftp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF FTP/SFTP 설정
 */
@ConfigurationProperties(prefix = "eraf.ftp")
public class ErafFtpProperties {

    /**
     * FTP 타입 (ftp, sftp)
     */
    private FtpType type = FtpType.FTP;

    /**
     * 호스트
     */
    private String host;

    /**
     * 포트
     */
    private int port = 21;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 개인키 경로 (SFTP용)
     */
    private String privateKeyPath;

    /**
     * 개인키 암호 (SFTP용)
     */
    private String privateKeyPassphrase;

    /**
     * 연결 타임아웃 (ms)
     */
    private int connectionTimeout = 10000;

    /**
     * 데이터 타임아웃 (ms)
     */
    private int dataTimeout = 30000;

    /**
     * 패시브 모드 (FTP용)
     */
    private boolean passiveMode = true;

    /**
     * 바이너리 모드
     */
    private boolean binaryMode = true;

    public enum FtpType {
        FTP, SFTP
    }

    public FtpType getType() {
        return type;
    }

    public void setType(FtpType type) {
        this.type = type;
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

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getDataTimeout() {
        return dataTimeout;
    }

    public void setDataTimeout(int dataTimeout) {
        this.dataTimeout = dataTimeout;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public boolean isBinaryMode() {
        return binaryMode;
    }

    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }
}
