package com.eraf.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    /**
     * JasperReports PDF Generator
     * Only registered when JasperReports is on classpath and enabled
     */
    @Bean
    @ConditionalOnClass(name = "net.sf.jasperreports.engine.JasperReport")
    @ConditionalOnProperty(name = "eraf.report.jasper.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public JasperReportGenerator jasperReportGenerator(ReportProperties properties) {
        String templatePath = properties.getJasper().getDefaultTemplate();
        log.debug("Registering JasperReports generator with template: {}", templatePath);
        return new JasperReportGenerator(templatePath);
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
