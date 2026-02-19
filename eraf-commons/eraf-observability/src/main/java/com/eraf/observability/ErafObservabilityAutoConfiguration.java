package com.eraf.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * ERAF Observability Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(OpenTelemetry.class)
@ConditionalOnProperty(name = "eraf.observability.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafObservabilityProperties.class)
public class ErafObservabilityAutoConfiguration {

    /**
     * OpenTelemetry Resource 설정
     */
    @Bean
    @ConditionalOnMissingBean
    public Resource otelResource(ErafObservabilityProperties properties) {
        return Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put(ResourceAttributes.SERVICE_NAME, properties.getServiceName())
                        .put(ResourceAttributes.SERVICE_VERSION, properties.getServiceVersion())
                        .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, properties.getEnvironment())
                        .build()));
    }

    /**
     * OTLP Span Exporter 설정
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public OtlpGrpcSpanExporter otlpSpanExporter(ErafObservabilityProperties properties) {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(properties.getExporter().getEndpoint())
                .setTimeout(properties.getExporter().getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Tracer Provider 설정
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public SdkTracerProvider sdkTracerProvider(
            Resource resource,
            OtlpGrpcSpanExporter spanExporter,
            ErafObservabilityProperties properties) {

        double samplingRate = properties.getTracing().getSamplingRate();
        Sampler sampler = samplingRate >= 1.0
                ? Sampler.alwaysOn()
                : Sampler.traceIdRatioBased(samplingRate);

        return SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                        .setScheduleDelay(1, TimeUnit.SECONDS)
                        .build())
                .setSampler(sampler)
                .build();
    }

    /**
     * OpenTelemetry SDK 설정
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(SdkTracerProvider tracerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
    }

    /**
     * Tracer Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry, ErafObservabilityProperties properties) {
        return openTelemetry.getTracer(properties.getServiceName(), properties.getServiceVersion());
    }

    /**
     * Tracing Utility Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public TracingUtil tracingUtil(Tracer tracer) {
        return new TracingUtil(tracer);
    }

    /**
     * Metrics Utility Bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.observability.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public MetricsUtil metricsUtil(OpenTelemetry openTelemetry) {
        return new MetricsUtil(openTelemetry.getMeter("eraf-metrics"));
    }

    /**
     * @Trace AOP Aspect Bean
     * 선언적 Span 생성을 위한 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean(TraceAspect.class)
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    @ConditionalOnProperty(name = "eraf.observability.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public TraceAspect traceAspect(Tracer tracer) {
        return new TraceAspect(tracer);
    }
}
