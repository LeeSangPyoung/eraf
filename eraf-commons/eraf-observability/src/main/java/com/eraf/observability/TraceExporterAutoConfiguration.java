package com.eraf.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Trace Exporter 자동 구성
 *
 * eraf.observability.trace-exporter.type 설정에 따라 활성화됩니다.
 * 실제 exporter 인스턴스는 Micrometer/OpenTelemetry 의존성에 의해 제공되며,
 * 이 설정 클래스는 ERAF 프로퍼티를 통해 일관된 구성 인터페이스를 제공합니다.
 *
 * 사용 예:
 * <pre>
 * eraf:
 *   observability:
 *     trace-exporter:
 *       type: OTLP
 *       endpoint: http://localhost:4317
 *       sample-rate: 0.5
 * </pre>
 */
@AutoConfiguration(before = ErafObservabilityAutoConfiguration.class)
@ConditionalOnProperty(name = "eraf.observability.trace-exporter.type", matchIfMissing = false)
@EnableConfigurationProperties(TraceExporterProperties.class)
public class TraceExporterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TraceExporterAutoConfiguration.class);

    public TraceExporterAutoConfiguration(TraceExporterProperties properties) {
        if (properties.getType() != TraceExporterProperties.ExporterType.NONE) {
            log.info("ERAF Trace Exporter configured: type={}, endpoint={}, sampleRate={}",
                    properties.getType(), properties.getEndpoint(), properties.getSampleRate());
        }
    }
}
