package com.eraf.elasticsearch;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * ERAF Elasticsearch Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(ElasticsearchTemplate.class)
@EnableConfigurationProperties(ErafElasticsearchProperties.class)
public class ErafElasticsearchAutoConfiguration {

    /**
     * RestClient 커스터마이저
     * ERAF 설정 기반 타임아웃, 인증, SSL 적용
     */
    @Bean
    @ConditionalOnMissingBean
    public RestClientBuilderCustomizer erafRestClientBuilderCustomizer(ErafElasticsearchProperties properties) {
        return builder -> {
            // 타임아웃 설정
            builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                    .setConnectTimeout(properties.getConnectionTimeout())
                    .setSocketTimeout(properties.getSocketTimeout())
            );

            // 인증 + SSL 설정
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // Basic Auth
                if (properties.getUsername() != null && !properties.getUsername().isEmpty()) {
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                            AuthScope.ANY,
                            new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword())
                    );
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }

                // SSL/TLS
                ErafElasticsearchProperties.Ssl ssl = properties.getSsl();
                if (ssl.isEnabled()) {
                    try {
                        SSLContextBuilder sslBuilder = SSLContextBuilder.create();

                        if (ssl.getTrustStorePath() != null && !ssl.getTrustStorePath().isEmpty()) {
                            KeyStore trustStore = KeyStore.getInstance(
                                    ssl.getTrustStoreType() != null ? ssl.getTrustStoreType() : "PKCS12");
                            try (FileInputStream fis = new FileInputStream(ssl.getTrustStorePath())) {
                                trustStore.load(fis, ssl.getTrustStorePassword() != null
                                        ? ssl.getTrustStorePassword().toCharArray() : null);
                            }
                            sslBuilder.loadTrustMaterial(trustStore, null);
                        } else if (!ssl.isVerifyHostname()) {
                            sslBuilder.loadTrustMaterial(null, (chain, authType) -> true);
                        }

                        SSLContext sslContext = sslBuilder.build();
                        httpClientBuilder.setSSLContext(sslContext);

                        if (!ssl.isVerifyHostname()) {
                            httpClientBuilder.setSSLHostnameVerifier(
                                    org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure SSL for Elasticsearch", e);
                    }
                }

                return httpClientBuilder;
            });
        };
    }

    /**
     * Elasticsearch 인덱스 관리자
     * ElasticsearchOperations가 존재할 때만 등록
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ElasticsearchOperations.class)
    public IndexManager indexManager(ElasticsearchOperations elasticsearchOperations) {
        return new IndexManager(elasticsearchOperations);
    }
}
