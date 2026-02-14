package com.eraf.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * TracingUtil 테스트
 */
class TracingUtilTest {

    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

    private TracingUtil tracingUtil;
    private Tracer tracer;

    @BeforeEach
    void setUp() {
        tracer = otelTesting.getOpenTelemetry().getTracer("test-tracer");
        tracingUtil = new TracingUtil(tracer);
    }

    @Test
    void withSpan_ShouldExecuteAndReturnValue() {
        // when
        String result = tracingUtil.withSpan("test-operation", () -> "test-result");

        // then
        assertThat(result).isEqualTo("test-result");
        assertThat(otelTesting.getSpans()).hasSize(1);
        assertThat(otelTesting.getSpans().get(0).getName()).isEqualTo("test-operation");
    }

    @Test
    void withSpan_Runnable_ShouldExecute() {
        // given
        final boolean[] executed = {false};

        // when
        tracingUtil.withSpan("test-runnable", () -> executed[0] = true);

        // then
        assertThat(executed[0]).isTrue();
        assertThat(otelTesting.getSpans()).hasSize(1);
    }

    @Test
    void withSpan_ShouldRecordExceptionOnError() {
        // when/then
        assertThatThrownBy(() ->
            tracingUtil.withSpan("failing-operation", () -> {
                throw new RuntimeException("Test error");
            })
        ).isInstanceOf(RuntimeException.class)
         .hasMessage("Test error");

        // Span should still be recorded with error
        assertThat(otelTesting.getSpans()).hasSize(1);
        var span = otelTesting.getSpans().get(0);
        assertThat(span.getName()).isEqualTo("failing-operation");
        assertThat(span.getEvents()).isNotEmpty(); // Exception event recorded
    }

    @Test
    void withSpanAndAttributes_ShouldAddAttributes() {
        // when
        tracingUtil.withSpan("attributed-operation", () -> {
            tracingUtil.addAttribute("user.id", "123");
            tracingUtil.addAttribute("action", "read");
            return "result";
        });

        // then
        assertThat(otelTesting.getSpans()).hasSize(1);
        var span = otelTesting.getSpans().get(0);
        assertThat(span.getAttributes().asMap()).containsKeys(
            io.opentelemetry.api.common.AttributeKey.stringKey("user.id"),
            io.opentelemetry.api.common.AttributeKey.stringKey("action")
        );
    }

    @Test
    void startSpan_ShouldCreateSpan() {
        // when
        Span span = tracingUtil.startSpan("manual-span");
        span.end();

        // then
        assertThat(otelTesting.getSpans()).hasSize(1);
        assertThat(otelTesting.getSpans().get(0).getName()).isEqualTo("manual-span");
    }

    @Test
    void addEvent_ShouldRecordEvent() {
        // when
        tracingUtil.withSpan("event-test", () -> {
            tracingUtil.addEvent("processing");
            tracingUtil.addEvent("completed");
            return null;
        });

        // then
        var span = otelTesting.getSpans().get(0);
        assertThat(span.getEvents()).hasSize(2);
    }

    @Test
    void addAttribute_ShouldAddAttributes() {
        // when
        tracingUtil.withSpan("attr-test", () -> {
            tracingUtil.addAttribute("http.method", "GET");
            tracingUtil.addAttribute("http.status_code", "200");
            return null;
        });

        // then
        var span = otelTesting.getSpans().get(0);
        assertThat(span.getAttributes().asMap()).isNotEmpty();
    }
}
