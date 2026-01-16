package com.eraf.gateway.starter;

import com.eraf.gateway.analytics.AnalyticsFilter;
import com.eraf.gateway.analytics.AnalyticsRepository;
import com.eraf.gateway.analytics.AnalyticsService;
import com.eraf.gateway.bot.BotDetectionFilter;
import com.eraf.gateway.bot.BotDetector;
import com.eraf.gateway.bot.UserAgentBotDetector;
import com.eraf.gateway.cache.ResponseCacheFilter;
import com.eraf.gateway.cache.ResponseCacheRepository;
import com.eraf.gateway.circuitbreaker.CircuitBreaker;
import com.eraf.gateway.circuitbreaker.CircuitBreakerFilter;
import com.eraf.gateway.circuitbreaker.CircuitBreakerRegistry;
import com.eraf.gateway.filter.ApiKeyAuthFilter;
import com.eraf.gateway.filter.IpRestrictionFilter;
import com.eraf.gateway.filter.RateLimitFilter;
import com.eraf.gateway.jwt.DefaultJwtValidator;
import com.eraf.gateway.jwt.JwtValidationFilter;
import com.eraf.gateway.jwt.JwtValidator;
import com.eraf.gateway.repository.*;
import com.eraf.gateway.service.ApiKeyService;
import com.eraf.gateway.service.IpRestrictionService;
import com.eraf.gateway.service.RateLimitService;
import com.eraf.gateway.store.memory.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * ERAF Gateway 자동 설정
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "eraf.gateway.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafGatewayProperties.class)
public class ErafGatewayAutoConfiguration {

