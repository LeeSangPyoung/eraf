package com.eraf.actuator.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * HTTP 요청 트레이싱 필터
 */
public class TracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TracingFilter.class);

    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_SPAN_ID = "spanId";

    private final ErafTracingProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Random random = new Random();

    public TracingFilter(ErafTracingProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip excluded patterns
        if (shouldSkip(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Sampling check
        if (!shouldSample()) {
            filterChain.doFilter(request, response);
            return;
        }

        TraceContext context = extractOrCreateContext(request);
        TraceContextHolder.setContext(context);

        try {
            // Add to MDC
            if (properties.isMdcEnabled()) {
                MDC.put(MDC_TRACE_ID, context.getTraceId());
                MDC.put(MDC_SPAN_ID, context.getSpanId());
            }

            // Add to response header
            if (properties.isIncludeInResponse()) {
                response.setHeader(properties.getTraceIdHeader(), context.getTraceId());
            }

            log.debug("Trace started: traceId={}, spanId={}, uri={}",
                    context.getTraceId(), context.getSpanId(), request.getRequestURI());

            filterChain.doFilter(request, response);

            log.debug("Trace completed: traceId={}, spanId={}, duration={}ms, status={}",
                    context.getTraceId(), context.getSpanId(),
                    context.getDurationMs(), response.getStatus());

        } finally {
            TraceContextHolder.clear();
            if (properties.isMdcEnabled()) {
                MDC.remove(MDC_TRACE_ID);
                MDC.remove(MDC_SPAN_ID);
            }
        }
    }

    private TraceContext extractOrCreateContext(HttpServletRequest request) {
        String traceId = request.getHeader(properties.getTraceIdHeader());
        String spanId = request.getHeader(properties.getSpanIdHeader());
        String parentSpanId = request.getHeader(properties.getParentSpanIdHeader());

        if (StringUtils.hasText(traceId)) {
            // Propagate existing trace
            return TraceContext.builder()
                    .traceId(traceId)
                    .spanId(StringUtils.hasText(spanId) ? spanId : TraceContext.generateId())
                    .parentSpanId(parentSpanId)
                    .startTime(System.currentTimeMillis())
                    .build();
        } else {
            // Create new trace
            return TraceContext.create();
        }
    }

    private boolean shouldSkip(String requestUri) {
        List<String> excludePatterns = properties.getExcludePatterns();
        return excludePatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    private boolean shouldSample() {
        float rate = properties.getSamplingRate();
        if (rate >= 1.0f) {
            return true;
        }
        if (rate <= 0.0f) {
            return false;
        }
        return random.nextFloat() < rate;
    }
}
