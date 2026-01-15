package com.eraf.core.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 낙관적 락 재시도 AOP Aspect
 * @OptimisticRetry 어노테이션을 처리하여 OptimisticLockingFailureException 발생 시 재시도
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class OptimisticRetryAspect {

    private static final Logger log = LoggerFactory.getLogger(OptimisticRetryAspect.class);

    @Around("@annotation(optimisticRetry)")
    public Object around(ProceedingJoinPoint joinPoint, OptimisticRetry optimisticRetry) throws Throwable {
        int maxRetries = optimisticRetry.maxRetries();
        long backoffMs = optimisticRetry.backoffMs();
        boolean exponentialBackoff = optimisticRetry.exponentialBackoff();
        Class<? extends Exception>[] retryOnExceptions = optimisticRetry.retryOn();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        Exception lastException = null;
        int attempt = 0;

        while (attempt <= maxRetries) {
            try {
                Object result = joinPoint.proceed();

                if (attempt > 0) {
                    log.info("낙관적 락 재시도 성공: method={}, attempt={}", methodName, attempt);
                }

                return result;
            } catch (Exception e) {
                if (!isRetryableException(e, retryOnExceptions)) {
                    throw e;
                }

                lastException = e;
                attempt++;

                if (attempt > maxRetries) {
                    log.warn("낙관적 락 재시도 횟수 초과: method={}, maxRetries={}", methodName, maxRetries);
                    break;
                }

                long sleepTime = calculateBackoff(backoffMs, attempt, exponentialBackoff);
                log.debug("낙관적 락 충돌, 재시도: method={}, attempt={}/{}, backoff={}ms",
                        methodName, attempt, maxRetries, sleepTime);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }

        throw lastException;
    }

    private boolean isRetryableException(Exception e, Class<? extends Exception>[] retryOnExceptions) {
        return Arrays.stream(retryOnExceptions)
                .anyMatch(exClass -> exClass.isInstance(e) || isCausedBy(e, exClass));
    }

    private boolean isCausedBy(Throwable throwable, Class<? extends Exception> exceptionClass) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (exceptionClass.isInstance(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private long calculateBackoff(long baseBackoffMs, int attempt, boolean exponentialBackoff) {
        if (exponentialBackoff) {
            // 지수 백오프: 100ms, 200ms, 400ms, 800ms...
            return baseBackoffMs * (1L << (attempt - 1));
        }
        return baseBackoffMs;
    }
}
