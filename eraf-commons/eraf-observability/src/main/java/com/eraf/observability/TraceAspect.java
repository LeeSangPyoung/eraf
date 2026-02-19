package com.eraf.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * {@link Trace} 어노테이션을 위한 AOP Aspect
 *
 * <p>메서드 실행 전 Span을 시작하고, 완료 후 종료합니다.
 * 예외 발생 시 Span에 에러 상태와 예외를 기록합니다.
 * {@link Trace#attributes()}에 지정된 파라미터는 Span attribute로 추가됩니다.
 */
@Aspect
public class TraceAspect {

    private final Tracer tracer;

    public TraceAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(trace)")
    public Object aroundTrace(ProceedingJoinPoint pjp, Trace trace) throws Throwable {
        String spanName = resolveSpanName(trace, pjp);
        SpanKind kind = trace.kind();

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();

        addParameterAttributes(span, trace, pjp);

        try (Scope scope = span.makeCurrent()) {
            Object result = pjp.proceed();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Throwable t) {
            span.recordException(t);
            span.setStatus(StatusCode.ERROR, t.getMessage());
            throw t;
        } finally {
            span.end();
        }
    }

    @Around("@within(trace) && !@annotation(com.eraf.observability.Trace)")
    public Object aroundTypeTrace(ProceedingJoinPoint pjp, Trace trace) throws Throwable {
        String spanName = resolveSpanName(trace, pjp);
        SpanKind kind = trace.kind();

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Object result = pjp.proceed();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Throwable t) {
            span.recordException(t);
            span.setStatus(StatusCode.ERROR, t.getMessage());
            throw t;
        } finally {
            span.end();
        }
    }

    private String resolveSpanName(Trace trace, ProceedingJoinPoint pjp) {
        if (!trace.value().isEmpty()) {
            return trace.value();
        }
        // 기본값: "SimpleClassName.methodName"
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        return className + "." + methodName;
    }

    private void addParameterAttributes(Span span, Trace trace, ProceedingJoinPoint pjp) {
        if (trace.attributes().length == 0) {
            return;
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = pjp.getArgs();

        for (String attrName : trace.attributes()) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(attrName) && i < args.length && args[i] != null) {
                    span.setAttribute(attrName, String.valueOf(args[i]));
                    break;
                }
            }
        }
    }
}
