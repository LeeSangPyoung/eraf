package com.eraf.observability;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenTelemetry Metrics Utility
 */
public class MetricsUtil {

    private final Meter meter;
    private final Map<String, LongCounter> counters = new ConcurrentHashMap<>();
    private final Map<String, DoubleHistogram> histograms = new ConcurrentHashMap<>();
    private final Map<String, LongUpDownCounter> upDownCounters = new ConcurrentHashMap<>();

    public MetricsUtil(Meter meter) {
        this.meter = meter;
    }

    /**
     * Counter 증가 (단순 증가만 가능)
     *
     * @param name Counter 이름
     */
    public void incrementCounter(String name) {
        incrementCounter(name, 1);
    }

    /**
     * Counter 증가 with value
     *
     * @param name Counter 이름
     * @param value 증가량
     */
    public void incrementCounter(String name, long value) {
        LongCounter counter = counters.computeIfAbsent(name, n ->
                meter.counterBuilder(n)
                        .setDescription("Counter: " + n)
                        .build()
        );
        counter.add(value);
    }

    /**
     * Counter 증가 with attributes
     *
     * @param name Counter 이름
     * @param value 증가량
     * @param attributes 속성 (예: {\"http.method\": \"GET\"})
     */
    public void incrementCounter(String name, long value, Map<String, String> attributes) {
        LongCounter counter = counters.computeIfAbsent(name, n ->
                meter.counterBuilder(n)
                        .setDescription("Counter: " + n)
                        .build()
        );
        counter.add(value, toAttributes(attributes));
    }

    /**
     * Histogram 기록 (분포 측정: 응답 시간, 요청 크기 등)
     *
     * @param name Histogram 이름
     * @param value 측정 값
     */
    public void recordHistogram(String name, double value) {
        DoubleHistogram histogram = histograms.computeIfAbsent(name, n ->
                meter.histogramBuilder(n)
                        .setDescription("Histogram: " + n)
                        .build()
        );
        histogram.record(value);
    }

    /**
     * Histogram 기록 with attributes
     *
     * @param name Histogram 이름
     * @param value 측정 값
     * @param attributes 속성
     */
    public void recordHistogram(String name, double value, Map<String, String> attributes) {
        DoubleHistogram histogram = histograms.computeIfAbsent(name, n ->
                meter.histogramBuilder(n)
                        .setDescription("Histogram: " + n)
                        .build()
        );
        histogram.record(value, toAttributes(attributes));
    }

    /**
     * UpDownCounter 증가/감소 (증가와 감소 모두 가능: 동시 접속자 수 등)
     *
     * @param name UpDownCounter 이름
     * @param value 증가/감소량 (음수 가능)
     */
    public void addUpDownCounter(String name, long value) {
        LongUpDownCounter upDownCounter = upDownCounters.computeIfAbsent(name, n ->
                meter.upDownCounterBuilder(n)
                        .setDescription("UpDownCounter: " + n)
                        .build()
        );
        upDownCounter.add(value);
    }

    /**
     * UpDownCounter 증가/감소 with attributes
     */
    public void addUpDownCounter(String name, long value, Map<String, String> attributes) {
        LongUpDownCounter upDownCounter = upDownCounters.computeIfAbsent(name, n ->
                meter.upDownCounterBuilder(n)
                        .setDescription("UpDownCounter: " + n)
                        .build()
        );
        upDownCounter.add(value, toAttributes(attributes));
    }

    /**
     * Gauge 등록 (관찰 가능한 값: 메모리 사용량, 큐 크기 등)
     *
     * @param name Gauge 이름
     * @param valueProvider 값 제공자
     */
    public void registerGauge(String name, java.util.function.Supplier<Long> valueProvider) {
        meter.gaugeBuilder(name)
                .setDescription("Gauge: " + name)
                .buildWithCallback(measurement -> measurement.record(valueProvider.get().doubleValue()));
    }

    /**
     * Double Gauge 등록
     */
    public void registerDoubleGauge(String name, java.util.function.Supplier<Double> valueProvider) {
        meter.gaugeBuilder(name)
                .setDescription("Gauge: " + name)
                .buildWithCallback(measurement -> measurement.record(valueProvider.get()));
    }

    /**
     * 응답 시간 기록 (milliseconds)
     */
    public void recordResponseTime(String endpoint, long milliseconds) {
        recordHistogram("http.server.duration",
                milliseconds,
                Map.of("http.route", endpoint));
    }

    /**
     * 요청 카운트 증가
     */
    public void incrementRequestCount(String method, String endpoint, int statusCode) {
        incrementCounter("http.server.requests",
                1,
                Map.of(
                        "http.method", method,
                        "http.route", endpoint,
                        "http.status_code", String.valueOf(statusCode)
                ));
    }

    /**
     * 에러 카운트 증가
     */
    public void incrementErrorCount(String errorType) {
        incrementCounter("application.errors",
                1,
                Map.of("error.type", errorType));
    }

    /**
     * Map을 OpenTelemetry Attributes로 변환
     */
    private Attributes toAttributes(Map<String, String> map) {
        var builder = Attributes.builder();
        map.forEach((key, value) ->
                builder.put(AttributeKey.stringKey(key), value));
        return builder.build();
    }
}
