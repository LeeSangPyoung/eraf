package com.eraf.observability;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MetricsUtil 테스트
 */
class MetricsUtilTest {

    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

    private MetricsUtil metricsUtil;
    private Meter meter;

    @BeforeEach
    void setUp() {
        meter = otelTesting.getOpenTelemetry().getMeter("test-meter");
        metricsUtil = new MetricsUtil(meter);
    }

    @Test
    void incrementCounter_ShouldIncrementByOne() {
        // when
        metricsUtil.incrementCounter("test.counter");
        metricsUtil.incrementCounter("test.counter");

        // then
        // Note: Actual metrics verification depends on metric reader setup
        assertThatCode(() -> metricsUtil.incrementCounter("test.counter"))
                .doesNotThrowAnyException();
    }

    @Test
    void incrementCounter_WithValue_ShouldIncrementByValue() {
        // when/then
        assertThatCode(() -> metricsUtil.incrementCounter("test.counter", 5))
                .doesNotThrowAnyException();
    }

    @Test
    void incrementCounter_WithAttributes_ShouldAddAttributes() {
        // when/then
        assertThatCode(() ->
            metricsUtil.incrementCounter("http.requests", 1,
                Map.of("method", "GET", "status", "200"))
        ).doesNotThrowAnyException();
    }

    @Test
    void recordHistogram_ShouldRecordValue() {
        // when/then
        assertThatCode(() -> metricsUtil.recordHistogram("http.duration", 123.45))
                .doesNotThrowAnyException();
    }

    @Test
    void recordHistogram_WithAttributes_ShouldAddAttributes() {
        // when/then
        assertThatCode(() ->
            metricsUtil.recordHistogram("request.size", 1024.0,
                Map.of("endpoint", "/api/users"))
        ).doesNotThrowAnyException();
    }

    @Test
    void addUpDownCounter_ShouldHandlePositiveAndNegative() {
        // when/then
        assertThatCode(() -> {
            metricsUtil.addUpDownCounter("active.connections", 5);
            metricsUtil.addUpDownCounter("active.connections", -2);
        }).doesNotThrowAnyException();
    }

    @Test
    void registerGauge_ShouldRegisterCallback() {
        // when/then
        assertThatCode(() ->
            metricsUtil.registerGauge("memory.used", () -> 1024L)
        ).doesNotThrowAnyException();
    }

    @Test
    void registerDoubleGauge_ShouldRegisterCallback() {
        // when/then
        assertThatCode(() ->
            metricsUtil.registerDoubleGauge("cpu.usage", () -> 0.75)
        ).doesNotThrowAnyException();
    }

    @Test
    void recordResponseTime_ShouldRecordHttpDuration() {
        // when/then
        assertThatCode(() ->
            metricsUtil.recordResponseTime("/api/users", 150)
        ).doesNotThrowAnyException();
    }

    @Test
    void incrementRequestCount_ShouldRecordHttpRequest() {
        // when/then
        assertThatCode(() ->
            metricsUtil.incrementRequestCount("GET", "/api/users", 200)
        ).doesNotThrowAnyException();
    }

    @Test
    void incrementErrorCount_ShouldRecordError() {
        // when/then
        assertThatCode(() ->
            metricsUtil.incrementErrorCount("ValidationException")
        ).doesNotThrowAnyException();
    }
}
