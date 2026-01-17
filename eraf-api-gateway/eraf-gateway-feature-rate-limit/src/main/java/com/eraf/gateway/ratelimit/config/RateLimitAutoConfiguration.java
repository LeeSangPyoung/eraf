package com.eraf.gateway.ratelimit.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.ratelimit.filter.RateLimitFilter;
import com.eraf.gateway.ratelimit.repository.RateLimitRecordRepository;
import com.eraf.gateway.ratelimit.repository.RateLimitRuleRepository;
import com.eraf.gateway.ratelimit.repository.InMemoryRateLimitRecordRepository;
import com.eraf.gateway.ratelimit.repository.InMemoryRateLimitRuleRepository;
import com.eraf.gateway.ratelimit.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rate Limit 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(RateLimitFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitRuleRepository rateLimitRuleRepository() {
        log.info("Initializing InMemoryRateLimitRuleRepository");
        return new InMemoryRateLimitRuleRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitRecordRepository rateLimitRecordRepository() {
        log.info("Initializing InMemoryRateLimitRecordRepository");
        return new InMemoryRateLimitRecordRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitService rateLimitService(
            RateLimitRuleRepository ruleRepository,
            RateLimitRecordRepository recordRepository) {
        log.info("Initializing RateLimitService");
        return new RateLimitService(ruleRepository, recordRepository);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitService rateLimitService,
            RateLimitProperties properties) {
        log.info("Registering RateLimitFilter with order: {}", FilterOrder.RATE_LIMIT);

        RateLimitFilter filter = new RateLimitFilter(rateLimitService, properties.isEnabled());
        filter.setExcludePatterns(properties.getExcludePatterns());

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.RATE_LIMIT);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
