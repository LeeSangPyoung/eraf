package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * 커스텀 메트릭 수집기
 * BusinessMetrics보다 더 유연하고 고급 기능 제공
 */
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 타이머 생성 또는 조회 (재사용)
     */
    public Timer timer(String name, String... tags) {
        String key = buildKey(name, tags);
        return timers.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .tags(tags)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .publishPercentileHistogram()
                        .register(meterRegistry)
        );
    }

    /**
     * 카운터 생성 또는 조회 (재사용)
     */
    public Counter counter(String name, String... tags) {
        String key = buildKey(name, tags);
        return counters.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 시간 측정 샘플 시작
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 시간 측정 샘플 종료
     */
    public long stopTimer(Timer.Sample sample, String name, String... tags) {
        return sample.stop(timer(name, tags));
    }

    /**
     * 동적 태그로 시간 측정
     */
    public <T> T timeWithTags(String name, Supplier<T> supplier, Map<String, String> tags) {
        String[] tagArray = flattenTags(tags);
        return timer(name, tagArray).record(supplier);
    }

    /**
     * 동적 태그로 카운트
     */
    public void countWithTags(String name, Map<String, String> tags) {
        String[] tagArray = flattenTags(tags);
        counter(name, tagArray).increment();
    }

    /**
     * Long Task Timer (긴 작업 측정)
     */
    public LongTaskTimer longTaskTimer(String name, String... tags) {
        return LongTaskTimer.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Function Timer (함수 기반 타이머)
     */
    public <T> void functionTimer(String name, T obj,
                                   ToLongFunction<T> countFunction,
                                   ToDoubleFunction<T> totalTimeFunction,
                                   TimeUnit timeUnit,
                                   String... tags) {
        FunctionTimer.builder(name, obj, countFunction, totalTimeFunction, timeUnit)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Function Counter (함수 기반 카운터)
     */
    public <T> void functionCounter(String name, T obj,
                                     ToDoubleFunction<T> countFunction,
                                     String... tags) {
        FunctionCounter.builder(name, obj, countFunction)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Time Gauge (시간 기반 게이지)
     */
    public <T> void timeGauge(String name, T obj,
                              TimeUnit timeUnit,
                              ToDoubleFunction<T> valueFunction,
                              String... tags) {
        TimeGauge.builder(name, obj, timeUnit, valueFunction)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Distribution Summary with Percentiles
     */
    public DistributionSummary distributionSummary(String name, String... tags) {
        return DistributionSummary.builder(name)
                .tags(tags)
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    /**
     * SLA 기반 Timer (응답 시간 SLA 측정)
     */
    public Timer slaTimer(String name, Duration... sla) {
        return Timer.builder(name)
                .serviceLevelObjectives(sla)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * 최대값 게이지
     */
    public <T> void maxGauge(String name, T obj, ToDoubleFunction<T> valueFunction, String... tags) {
        Gauge.builder(name, obj, valueFunction)
                .tags(tags)
                .strongReference(true)  // 강한 참조로 GC 방지
                .register(meterRegistry);
    }

    /**
     * 메트릭 제거
     */
    public void remove(Meter.Id id) {
        meterRegistry.remove(id);
        // 캐시에서도 제거
        timers.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));
        counters.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));
    }

    /**
     * 메트릭 이름으로 조회
     */
    public Meter find(String name, String... tags) {
        return meterRegistry.find(name).tags(tags).meter();
    }

    /**
     * 모든 메트릭 조회
     */
    public Iterable<Meter> getMeters() {
        return meterRegistry.getMeters();
    }

    /**
     * Tags를 String 배열로 변환
     */
    private String[] flattenTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }

        String[] result = new String[tags.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            result[index++] = entry.getKey();
            result[index++] = entry.getValue();
        }
        return result;
    }

    /**
     * 캐시 키 생성
     */
    private String buildKey(String name, String[] tags) {
        StringBuilder sb = new StringBuilder(name);
        for (int i = 0; i < tags.length; i += 2) {
            sb.append(":").append(tags[i]).append("=").append(tags[i + 1]);
        }
        return sb.toString();
    }
}
