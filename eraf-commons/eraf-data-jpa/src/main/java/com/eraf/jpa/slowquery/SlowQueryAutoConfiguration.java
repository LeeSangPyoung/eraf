package com.eraf.jpa.slowquery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;

/**
 * Slow Query 감지 자동 설정
 */
@Configuration
@ConditionalOnClass({DataSource.class, DelegatingDataSource.class})
@ConditionalOnProperty(prefix = "eraf.jpa.slow-query", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SlowQueryProperties.class)
public class SlowQueryAutoConfiguration {

    /**
     * SlowQueryLoggingDataSource를 원본 DataSource를 감싸는 형태로 등록
     *
     * 주의: 이 방식은 BeanPostProcessor를 통해 DataSource를 교체해야 함
     * 또는 @Primary DataSource를 직접 정의하여 사용
     */
    @Bean
    public SlowQueryProperties slowQueryProperties() {
        return new SlowQueryProperties();
    }

    /**
     * Slow Query Detector (StatementInspector 방식 - Hibernate 전용)
     */
    @Bean
    @ConditionalOnClass(name = "org.hibernate.resource.jdbc.spi.StatementInspector")
    public SlowQueryDetector slowQueryDetector(SlowQueryProperties properties) {
        return new SlowQueryDetector(properties);
    }

    /**
     * DataSource Wrapper 방식
     *
     * 사용자가 application.yml에서 다음과 같이 설정 필요:
     *
     * eraf:
     *   jpa:
     *     slow-query:
     *       enabled: true
     *       threshold-ms: 1000
     *       critical-threshold-ms: 5000
     *       log-as-error: false
     *       log-stack-trace: true
     *       stack-trace-depth: 10
     */
    // 참고: DataSource를 자동으로 Wrap하는 것은 복잡하므로
    // 사용자가 직접 @Primary DataSource를 정의하도록 유도
    // 또는 BeanPostProcessor를 사용
}
