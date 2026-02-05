package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 비즈니스 메트릭 헬퍼
 *
 * 사용 예:
 * <pre>
 * @Autowired
 * BusinessMetrics metrics;
 *
 * // 카운터 증가
 * metrics.increment("orders.created", "status", "success");
 *
 * // 게이지 등록
 * metrics.gauge("orders.pending", pendingOrders::size);
 *
 * // 시간 측정
 * metrics.recordTime("orders.processing", processingTimeMs, TimeUnit.MILLISECONDS);
 * </pre>
 */
public class BusinessMetrics {

    private static final Logger log = LoggerFactory.getLogger(BusinessMetrics.class);

    private final MeterRegistry meterRegistry;
    private final String prefix;

    public BusinessMetrics(MeterRegistry meterRegistry, String prefix) {
        this.meterRegistry = meterRegistry;
        this.prefix = prefix;
    }

    /**
     * 카운터 증가
     */
    public void increment(String name, String... tags) {
        Counter.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 카운터 증가 (특정 값만큼)
     */
    public void increment(String name, double amount, String... tags) {
        Counter.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry)
                .increment(amount);
    }

    /**
     * 게이지 등록
     */
    public <T extends Number> void gauge(String name, Supplier<T> valueSupplier, String... tags) {
        Gauge.builder(buildName(name), valueSupplier, supplier -> supplier.get().doubleValue())
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * AtomicLong 게이지 등록
     */
    public AtomicLong gauge(String name, String... tags) {
        AtomicLong value = new AtomicLong(0);
        Gauge.builder(buildName(name), value, AtomicLong::doubleValue)
                .tags(tags)
                .register(meterRegistry);
        return value;
    }

    /**
     * 시간 측정
     */
    public void recordTime(String name, long duration, TimeUnit unit, String... tags) {
        Timer.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry)
                .record(duration, unit);
    }

    /**
     * 코드 블록 시간 측정
     */
    public <T> T recordTime(String name, Supplier<T> supplier, String... tags) {
        Timer timer = Timer.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry);
        return timer.record(supplier);
    }

    /**
     * 코드 블록 시간 측정 (void)
     */
    public void recordTime(String name, Runnable runnable, String... tags) {
        Timer timer = Timer.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry);
        timer.record(runnable);
    }

    /**
     * 분포 요약 기록
     */
    public void recordDistribution(String name, double value, String... tags) {
        DistributionSummary.builder(buildName(name))
                .tags(tags)
                .register(meterRegistry)
                .record(value);
    }

    /**
     * 주문 금액, 결제 금액 등 금액 관련 메트릭
     */
    public void recordAmount(String name, double amount, String currency, String... additionalTags) {
        String[] allTags = new String[additionalTags.length + 2];
        allTags[0] = "currency";
        allTags[1] = currency;
        System.arraycopy(additionalTags, 0, allTags, 2, additionalTags.length);

        DistributionSummary.builder(buildName(name))
                .tags(allTags)
                .register(meterRegistry)
                .record(amount);
    }

    /**
     * 성공/실패 비율 기록
     */
    public void recordOutcome(String name, boolean success, String... tags) {
        String[] allTags = new String[tags.length + 2];
        System.arraycopy(tags, 0, allTags, 0, tags.length);
        allTags[tags.length] = "outcome";
        allTags[tags.length + 1] = success ? "success" : "failure";

        Counter.builder(buildName(name))
                .tags(allTags)
                .register(meterRegistry)
                .increment();
    }

    private String buildName(String name) {
        if (prefix == null || prefix.isEmpty()) {
            return name;
        }
        return prefix + "." + name;
    }
}
