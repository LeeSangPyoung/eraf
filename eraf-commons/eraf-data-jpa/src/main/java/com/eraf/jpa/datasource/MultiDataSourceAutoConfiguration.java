package com.eraf.jpa.datasource;

import com.eraf.jpa.ErafJpaProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 멀티 DataSource (Primary + Replica) 자동 구성
 *
 * <p>{@code eraf.jpa.datasource-routing.replica.url}이 설정되면 자동으로 활성화되며,
 * Spring Boot의 {@link DataSourceAutoConfiguration}보다 먼저 실행되어
 * RoutingDataSource를 Primary DataSource로 등록합니다.</p>
 *
 * <h3>동작 원리</h3>
 * <ol>
 *   <li>Primary DataSource: {@code spring.datasource.*} 설정으로 생성 (기존 방식 그대로)</li>
 *   <li>Replica DataSource: {@code eraf.jpa.datasource-routing.replica.*}로 생성</li>
 *   <li>RoutingDataSource: Primary + Replica를 래핑, {@link DataSourceContextHolder}로 동적 전환</li>
 * </ol>
 *
 * <h3>설정 예시</h3>
 * <pre>
 * spring:
 *   datasource:                          # Primary DB (쓰기)
 *     url: jdbc:postgresql://primary:5432/mydb
 *     username: user
 *     password: ${DB_PASSWORD}
 *
 * eraf:
 *   jpa:
 *     datasource-routing:
 *       enabled: true
 *       primary-key: primary             # @DataSourceRouting("primary") 값
 *       read-only-key: readOnly          # @DataSourceRouting("readOnly") 값
 *       replica:                         # Replica DB (읽기)
 *         url: jdbc:postgresql://replica:5432/mydb
 *         username: user
 *         password: ${REPLICA_DB_PASSWORD}
 *         hikari:
 *           maximum-pool-size: 10
 *           pool-name: eraf-replica-pool
 * </pre>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * // 읽기 전용 쿼리 (Replica로 라우팅)
 * {@literal @}DataSourceRouting("readOnly")
 * public List&lt;User&gt; findAll() { ... }
 *
 * // 쓰기 쿼리 (기본: Primary로 라우팅)
 * {@literal @}DataSourceRouting("primary")
 * public void save(User user) { ... }
 * </pre>
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnClass({HikariDataSource.class, AbstractRoutingDataSource.class})
@ConditionalOnProperty(name = "eraf.jpa.datasource-routing.replica.url")
@EnableConfigurationProperties({ErafJpaProperties.class, DataSourceProperties.class})
public class MultiDataSourceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MultiDataSourceAutoConfiguration.class);

    /**
     * Primary DataSource 생성 (spring.datasource.* 설정 사용)
     */
    @Bean("primaryDataSource")
    @ConditionalOnMissingBean(name = "primaryDataSource")
    public DataSource primaryDataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setPoolName("eraf-primary-pool");
        log.info("Primary DataSource created: url={}", dataSourceProperties.getUrl());
        return dataSource;
    }

    /**
     * Replica DataSource 생성 (eraf.jpa.datasource-routing.replica.* 설정 사용)
     *
     * <p>HikariCP 설정도 함께 바인딩됩니다 (hikari.maximum-pool-size 등)</p>
     */
    @Bean("replicaDataSource")
    @ConditionalOnMissingBean(name = "replicaDataSource")
    @ConfigurationProperties(prefix = "eraf.jpa.datasource-routing.replica")
    public DataSource replicaDataSource() {
        log.info("Replica DataSource configured via eraf.jpa.datasource-routing.replica.*");
        return new HikariDataSource();
    }

    /**
     * RoutingDataSource 생성 (Primary + Replica 래핑)
     *
     * <p>{@link DataSourceContextHolder}의 값에 따라 동적으로 DataSource를 선택합니다.</p>
     * <ul>
     *   <li>{@code "primary"} (또는 {@code primaryKey}): Primary DataSource 사용</li>
     *   <li>{@code "readOnly"} (또는 {@code readOnlyKey}): Replica DataSource 사용</li>
     *   <li>컨텍스트 없음: Primary DataSource 사용 (기본값)</li>
     * </ul>
     */
    @Bean("routingDataSource")
    @Primary
    @ConditionalOnMissingBean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource,
            ErafJpaProperties properties) {

        ErafJpaProperties.DataSourceRoutingConfig config = properties.getDatasourceRouting();
        String primaryKey = config.getPrimaryKey();
        String readOnlyKey = config.getReadOnlyKey();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(primaryKey, primaryDataSource);
        targetDataSources.put(readOnlyKey, replicaDataSource);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(Objects.requireNonNull(primaryDataSource));
        routingDataSource.afterPropertiesSet();

        log.info("RoutingDataSource configured: primaryKey='{}', readOnlyKey='{}'", primaryKey, readOnlyKey);
        return routingDataSource;
    }
}
