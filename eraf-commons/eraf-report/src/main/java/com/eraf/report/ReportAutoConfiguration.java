package com.eraf.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * ERAF Report Auto Configuration
 *
 * <p>Automatically configures the report generation framework with built-in
 * CSV and HTML generators. Additional generators (PDF, Excel) are registered
 * automatically when their respective modules (eraf-pdf, eraf-excel) are
 * on the classpath.</p>
 *
 * <p>Disable with:</p>
 * <pre>
 * eraf:
 *   report:
 *     enabled: false
 * </pre>
 */
@AutoConfiguration
@ConditionalOnProperty(name = "eraf.report.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ReportProperties.class)
public class ReportAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReportAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CsvReportGenerator csvReportGenerator() {
        log.debug("Registering CSV report generator");
        return new CsvReportGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public HtmlReportGenerator htmlReportGenerator() {
        log.debug("Registering HTML report generator");
        return new HtmlReportGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportService reportService(ReportProperties properties,
                                       List<ReportGenerator> generators) {
        ReportService service = new ReportService(properties);
        for (ReportGenerator generator : generators) {
            service.registerGenerator(generator);
        }
        log.info("ERAF ReportService initialized with {} generator(s): {}",
                generators.size(),
                generators.stream().map(g -> g.getFormat().name()).toList());
        return service;
    }
}
