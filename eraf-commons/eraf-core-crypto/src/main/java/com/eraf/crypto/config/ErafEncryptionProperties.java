package com.eraf.crypto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF 설정 암호화 Properties
 */
@ConfigurationProperties(prefix = "eraf.encryption")
public class ErafEncryptionProperties {

    /**
     * 설정 암호화 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 암호화 알고리즘 (기본: PBEWITHHMACSHA512ANDAES_256)
     */
    private String algorithm = "PBEWITHHMACSHA512ANDAES_256";

    /**
     * Key Obtention Iterations (기본: 1000)
     */
    private int keyObtentionIterations = 1000;

    /**
     * Pool Size (기본: 1)
     */
    private int poolSize = 1;

    /**
     * Salt Generator Class (기본: RandomSaltGenerator)
     */
    private String saltGeneratorClassname = "org.jasypt.salt.RandomSaltGenerator";

    /**
     * IV Generator Class (기본: RandomIvGenerator)
     */
    private String ivGeneratorClassname = "org.jasypt.iv.RandomIvGenerator";

    /**
     * String Output Type (기본: base64)
     */
    private String stringOutputType = "base64";

    /**
     * 환경변수에서 암호화 키를 읽을 변수명 (기본: JASYPT_ENCRYPTOR_PASSWORD)
     */
    private String passwordEnvName = "JASYPT_ENCRYPTOR_PASSWORD";

    /**
     * 시스템 속성에서 암호화 키를 읽을 속성명 (기본: jasypt.encryptor.password)
     */
    private String passwordPropertyName = "jasypt.encryptor.password";

    /**
     * 암호화된 값의 Prefix (기본: ENC()
     */
    private String prefix = "ENC(";

    /**
     * 암호화된 값의 Suffix (기본: ))
     */
    private String suffix = ")";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getKeyObtentionIterations() {
        return keyObtentionIterations;
    }

    public void setKeyObtentionIterations(int keyObtentionIterations) {
        this.keyObtentionIterations = keyObtentionIterations;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getSaltGeneratorClassname() {
        return saltGeneratorClassname;
    }

    public void setSaltGeneratorClassname(String saltGeneratorClassname) {
        this.saltGeneratorClassname = saltGeneratorClassname;
    }

    public String getIvGeneratorClassname() {
        return ivGeneratorClassname;
    }

    public void setIvGeneratorClassname(String ivGeneratorClassname) {
        this.ivGeneratorClassname = ivGeneratorClassname;
    }

    public String getStringOutputType() {
        return stringOutputType;
    }

    public void setStringOutputType(String stringOutputType) {
        this.stringOutputType = stringOutputType;
    }

    public String getPasswordEnvName() {
        return passwordEnvName;
    }

    public void setPasswordEnvName(String passwordEnvName) {
        this.passwordEnvName = passwordEnvName;
    }

    public String getPasswordPropertyName() {
        return passwordPropertyName;
    }

    public void setPasswordPropertyName(String passwordPropertyName) {
        this.passwordPropertyName = passwordPropertyName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
