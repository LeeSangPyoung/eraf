package com.eraf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;
import java.util.Objects;

/**
 * HashiCorp Vault 자동 구성
 *
 * <p>{@code eraf.config.cloud.vault.enabled=true}로 활성화됩니다.</p>
 * <p>{@code spring-cloud-starter-vault-config} 의존성이 classpath에 있어야 합니다.</p>
 *
 * <h3>설정 예시</h3>
 * <pre>
 * eraf:
 *   config:
 *     cloud:
 *       vault:
 *         enabled: true
 *         uri: http://vault-server:8200
 *         token: ${VAULT_TOKEN}   # 환경 변수 사용 권장
 *         backend: secret         # KV secrets engine mount path (기본값: secret)
 * </pre>
 */
@AutoConfiguration(before = ErafConfigAutoConfiguration.class)
@ConditionalOnClass(VaultTemplate.class)
@ConditionalOnProperty(prefix = "eraf.config.cloud.vault", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CloudConfigProperties.class)
public class VaultConfigAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VaultConfigAutoConfiguration.class);

    /**
     * VaultTemplate 빈 등록
     *
     * <p>Spring Vault의 핵심 Vault 연동 클라이언트입니다.
     * Token 기반 인증을 사용하며, AppRole 등 다른 인증 방식은 직접 Bean을 등록하여 오버라이드할 수 있습니다.</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public VaultTemplate vaultTemplate(CloudConfigProperties properties) {
        CloudConfigProperties.VaultConfig vaultConfig = properties.getVault();
        String uri = vaultConfig.getUri() != null ? vaultConfig.getUri() : "http://localhost:8200";
        String token = vaultConfig.getToken();

        if (token == null || token.isBlank()) {
            log.warn("Vault token is not configured. Set eraf.config.cloud.vault.token or VAULT_TOKEN env variable.");
        }

        VaultEndpoint endpoint = VaultEndpoint.from(Objects.requireNonNull(URI.create(uri)));
        ClientAuthentication authentication = new TokenAuthentication(token != null ? token : "");

        log.info("Initializing VaultTemplate: uri={}, backend={}", uri, vaultConfig.getBackend());
        return new VaultTemplate(endpoint, authentication);
    }

    /**
     * VaultConfigService 빈 등록
     *
     * <p>Vault 시크릿 조회/저장을 위한 고수준 유틸리티 서비스입니다.</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public VaultConfigService vaultConfigService(VaultTemplate vaultTemplate, CloudConfigProperties properties) {
        log.info("VaultConfigService initialized with backend: {}", properties.getVault().getBackend());
        return new VaultConfigService(vaultTemplate, properties.getVault());
    }
}
