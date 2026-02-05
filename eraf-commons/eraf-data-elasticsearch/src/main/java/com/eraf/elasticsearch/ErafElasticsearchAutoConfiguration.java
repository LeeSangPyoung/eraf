package com.eraf.elasticsearch;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

/**
 * ERAF Elasticsearch Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(ElasticsearchTemplate.class)
@EnableConfigurationProperties(ErafElasticsearchProperties.class)
public class ErafElasticsearchAutoConfiguration {

    /**
     * RestClient 커스터마이저
     * ERAF 설정 기반 타임아웃 적용
     */
    @Bean
    @ConditionalOnMissingBean
    public RestClientBuilderCustomizer erafRestClientBuilderCustomizer(ErafElasticsearchProperties properties) {
        return builder -> builder
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(properties.getConnectionTimeout())
                        .setSocketTimeout(properties.getSocketTimeout())
                );
    }
}
