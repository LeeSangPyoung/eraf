package com.eraf.gateway.analytics.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.analytics.filter.AnalyticsFilter;
import com.eraf.gateway.analytics.repository.AnalyticsRepository;
import com.eraf.gateway.analytics.repository.InMemoryAnalyticsRepository;
import com.eraf.gateway.analytics.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Analytics 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(AnalyticsFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.analytics", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AnalyticsProperties.class)
public class AnalyticsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AnalyticsRepository analyticsRepository() {
        log.info("Initializing InMemoryAnalyticsRepository");
        return new InMemoryAnalyticsRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalyticsService analyticsService(AnalyticsRepository repository) {
        log.info("Initializing AnalyticsService");
        return new AnalyticsService(repository);
    }

    @Bean
    public FilterRegistrationBean<AnalyticsFilter> analyticsFilterRegistration(
            AnalyticsService analyticsService,
            AnalyticsProperties properties) {
        log.info("Registering AnalyticsFilter with order: {}", FilterOrder.ANALYTICS);

        AnalyticsFilter filter = new AnalyticsFilter(analyticsService, properties.isEnabled());
        filter.setExcludePatterns(properties.getExcludePatterns());

        FilterRegistrationBean<AnalyticsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.ANALYTICS);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
