package com.eraf.actuator;

import com.eraf.actuator.metrics.BusinessMetrics;
import com.eraf.actuator.tracing.TraceContext;
import com.eraf.actuator.tracing.TraceContextHolder;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * 메트릭 + 분산 추적 통합 테스트
 * Micrometer 메트릭과 TraceContext의 통합 동작 검증
 */
class ObservabilityIntegrationTest {

    private MeterRegistry meterRegistry;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        businessMetrics = new BusinessMetrics(meterRegistry, "eraf.test");
        TraceContextHolder.clear(); // 각 테스트 전에 초기화
    }

    @Test
    void metrics_CounterShouldIncrement() {
        // when
        businessMetrics.increment("orders.created", "status", "success");
        businessMetrics.increment("orders.created", "status", "success");
        businessMetrics.increment("orders.created", "status", "failed");

        // then
        Counter successCounter = meterRegistry.find("eraf.test.orders.created")
                .tag("status", "success")
                .counter();
        Counter failedCounter = meterRegistry.find("eraf.test.orders.created")
                .tag("status", "failed")
                .counter();

        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isEqualTo(2);
        assertThat(failedCounter.count()).isEqualTo(1);
    }

    @Test
    void metrics_GaugeShouldTrackValue() {
        // given
        AtomicLong pendingOrders = businessMetrics.gauge("orders.pending");

        // when
        pendingOrders.set(10);
        pendingOrders.incrementAndGet();
        pendingOrders.incrementAndGet();

        // then
        Gauge gauge = meterRegistry.find("eraf.test.orders.pending").gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(12.0);
    }

    @Test
    void metrics_TimerShouldRecordDuration() {
        // when
        businessMetrics.recordTime("api.processing", 100, TimeUnit.MILLISECONDS);
        businessMetrics.recordTime("api.processing", 200, TimeUnit.MILLISECONDS);

        // then
        Timer timer = meterRegistry.find("eraf.test.api.processing").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(300);
    }

    @Test
    void metrics_TimerShouldMeasureCodeBlock() {
        // when
        String result = businessMetrics.recordTime("api.process", () -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "completed";
        });

        // then
        assertThat(result).isEqualTo("completed");
        Timer timer = meterRegistry.find("eraf.test.api.process").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(40);
    }

    @Test
    void metrics_DistributionSummaryShouldRecordValues() {
        // when
        businessMetrics.recordDistribution("order.amount", 1000.0);
        businessMetrics.recordDistribution("order.amount", 2000.0);
        businessMetrics.recordDistribution("order.amount", 1500.0);

        // then
        DistributionSummary summary = meterRegistry.find("eraf.test.order.amount")
                .summary();
        assertThat(summary).isNotNull();
        assertThat(summary.count()).isEqualTo(3);
        assertThat(summary.totalAmount()).isEqualTo(4500.0);
        assertThat(summary.mean()).isEqualTo(1500.0);
    }

    @Test
    void metrics_RecordOutcomeShouldTrackSuccessAndFailure() {
        // when
        businessMetrics.recordOutcome("payment.process", true);
        businessMetrics.recordOutcome("payment.process", true);
        businessMetrics.recordOutcome("payment.process", false);

        // then
        Counter successCounter = meterRegistry.find("eraf.test.payment.process")
                .tag("outcome", "success")
                .counter();
        Counter failureCounter = meterRegistry.find("eraf.test.payment.process")
                .tag("outcome", "failure")
                .counter();

        assertThat(successCounter.count()).isEqualTo(2);
        assertThat(failureCounter.count()).isEqualTo(1);
    }

    @Test
    void metrics_RecordAmountShouldIncludeCurrency() {
        // when
        businessMetrics.recordAmount("order.total", 50000.0, "KRW", "region", "seoul");
        businessMetrics.recordAmount("order.total", 100.0, "USD", "region", "newyork");

        // then
        DistributionSummary krwSummary = meterRegistry.find("eraf.test.order.total")
                .tag("currency", "KRW")
                .tag("region", "seoul")
                .summary();
        DistributionSummary usdSummary = meterRegistry.find("eraf.test.order.total")
                .tag("currency", "USD")
                .tag("region", "newyork")
                .summary();

        assertThat(krwSummary.totalAmount()).isEqualTo(50000.0);
        assertThat(usdSummary.totalAmount()).isEqualTo(100.0);
    }

    @Test
    void tracing_TraceContextShouldHoldTraceInfo() {
        // when
        TraceContext context = TraceContext.create();

        // then
        assertThat(context.getTraceId()).isNotNull();
        assertThat(context.getSpanId()).isNotNull();
        assertThat(context.getTraceId()).hasSize(16); // generateId() produces 16-char string
        assertThat(context.getSpanId()).hasSize(16);
    }

    @Test
    void tracing_TraceContextHolderShouldManageContext() {
        // when
        TraceContext context1 = TraceContext.create();
        TraceContextHolder.setContext(context1);

        // then
        TraceContext retrieved = TraceContextHolder.getContext();
        assertThat(retrieved).isSameAs(context1);
        assertThat(retrieved.getTraceId()).isEqualTo(context1.getTraceId());

        // cleanup
        TraceContextHolder.clear();
        assertThat(TraceContextHolder.getContext()).isNull();
    }

    @Test
    void tracing_NestedSpansShouldShareTraceId() {
        // given
        TraceContext parentContext = TraceContext.create();
        TraceContextHolder.setContext(parentContext);

        // when - 자식 스팬 생성
        TraceContext childContext = TraceContext.createChild(parentContext);

        // then
        assertThat(childContext.getTraceId()).isEqualTo(parentContext.getTraceId());
        assertThat(childContext.getParentSpanId()).isEqualTo(parentContext.getSpanId());
        assertThat(childContext.getSpanId()).isNotEqualTo(parentContext.getSpanId());

        TraceContextHolder.clear();
    }

    @Test
    void integration_MetricsWithTracing() {
        // given - 추적 컨텍스트 설정
        TraceContext traceContext = TraceContext.create();
        TraceContextHolder.setContext(traceContext);

        // when - 메트릭 기록과 동시에 추적 정보 사용
        businessMetrics.increment("api.calls",
                "traceId", traceContext.getTraceId(),
                "spanId", traceContext.getSpanId());

        // then
        Counter counter = meterRegistry.find("eraf.test.api.calls")
                .tag("traceId", traceContext.getTraceId())
                .tag("spanId", traceContext.getSpanId())
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1);

        TraceContextHolder.clear();
    }

    @Test
    void integration_TimingWithTracing() {
        // given
        TraceContext traceContext = TraceContext.create();
        TraceContextHolder.setContext(traceContext);

        // when - 추적 정보를 포함한 시간 측정
        businessMetrics.recordTime("api.process", () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "traceId", traceContext.getTraceId());

        // then
        Timer timer = meterRegistry.find("eraf.test.api.process")
                .tag("traceId", traceContext.getTraceId())
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);

        TraceContextHolder.clear();
    }

    @Test
    void integration_MultipleMetricsWithSameTrace() {
        // given - 하나의 요청에서 여러 메트릭 기록
        TraceContext traceContext = TraceContext.create();
        TraceContextHolder.setContext(traceContext);
        String traceId = traceContext.getTraceId();

        // when - 요청 처리 시뮬레이션
        businessMetrics.increment("request.received", "traceId", traceId);
        businessMetrics.recordTime("request.validation", 10, TimeUnit.MILLISECONDS, "traceId", traceId);
        businessMetrics.recordTime("request.processing", 100, TimeUnit.MILLISECONDS, "traceId", traceId);
        businessMetrics.recordOutcome("request.result", true, "traceId", traceId);

        // then - 모든 메트릭이 동일한 traceId로 태깅됨
        assertThat(meterRegistry.find("eraf.test.request.received")
                .tag("traceId", traceId).counter()).isNotNull();
        assertThat(meterRegistry.find("eraf.test.request.validation")
                .tag("traceId", traceId).timer()).isNotNull();
        assertThat(meterRegistry.find("eraf.test.request.processing")
                .tag("traceId", traceId).timer()).isNotNull();
        assertThat(meterRegistry.find("eraf.test.request.result")
                .tag("traceId", traceId).tag("outcome", "success").counter()).isNotNull();

        TraceContextHolder.clear();
    }

    @Test
    void integration_MetricsAggregationAcrossTraces() {
        // when - 여러 추적에서 메트릭 수집
        for (int i = 0; i < 5; i++) {
            TraceContext context = TraceContext.create();
            TraceContextHolder.setContext(context);

            businessMetrics.increment("requests.total");
            businessMetrics.recordTime("requests.duration", 100 + i * 10, TimeUnit.MILLISECONDS);

            TraceContextHolder.clear();
        }

        // then - traceId 태그 없이 집계
        Counter totalCounter = meterRegistry.find("eraf.test.requests.total").counter();
        Timer totalTimer = meterRegistry.find("eraf.test.requests.duration").timer();

        assertThat(totalCounter.count()).isEqualTo(5);
        assertThat(totalTimer.count()).isEqualTo(5);
        assertThat(totalTimer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(600); // 100+110+120+130+140
    }

    @Test
    void metrics_PrefixShouldBeApplied() {
        // when
        businessMetrics.increment("custom.metric");

        // then
        Counter counter = meterRegistry.find("eraf.test.custom.metric").counter();
        assertThat(counter).isNotNull();
    }

    @Test
    void metrics_TagsShouldFilterCorrectly() {
        // when
        businessMetrics.increment("api.calls", "method", "GET", "status", "200");
        businessMetrics.increment("api.calls", "method", "POST", "status", "201");
        businessMetrics.increment("api.calls", "method", "GET", "status", "404");

        // then - Micrometer에서 각 태그 조합은 별도의 카운터
        // GET+200과 GET+404는 서로 다른 카운터
        Counter get200Counter = meterRegistry.find("eraf.test.api.calls")
                .tag("method", "GET")
                .tag("status", "200")
                .counter();
        Counter get404Counter = meterRegistry.find("eraf.test.api.calls")
                .tag("method", "GET")
                .tag("status", "404")
                .counter();
        Counter postCounter = meterRegistry.find("eraf.test.api.calls")
                .tag("method", "POST")
                .tag("status", "201")
                .counter();

        assertThat(get200Counter).isNotNull();
        assertThat(get200Counter.count()).isEqualTo(1);
        assertThat(get404Counter).isNotNull();
        assertThat(get404Counter.count()).isEqualTo(1);
        assertThat(postCounter).isNotNull();
        assertThat(postCounter.count()).isEqualTo(1);
    }

    @Test
    void tracing_ParentChildRelationship() {
        // given - 부모 스팬
        TraceContext parent = TraceContext.create();

        // when - 자식 스팬들 생성
        TraceContext child1 = TraceContext.createChild(parent);
        TraceContext child2 = TraceContext.createChild(parent);

        // then
        assertThat(child1.getTraceId()).isEqualTo(parent.getTraceId());
        assertThat(child2.getTraceId()).isEqualTo(parent.getTraceId());
        assertThat(child1.getParentSpanId()).isEqualTo(parent.getSpanId());
        assertThat(child2.getParentSpanId()).isEqualTo(parent.getSpanId());

        // 자식들은 서로 다른 spanId를 가짐
        assertThat(child1.getSpanId()).isNotEqualTo(child2.getSpanId());
    }
}
