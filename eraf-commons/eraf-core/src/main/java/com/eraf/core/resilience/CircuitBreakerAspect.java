package com.eraf.core.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Circuit Breaker AOP Aspect
 */
@Aspect
public class CircuitBreakerAspect {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerAspect.class);

    private final CircuitBreakerRegistry registry;

    public CircuitBreakerAspect(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(enableCircuitBreaker)")
    public Object around(ProceedingJoinPoint joinPoint, EnableCircuitBreaker enableCircuitBreaker) throws Throwable {
        String name = enableCircuitBreaker.name();

        CircuitBreaker cb = registry.getOrCreate(
                name,
                enableCircuitBreaker.failureThreshold(),
                enableCircuitBreaker.successThreshold(),
                enableCircuitBreaker.openTimeoutMs()
        );

        if (!cb.allowRequest()) {
            log.warn("Circuit breaker [{}] is OPEN, request rejected", name);
            return handleOpenState(joinPoint, enableCircuitBreaker, cb);
        }

        try {
            Object result = joinPoint.proceed();
            cb.recordSuccess();
            return result;
        } catch (Throwable t) {
            cb.recordFailure();
            log.warn("Circuit breaker [{}] recorded failure: {}", name, t.getMessage());

            // 폴백 메서드가 있으면 호출
            String fallbackMethod = enableCircuitBreaker.fallbackMethod();
            if (!fallbackMethod.isEmpty()) {
                return invokeFallback(joinPoint, fallbackMethod, t);
            }
            throw t;
        }
    }

    private Object handleOpenState(ProceedingJoinPoint joinPoint, EnableCircuitBreaker annotation, CircuitBreaker cb) throws Throwable {
        String fallbackMethod = annotation.fallbackMethod();

        if (!fallbackMethod.isEmpty()) {
            return invokeFallback(joinPoint, fallbackMethod, new CircuitBreakerOpenException(annotation.name()));
        }

        throw new CircuitBreakerOpenException(annotation.name(), cb.getState());
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethod, Throwable cause) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method originalMethod = signature.getMethod();

        try {
            // 같은 파라미터 + Throwable 파라미터를 받는 폴백 먼저 시도
            try {
                Class<?>[] paramTypesWithThrowable = new Class<?>[originalMethod.getParameterTypes().length + 1];
                System.arraycopy(originalMethod.getParameterTypes(), 0, paramTypesWithThrowable, 0, originalMethod.getParameterTypes().length);
                paramTypesWithThrowable[paramTypesWithThrowable.length - 1] = Throwable.class;

                Method fallback = targetClass.getDeclaredMethod(fallbackMethod, paramTypesWithThrowable);
                fallback.setAccessible(true);

                Object[] argsWithThrowable = new Object[joinPoint.getArgs().length + 1];
                System.arraycopy(joinPoint.getArgs(), 0, argsWithThrowable, 0, joinPoint.getArgs().length);
                argsWithThrowable[argsWithThrowable.length - 1] = cause;

                return fallback.invoke(joinPoint.getTarget(), argsWithThrowable);
            } catch (NoSuchMethodException e) {
                // 같은 파라미터만 받는 폴백 시도
                Method fallback = targetClass.getDeclaredMethod(fallbackMethod, originalMethod.getParameterTypes());
                fallback.setAccessible(true);
                return fallback.invoke(joinPoint.getTarget(), joinPoint.getArgs());
            }
        } catch (NoSuchMethodException e) {
            log.error("Fallback method '{}' not found in class {}", fallbackMethod, targetClass.getName());
            throw cause;
        } catch (Exception e) {
            log.error("Failed to invoke fallback method '{}': {}", fallbackMethod, e.getMessage());
            throw cause;
        }
    }
}
