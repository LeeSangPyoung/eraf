package com.eraf.actuator.tracing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;

/**
 * @Traced 어노테이션 처리 Aspect
 */
@Aspect
public class TracedAspect {

    private static final Logger log = LoggerFactory.getLogger(TracedAspect.class);

    @Around("@annotation(traced)")
    public Object around(ProceedingJoinPoint joinPoint, Traced traced) throws Throwable {
        String spanName = resolveSpanName(joinPoint, traced);

        // Create child span
        TraceContext parentContext = TraceContextHolder.getContext();
        TraceContext childContext = parentContext != null
                ? TraceContext.createChild(parentContext)
                : TraceContext.create();

        TraceContextHolder.setContext(childContext);

        String previousSpanId = MDC.get("spanId");
        MDC.put("spanId", childContext.getSpanId());

        try {
            if (traced.logParameters()) {
                log.debug("Span started: name={}, traceId={}, spanId={}, params={}",
                        spanName, childContext.getTraceId(), childContext.getSpanId(),
                        Arrays.toString(joinPoint.getArgs()));
            } else {
                log.debug("Span started: name={}, traceId={}, spanId={}",
                        spanName, childContext.getTraceId(), childContext.getSpanId());
            }

            Object result = joinPoint.proceed();

            if (traced.logResult()) {
                log.debug("Span completed: name={}, duration={}ms, result={}",
                        spanName, childContext.getDurationMs(), result);
            } else {
                log.debug("Span completed: name={}, duration={}ms",
                        spanName, childContext.getDurationMs());
            }

            return result;
        } catch (Throwable t) {
            log.debug("Span failed: name={}, duration={}ms, error={}",
                    spanName, childContext.getDurationMs(), t.getMessage());
            throw t;
        } finally {
            // Restore parent context
            TraceContextHolder.setContext(parentContext);
            if (previousSpanId != null) {
                MDC.put("spanId", previousSpanId);
            } else {
                MDC.remove("spanId");
            }
        }
    }

    private String resolveSpanName(ProceedingJoinPoint joinPoint, Traced traced) {
        if (!traced.value().isEmpty()) {
            return traced.value();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
    }
}
