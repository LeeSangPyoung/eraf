package com.eraf.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Bulkhead AOP Aspect
 */
@Aspect
public class BulkheadAspect {

    private static final Logger log = LoggerFactory.getLogger(BulkheadAspect.class);

    private final ConcurrentMap<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    @Around("@annotation(bulkhead)")
    public Object around(ProceedingJoinPoint joinPoint, Bulkhead bulkhead) throws Throwable {
        String name = bulkhead.name();
        int maxConcurrent = bulkhead.maxConcurrent();

        Semaphore semaphore = semaphores.computeIfAbsent(name, k -> new Semaphore(maxConcurrent));

        boolean acquired = false;
        try {
            if (bulkhead.maxWait() > 0 && bulkhead.waitTimeoutMs() > 0) {
                // 대기 큐가 있고 타임아웃이 설정된 경우
                acquired = semaphore.tryAcquire(bulkhead.waitTimeoutMs(), TimeUnit.MILLISECONDS);
            } else {
                // 즉시 시도
                acquired = semaphore.tryAcquire();
            }

            if (!acquired) {
                log.warn("Bulkhead [{}] is full (max concurrent: {})", name, maxConcurrent);
                return handleBulkheadFull(joinPoint, bulkhead);
            }

            return joinPoint.proceed();
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    private Object handleBulkheadFull(ProceedingJoinPoint joinPoint, Bulkhead bulkhead) throws Throwable {
        String fallbackMethod = bulkhead.fallbackMethod();

        if (!fallbackMethod.isEmpty()) {
            return invokeFallback(joinPoint, fallbackMethod);
        }

        throw new BulkheadFullException(bulkhead.name(), bulkhead.maxConcurrent());
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethod) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        try {
            Method fallback = targetClass.getDeclaredMethod(fallbackMethod, signature.getMethod().getParameterTypes());
            fallback.setAccessible(true);
            return fallback.invoke(joinPoint.getTarget(), joinPoint.getArgs());
        } catch (NoSuchMethodException e) {
            log.error("Fallback method '{}' not found in class {}", fallbackMethod, targetClass.getName());
            throw new BulkheadFullException(
                    ((Bulkhead) signature.getMethod().getAnnotation(Bulkhead.class)).name(),
                    ((Bulkhead) signature.getMethod().getAnnotation(Bulkhead.class)).maxConcurrent()
            );
        }
    }

    /**
     * 특정 Bulkhead의 사용 가능한 허가 수 조회
     */
    public int getAvailablePermits(String name) {
        Semaphore semaphore = semaphores.get(name);
        return semaphore != null ? semaphore.availablePermits() : -1;
    }

    /**
     * 특정 Bulkhead의 대기 중인 스레드 수 조회
     */
    public int getQueueLength(String name) {
        Semaphore semaphore = semaphores.get(name);
        return semaphore != null ? semaphore.getQueueLength() : -1;
    }
}
