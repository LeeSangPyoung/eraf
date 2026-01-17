package com.eraf.gateway.cache.config;

import com.eraf.gateway.cache.domain.CacheRule;
import com.eraf.gateway.cache.filter.ResponseCacheFilter;
import com.eraf.gateway.cache.repository.ResponseCacheRepository;
import com.eraf.gateway.cache.repository.InMemoryResponseCacheRepository;
import com.eraf.gateway.common.filter.FilterOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Cache 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(ResponseCacheFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ResponseCacheRepository responseCacheRepository(CacheProperties properties) {
        log.info("Initializing InMemoryResponseCacheRepository");
        return new InMemoryResponseCacheRepository();
    }

    @Bean
    public FilterRegistrationBean<ResponseCacheFilter> responseCacheFilterRegistration(
            ResponseCacheRepository cacheRepository,
            CacheProperties properties) {
        log.info("Registering ResponseCacheFilter with order: {}", FilterOrder.RESPONSE_CACHE);

        // 기본 캐시 규칙 생성 (추후 동적으로 관리 가능)
        List<CacheRule> rules = createDefaultCacheRules(properties);

        ResponseCacheFilter filter = new ResponseCacheFilter(cacheRepository, rules);

        FilterRegistrationBean<ResponseCacheFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.RESPONSE_CACHE);
        registration.addUrlPatterns("/*");

        return registration;
    }

    private List<CacheRule> createDefaultCacheRules(CacheProperties properties) {
        if (!properties.isEnabled()) {
            return Collections.emptyList();
        }

        // 기본 규칙: GET 메소드만 캐싱
        CacheRule defaultRule = CacheRule.builder()
                .id("default")
                .pathPattern("/**")
                .methods(Collections.singleton("GET"))
                .ttlSeconds(properties.getDefaultTtlSeconds())
                .varyByQueryParams(properties.isVaryByQueryParams())
                .varyByHeaders(properties.isVaryByHeaders())
                .varyHeaders(properties.getVaryHeaders() != null
                    ? Set.copyOf(properties.getVaryHeaders())
                    : Collections.emptySet())
                .enabled(true)
                .build();

        return List.of(defaultRule);
    }
}
