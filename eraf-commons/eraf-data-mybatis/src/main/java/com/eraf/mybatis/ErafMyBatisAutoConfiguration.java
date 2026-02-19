package com.eraf.mybatis;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * ERAF MyBatis Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(SqlSessionFactory.class)
@EnableConfigurationProperties(ErafMyBatisProperties.class)
public class ErafMyBatisAutoConfiguration {

    /**
     * MyBatis Configuration Customizer
     * ERAF 설정 적용 (mapUnderscoreToCamelCase, lazyLoadingEnabled, typeAliasesPackage)
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationCustomizer erafMyBatisConfigurationCustomizer(ErafMyBatisProperties properties) {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(properties.isMapUnderscoreToCamelCase());
            configuration.setLazyLoadingEnabled(properties.isLazyLoadingEnabled());

            // typeAliasesPackage 적용
            String typeAliasesPackage = properties.getTypeAliasesPackage();
            if (typeAliasesPackage != null && !typeAliasesPackage.isEmpty()) {
                configuration.getTypeAliasRegistry().registerAliases(typeAliasesPackage);
            }

            // ERAF 내장 타입 핸들러 등록 (JsonTypeHandler 등)
            configuration.getTypeHandlerRegistry().register("com.eraf.mybatis.handler");
        };
    }

    /**
     * Spring Boot MyBatis 속성에 mapperLocations 적용
     */
    @Bean
    @ConditionalOnBean(MybatisProperties.class)
    public InitializingBean erafMyBatisPropertiesApplier(
            MybatisProperties mybatisProperties,
            ErafMyBatisProperties erafProperties) {
        return () -> {
            // mapperLocations 적용 (기존 설정이 없을 때만)
            if (mybatisProperties.getMapperLocations() == null ||
                mybatisProperties.getMapperLocations().length == 0) {
                String mapperLocations = erafProperties.getMapperLocations();
                if (mapperLocations != null && !mapperLocations.isEmpty()) {
                    mybatisProperties.setMapperLocations(new String[]{mapperLocations});
                }
            }
        };
    }

    /**
     * MyBatis 페이지네이션 인터셉터
     * RowBounds 사용 시 LIMIT/OFFSET을 자동으로 SQL에 추가
     */
    @Bean
    @ConditionalOnMissingBean(PaginationInterceptor.class)
    @ConditionalOnProperty(name = "eraf.mybatis.pagination-enabled", havingValue = "true", matchIfMissing = true)
    public Interceptor erafPaginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * MyBatis 슬로우 쿼리 감지 인터셉터
     * Mapper ID + SQL 정보를 포함하여 JDBC 레벨보다 상세한 슬로우 쿼리 로깅 제공
     */
    @Bean
    @ConditionalOnMissingBean(SlowQueryInterceptor.class)
    @ConditionalOnProperty(name = "eraf.mybatis.slow-query.enabled", havingValue = "true", matchIfMissing = true)
    public Interceptor erafMyBatisSlowQueryInterceptor(ErafMyBatisProperties properties,
                                                        ApplicationEventPublisher eventPublisher) {
        ErafMyBatisProperties.SlowQuery sq = properties.getSlowQuery();
        return new SlowQueryInterceptor(sq.getThresholdMs(), sq.getCriticalThresholdMs(),
                sq.isLogSql(), eventPublisher);
    }
}
