package com.eraf.gateway.analytics.advanced.config;

import com.eraf.gateway.analytics.advanced.controller.AnalyticsDashboardController;
import com.eraf.gateway.analytics.advanced.export.DatadogExporter;
import com.eraf.gateway.analytics.advanced.export.ElasticsearchExporter;
import com.eraf.gateway.analytics.advanced.export.PrometheusExporter;
import com.eraf.gateway.analytics.advanced.filter.AdvancedAnalyticsFilter;
import com.eraf.gateway.analytics.advanced.repository.InMemoryTimeSeriesRepository;
import com.eraf.gateway.analytics.advanced.repository.TimeSeriesRepository;
import com.eraf.gateway.analytics.advanced.service.AdvancedAnalyticsService;
import com.eraf.gateway.analytics.advanced.service.DashboardService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Advanced Analytics 자동 구성
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AnalyticsAdvancedProperties.class)
@ConditionalOnProperty(
        prefix = "eraf.gateway.analytics-advanced",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class AnalyticsAdvancedAutoConfiguration {

    private final AnalyticsAdvancedProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public TimeSeriesRepository timeSeriesRepository() {
        log.info("Creating InMemoryTimeSeriesRepository with maxSize={}", properties.getMaxStorageSize());
        return new InMemoryTimeSeriesRepository(properties.getMaxStorageSize());
    }

    @Bean
    @ConditionalOnMissingBean
    public AdvancedAnalyticsService advancedAnalyticsService(TimeSeriesRepository repository) {
        log.info("Creating AdvancedAnalyticsService with batchSize={}, asyncThreads={}",
                properties.getBatchSize(), properties.getAsyncThreads());
        return new AdvancedAnalyticsService(
                repository,
                properties.getBatchSize(),
                properties.getAsyncThreads()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "eraf.gateway.analytics-advanced", name = "real-time-dashboard", havingValue = "true")
    public DashboardService dashboardService(
            TimeSeriesRepository repository,
            AdvancedAnalyticsService analyticsService) {
        log.info("Creating DashboardService");
        return new DashboardService(repository, analyticsService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.analytics-advanced.prometheus", name = "enabled", havingValue = "true")
    public PrometheusExporter prometheusExporter(MeterRegistry meterRegistry) {
        String prefix = properties.getPrometheus().getMetricsPrefix();
        log.info("Creating PrometheusExporter with prefix={}", prefix);
        return new PrometheusExporter(meterRegistry, prefix);
    }

    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.analytics-advanced.datadog", name = "enabled", havingValue = "true")
    public DatadogExporter datadogExporter() {
        AnalyticsAdvancedProperties.DatadogExport config = properties.getDatadog();
        log.info("Creating DatadogExporter: host={}, port={}, prefix={}",
                config.getHost(), config.getPort(), config.getMetricsPrefix());
        return new DatadogExporter(
                config.getHost(),
                config.getPort(),
                config.getMetricsPrefix(),
                config.getEnvironment()
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.analytics-advanced.elasticsearch", name = "enabled", havingValue = "true")
    public ElasticsearchExporter elasticsearchExporter() {
        AnalyticsAdvancedProperties.ElasticsearchExport config = properties.getElasticsearch();
        log.info("Creating ElasticsearchExporter: host={}, port={}, indexPrefix={}",
                config.getHost(), config.getPort(), config.getIndexPrefix());
        return new ElasticsearchExporter(
                config.getHost(),
                config.getPort(),
                config.getIndexPrefix()
        );
    }

    @Bean
    public AdvancedAnalyticsFilter advancedAnalyticsFilter(
            AdvancedAnalyticsService analyticsService,
            PrometheusExporter prometheusExporter,
            DatadogExporter datadogExporter) {
        log.info("Creating AdvancedAnalyticsFilter: asyncRecording={}, exportPrometheus={}, exportDatadog={}",
                properties.isAsyncRecording(),
                properties.getPrometheus().isEnabled(),
                properties.getDatadog().isEnabled());

        return new AdvancedAnalyticsFilter(
                analyticsService,
                prometheusExporter,
                datadogExporter,
                properties.isEnabled(),
                properties.isAsyncRecording(),
                properties.getPrometheus().isEnabled(),
                properties.getDatadog().isEnabled()
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway.analytics-advanced", name = "real-time-dashboard", havingValue = "true")
    public AnalyticsDashboardController analyticsDashboardController(DashboardService dashboardService) {
        log.info("Creating AnalyticsDashboardController");
        return new AnalyticsDashboardController(dashboardService);
    }

    /**
     * Filter 순서 설정
     */
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<AdvancedAnalyticsFilter>
    advancedAnalyticsFilterRegistration(AdvancedAnalyticsFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<AdvancedAnalyticsFilter> registration =
                new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);  // Same as basic analytics
        registration.addUrlPatterns("/*");
        log.info("Registered AdvancedAnalyticsFilter with order={}", registration.getOrder());
        return registration;
    }
}
