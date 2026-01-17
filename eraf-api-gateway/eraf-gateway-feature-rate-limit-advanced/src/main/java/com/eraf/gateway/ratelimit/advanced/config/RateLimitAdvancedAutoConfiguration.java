package com.eraf.gateway.ratelimit.advanced.config;

import com.eraf.gateway.ratelimit.advanced.domain.AdvancedRateLimitRule;
import com.eraf.gateway.ratelimit.advanced.filter.AdvancedRateLimitFilter;
import com.eraf.gateway.ratelimit.advanced.repository.RedisRateLimitRepository;
import com.eraf.gateway.ratelimit.advanced.service.AdvancedRateLimitService;
import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 고급 Rate Limit AutoConfiguration
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RateLimitAdvancedProperties.class)
@ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitAdvancedAutoConfiguration {

    /**
     * Redis Connection Factory
     */
    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "distributed-mode", havingValue = "true")
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public RedisConnectionFactory redisConnectionFactory(RateLimitAdvancedProperties properties) {
        RateLimitAdvancedProperties.RedisConfig redisConfig = properties.getRedis();

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisConfig.getHost());
        config.setPort(redisConfig.getPort());
        config.setDatabase(redisConfig.getDatabase());

        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) {
            config.setPassword(redisConfig.getPassword());
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        log.info("Redis connection factory created for advanced rate limiting: {}:{}",
                redisConfig.getHost(), redisConfig.getPort());

        return factory;
    }

    /**
     * Redis Template
     */
    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "distributed-mode", havingValue = "true")
    @ConditionalOnMissingBean(name = "rateLimitRedisTemplate")
    public RedisTemplate<String, String> rateLimitRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        log.info("Redis template created for advanced rate limiting");

        return template;
    }

    /**
     * Redis Rate Limit Repository
     */
    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "distributed-mode", havingValue = "true")
    @ConditionalOnMissingBean
    public RedisRateLimitRepository redisRateLimitRepository(RedisTemplate<String, String> rateLimitRedisTemplate) {
        log.info("Redis rate limit repository created");
        return new RedisRateLimitRepository(rateLimitRedisTemplate);
    }

    /**
     * Advanced Rate Limit Rules
     */
    @Bean
    @ConditionalOnMissingBean
    public List<AdvancedRateLimitRule> advancedRateLimitRules(RateLimitAdvancedProperties properties) {
        List<AdvancedRateLimitRule> rules = new ArrayList<>();

        // 기본 규칙 생성
        AdvancedRateLimitRule defaultRule = AdvancedRateLimitRule.builder()
                .id("default-advanced")
                .name("Default Advanced Rate Limit")
                .pathPattern("/**")
                .type(RateLimitRule.RateLimitType.IP)
                .algorithm(properties.getDefaultAlgorithm())
                .windowSeconds(properties.getDefaultWindowSeconds())
                .maxRequests(properties.getDefaultMaxRequests())
                .burstSize(properties.getDefaultBurstSize())
                .refillRate(properties.getDefaultRefillRate())
                .distributedMode(properties.isDistributedMode())
                .enabled(true)
                .priority(1000)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        rules.add(defaultRule);

        // 경로별 규칙 생성
        for (Map.Entry<String, Integer> entry : properties.getPathLimits().entrySet()) {
            String path = entry.getKey();
            Integer limit = entry.getValue();

            AdvancedRateLimitRule pathRule = AdvancedRateLimitRule.builder()
                    .id("path-" + path.hashCode())
                    .name("Path Rule: " + path)
                    .pathPattern(path)
                    .type(RateLimitRule.RateLimitType.IP)
                    .algorithm(properties.getPathAlgorithms().getOrDefault(path, properties.getDefaultAlgorithm()))
                    .windowSeconds(properties.getDefaultWindowSeconds())
                    .maxRequests(limit)
                    .burstSize((int) (limit * 1.5))
                    .refillRate(limit / (double) properties.getDefaultWindowSeconds())
                    .distributedMode(properties.isDistributedMode())
                    .enabled(true)
                    .priority(100)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            rules.add(pathRule);
        }

        log.info("Created {} advanced rate limit rules", rules.size());

        return rules;
    }

    /**
     * Advanced Rate Limit Service (with Redis - distributed mode)
     */
    @Bean
    @ConditionalOnMissingBean(AdvancedRateLimitService.class)
    @ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "distributed-mode", havingValue = "true")
    public AdvancedRateLimitService advancedRateLimitService(
            List<AdvancedRateLimitRule> rules,
            RateLimitAdvancedProperties properties,
            RedisRateLimitRepository redisRepository) {

        log.info("Creating advanced rate limit service with {} rules, distributed mode: {}",
                rules.size(), properties.isDistributedMode());

        return new AdvancedRateLimitService(rules, redisRepository, properties.isDistributedMode());
    }

    /**
     * Advanced Rate Limit Service (without Redis - local mode)
     */
    @Bean
    @ConditionalOnMissingBean(AdvancedRateLimitService.class)
    @ConditionalOnProperty(prefix = "eraf.gateway.rate-limit-advanced", name = "distributed-mode", havingValue = "false", matchIfMissing = true)
    public AdvancedRateLimitService advancedRateLimitServiceLocal(
            List<AdvancedRateLimitRule> rules) {

        log.info("Creating advanced rate limit service (local mode) with {} rules", rules.size());

        return new AdvancedRateLimitService(rules, null, false);
    }

    /**
     * Advanced Rate Limit Filter
     */
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<AdvancedRateLimitFilter> advancedRateLimitFilterRegistration(
            AdvancedRateLimitService service,
            RateLimitAdvancedProperties properties) {

        AdvancedRateLimitFilter filter = new AdvancedRateLimitFilter(service, properties.isEnabled());
        filter.setExcludePatterns(properties.getExcludePatterns());

        FilterRegistrationBean<AdvancedRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(2); // 기본 rate limit 필터와 동일한 순서
        registration.addUrlPatterns("/*");

        log.info("Advanced rate limit filter registered with order: 2");

        return registration;
    }
}
