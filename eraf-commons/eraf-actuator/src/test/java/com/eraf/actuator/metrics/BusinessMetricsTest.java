package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessMetrics 테스트
 */
class BusinessMetricsTest {

    private MeterRegistry meterRegistry;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        businessMetrics = new BusinessMetrics(meterRegistry);
    }

    @Test
    @DisplayName("카운터 증가")
    void shouldIncrementCounter() {
        businessMetrics.incrementCounter("orders.created", "channel", "web");
        businessMetrics.incrementCounter("orders.created", "channel", "web");
        businessMetrics.incrementCounter("orders.created", "channel", "mobile");

        Counter webCounter = meterRegistry.counter("orders.created", "channel", "web");
        Counter mobileCounter = meterRegistry.counter("orders.created", "channel", "mobile");

        assertEquals(2.0, webCounter.count());
        assertEquals(1.0, mobileCounter.count());
    }

    @Test
    @DisplayName("지정된 양만큼 카운터 증가")
    void shouldIncrementCounterByAmount() {
        businessMetrics.incrementCounter("items.sold", 5, "category", "electronics");
        businessMetrics.incrementCounter("items.sold", 3, "category", "electronics");

        Counter counter = meterRegistry.counter("items.sold", "category", "electronics");

        assertEquals(8.0, counter.count());
    }

    @Test
    @DisplayName("타이머 기록")
    void shouldRecordTimer() {
        businessMetrics.recordTimer("api.latency", 150, TimeUnit.MILLISECONDS, "endpoint", "/users");
        businessMetrics.recordTimer("api.latency", 200, TimeUnit.MILLISECONDS, "endpoint", "/users");

        Timer timer = meterRegistry.timer("api.latency", "endpoint", "/users");

        assertEquals(2, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 350);
    }

    @Test
    @DisplayName("게이지 설정")
    void shouldSetGauge() {
        businessMetrics.gauge("queue.size", 100, "queue", "orders");

        // SimpleMeterRegistry에서 게이지 조회
        var gauge = meterRegistry.find("queue.size").tags("queue", "orders").gauge();
        assertNotNull(gauge);
        assertEquals(100.0, gauge.value());
    }

    @Test
    @DisplayName("게이지 업데이트")
    void shouldUpdateGauge() {
        businessMetrics.gauge("active.connections", 10, "pool", "db");
        businessMetrics.gauge("active.connections", 15, "pool", "db");

        var gauge = meterRegistry.find("active.connections").tags("pool", "db").gauge();
        assertNotNull(gauge);
        assertEquals(15.0, gauge.value());
    }

    @Test
    @DisplayName("태그 없이 카운터 증가")
    void shouldIncrementCounterWithoutTags() {
        businessMetrics.incrementCounter("global.events");
        businessMetrics.incrementCounter("global.events");

        Counter counter = meterRegistry.counter("global.events");
        assertEquals(2.0, counter.count());
    }

    @Test
    @DisplayName("여러 태그로 메트릭 구분")
    void shouldDistinguishMetricsByTags() {
        businessMetrics.incrementCounter("http.requests", "method", "GET", "status", "200");
        businessMetrics.incrementCounter("http.requests", "method", "POST", "status", "201");
        businessMetrics.incrementCounter("http.requests", "method", "GET", "status", "404");

        Counter get200 = meterRegistry.counter("http.requests", "method", "GET", "status", "200");
        Counter post201 = meterRegistry.counter("http.requests", "method", "POST", "status", "201");
        Counter get404 = meterRegistry.counter("http.requests", "method", "GET", "status", "404");

        assertEquals(1.0, get200.count());
        assertEquals(1.0, post201.count());
        assertEquals(1.0, get404.count());
    }

    @Test
    @DisplayName("타이머 통계 조회")
    void shouldProvideTimerStatistics() {
        businessMetrics.recordTimer("db.query", 100, TimeUnit.MILLISECONDS, "operation", "select");
        businessMetrics.recordTimer("db.query", 200, TimeUnit.MILLISECONDS, "operation", "select");
        businessMetrics.recordTimer("db.query", 150, TimeUnit.MILLISECONDS, "operation", "select");

        Timer timer = meterRegistry.timer("db.query", "operation", "select");

        assertEquals(3, timer.count());
        assertEquals(150.0, timer.mean(TimeUnit.MILLISECONDS), 1.0);
        assertTrue(timer.max(TimeUnit.MILLISECONDS) >= 200);
    }
}
