package com.eraf.crypto.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF 설정 암호화 Auto Configuration
 * Jasypt 기반 application.yml 암호화 지원
 */
@AutoConfiguration
@ConditionalOnClass(StringEncryptor.class)
@ConditionalOnProperty(name = "eraf.encryption.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafEncryptionProperties.class)
@EnableEncryptableProperties
public class ErafEncryptionAutoConfiguration {

    /**
     * Jasypt String Encryptor Bean
     * 환경변수 또는 시스템 속성에서 암호화 키를 읽어옴
     */
    @Bean(name = "jasyptStringEncryptor")
    @ConditionalOnMissingBean(name = "jasyptStringEncryptor")
    public StringEncryptor stringEncryptor(ErafEncryptionProperties properties) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 암호화 키 읽기: 환경변수 > 시스템 속성 > 빈 문자열 (경고)
        String password = getEncryptionPassword(properties);
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "Encryption password not found. Please set " +
                    "environment variable '" + properties.getPasswordEnvName() + "' or " +
                    "system property '" + properties.getPasswordPropertyName() + "'"
            );
        }

        config.setPassword(password);
        config.setAlgorithm(properties.getAlgorithm());
        config.setKeyObtentionIterations(properties.getKeyObtentionIterations());
        config.setPoolSize(properties.getPoolSize());
        config.setSaltGeneratorClassName(properties.getSaltGeneratorClassname());
        config.setIvGeneratorClassName(properties.getIvGeneratorClassname());
        config.setStringOutputType(properties.getStringOutputType());

        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 암호화 키 읽기 (우선순위: 환경변수 > 시스템 속성)
     */
    private String getEncryptionPassword(ErafEncryptionProperties properties) {
        // 1. 환경변수에서 읽기
        String password = System.getenv(properties.getPasswordEnvName());
        if (password != null && !password.isEmpty()) {
            return password;
        }

        // 2. 시스템 속성에서 읽기
        password = System.getProperty(properties.getPasswordPropertyName());
        if (password != null && !password.isEmpty()) {
            return password;
        }

        return null;
    }
}