    /**
     * Memory Store 설정 (기본값)
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.gateway.store-type", havingValue = "memory", matchIfMissing = true)
    static class MemoryStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(ApiKeyRepository.class)
        public ApiKeyRepository apiKeyRepository() {
            return new InMemoryApiKeyRepository();
        }

        @Bean
        @ConditionalOnMissingBean(IpRestrictionRepository.class)
        public IpRestrictionRepository ipRestrictionRepository() {
            return new InMemoryIpRestrictionRepository();
        }

        @Bean
        @ConditionalOnMissingBean(RateLimitRuleRepository.class)
        public RateLimitRuleRepository rateLimitRuleRepository() {
            return new InMemoryRateLimitRuleRepository();
        }

        @Bean
        @ConditionalOnMissingBean(RateLimitRecordRepository.class)
        public RateLimitRecordRepository rateLimitRecordRepository() {
            return new InMemoryRateLimitRecordRepository();
        }

        @Bean
        @ConditionalOnMissingBean(AnalyticsRepository.class)
        @ConditionalOnProperty(name = "eraf.gateway.analytics.enabled", havingValue = "true")
        public AnalyticsRepository analyticsRepository(ErafGatewayProperties properties) {
            return new InMemoryAnalyticsRepository(properties.getAnalytics().getMaxRecords());
        }

        @Bean
        @ConditionalOnMissingBean(ResponseCacheRepository.class)
        @ConditionalOnProperty(name = "eraf.gateway.cache.enabled", havingValue = "true")
        public ResponseCacheRepository responseCacheRepository(ErafGatewayProperties properties) {
            return new InMemoryResponseCacheRepository(properties.getCache().getMaxEntries());
        }
    }

    // ==================== 서비스 빈 등록 ====================

    @Bean
    @ConditionalOnMissingBean
    public RateLimitService rateLimitService(RateLimitRuleRepository ruleRepository,
                                              RateLimitRecordRepository recordRepository) {
        return new RateLimitService(ruleRepository, recordRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public IpRestrictionService ipRestrictionService(IpRestrictionRepository repository) {
        return new IpRestrictionService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiKeyService apiKeyService(ApiKeyRepository repository) {
        return new ApiKeyService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.gateway.circuit-breaker.enabled", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(ErafGatewayProperties properties) {
        ErafGatewayProperties.CircuitBreakerConfig config = properties.getCircuitBreaker();
        return new CircuitBreakerRegistry(
                config.getFailureThreshold(),
                config.getSuccessThreshold(),
                config.getOpenTimeoutMs()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.gateway.analytics.enabled", havingValue = "true")
    public AnalyticsService analyticsService(AnalyticsRepository repository) {
        return new AnalyticsService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.gateway.jwt.enabled", havingValue = "true")
    public JwtValidator jwtValidator(ErafGatewayProperties properties) {
        return new DefaultJwtValidator(properties.getJwt().getSecretKey());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.gateway.bot-detection.enabled", havingValue = "true")
    public BotDetector botDetector(ErafGatewayProperties properties) {
        ErafGatewayProperties.BotDetectionConfig config = properties.getBotDetection();
        return new UserAgentBotDetector(config.getAllowedBots(), config.isBlockUnknownBots());
    }

    // ==================== 필터 등록 ====================

    /**
     * Bot Detection 필터 등록 (가장 먼저 실행)
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.bot-detection.enabled", havingValue = "true")
    public FilterRegistrationBean<BotDetectionFilter> botDetectionFilter(
            BotDetector botDetector,
            ErafGatewayProperties properties) {

        BotDetectionFilter filter = new BotDetectionFilter(
                botDetector,
                Arrays.asList(properties.getBotDetection().getExcludePatterns()),
                properties.getBotDetection().isBlockBots()
        );

        FilterRegistrationBean<BotDetectionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        registration.addUrlPatterns("/*");
        registration.setName("botDetectionFilter");

        return registration;
    }

    /**
     * Rate Limiting 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            RateLimitService rateLimitService,
            ErafGatewayProperties properties) {

        RateLimitFilter filter = new RateLimitFilter(
                rateLimitService,
                Arrays.asList(properties.getRateLimit().getExcludePatterns())
        );

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        registration.setName("rateLimitFilter");

        return registration;
    }

    /**
     * IP Restriction 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.ip-restriction.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<IpRestrictionFilter> ipRestrictionFilter(
            IpRestrictionService ipRestrictionService,
            ErafGatewayProperties properties) {

        IpRestrictionFilter filter = new IpRestrictionFilter(
                ipRestrictionService,
                Arrays.asList(properties.getIpRestriction().getExcludePatterns())
        );

        FilterRegistrationBean<IpRestrictionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.addUrlPatterns("/*");
        registration.setName("ipRestrictionFilter");

        return registration;
    }

    /**
     * API Key 인증 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.api-key.enabled", havingValue = "true")
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter(
            ApiKeyService apiKeyService,
            ErafGatewayProperties properties) {

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(
                apiKeyService,
                properties.getApiKey().getHeaderName(),
                Arrays.asList(properties.getApiKey().getExcludePatterns())
        );

        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        registration.addUrlPatterns("/*");
        registration.setName("apiKeyAuthFilter");

        return registration;
    }

    /**
     * JWT Validation 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.jwt.enabled", havingValue = "true")
    public FilterRegistrationBean<JwtValidationFilter> jwtValidationFilter(
            JwtValidator jwtValidator,
            ErafGatewayProperties properties) {

        JwtValidationFilter filter = new JwtValidationFilter(
                jwtValidator,
                Arrays.asList(properties.getJwt().getExcludePatterns()),
                properties.getJwt().getHeaderName()
        );

        FilterRegistrationBean<JwtValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 35);
        registration.addUrlPatterns("/*");
        registration.setName("jwtValidationFilter");

        return registration;
    }

    /**
     * Circuit Breaker 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.circuit-breaker.enabled", havingValue = "true")
    public FilterRegistrationBean<CircuitBreakerFilter> circuitBreakerFilter(
            CircuitBreakerRegistry registry,
            ErafGatewayProperties properties) {

        CircuitBreakerFilter filter = new CircuitBreakerFilter(
                registry,
                Arrays.asList(properties.getCircuitBreaker().getExcludePatterns())
        );

        FilterRegistrationBean<CircuitBreakerFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        registration.addUrlPatterns("/*");
        registration.setName("circuitBreakerFilter");

        return registration;
    }

    /**
     * Response Cache 필터 등록
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.cache.enabled", havingValue = "true")
    public FilterRegistrationBean<ResponseCacheFilter> responseCacheFilter(
            ResponseCacheRepository cacheRepository,
            ErafGatewayProperties properties) {

        // 기본 캐시 룰이 없으므로 빈 리스트로 시작 (프로그래매틱하게 추가 가능)
        ResponseCacheFilter filter = new ResponseCacheFilter(cacheRepository, new ArrayList<>());

        FilterRegistrationBean<ResponseCacheFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        registration.addUrlPatterns("/*");
        registration.setName("responseCacheFilter");

        return registration;
    }

    /**
     * Analytics 필터 등록 (가장 마지막에 실행하여 모든 요청/응답 기록)
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.gateway.analytics.enabled", havingValue = "true")
    public FilterRegistrationBean<AnalyticsFilter> analyticsFilter(
            AnalyticsService analyticsService,
            ErafGatewayProperties properties) {

        AnalyticsFilter filter = new AnalyticsFilter(
                analyticsService,
                Arrays.asList(properties.getAnalytics().getExcludePatterns())
        );

        FilterRegistrationBean<AnalyticsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        registration.addUrlPatterns("/*");
        registration.setName("analyticsFilter");

        return registration;
    }
}
