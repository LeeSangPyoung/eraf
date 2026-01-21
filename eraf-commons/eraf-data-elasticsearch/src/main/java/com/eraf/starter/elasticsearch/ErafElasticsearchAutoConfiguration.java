package com.eraf.starter.elasticsearch;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

/**
 * ERAF Elasticsearch Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(ElasticsearchTemplate.class)
@EnableConfigurationProperties(ErafElasticsearchProperties.class)
public class ErafElasticsearchAutoConfiguration {

    // Spring Data Elasticsearch의 기본 Auto Configuration을 사용하되,
    // 추가적인 ERAF 설정이 필요한 경우 여기에 빈 정의 추가
}
