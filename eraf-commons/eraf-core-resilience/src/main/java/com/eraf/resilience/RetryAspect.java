package com.eraf.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retry AOP Aspect
 */
@Aspect
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    @Around("@annotation(retry)")
    public Object around(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int maxAttempts = retry.maxAttempts();
        long delay = retry.delay();
        double multiplier = retry.multiplier();
        long maxDelay = retry.maxDelay();

        Set<Class<? extends Throwable>> retryOn = Arrays.stream(retry.retryOn()).collect(Collectors.toSet());
        Set<Class<? extends Throwable>> ignoreOn = Arrays.stream(retry.ignoreOn()).collect(Collectors.toSet());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();

        Throwable lastException = null;
        long currentDelay = delay;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                lastException = t;

                // ignoreOn에 있으면 즉시 throw
                if (shouldIgnore(t, ignoreOn)) {
                    throw t;
                }

                // retryOn이 비어있지 않고, 해당 예외가 없으면 throw
                if (!retryOn.isEmpty() && !shouldRetry(t, retryOn)) {
                    throw t;
                }

                if (attempt < maxAttempts) {
                    log.warn("Retry attempt {}/{} for {} failed: {}. Retrying in {}ms",
                            attempt, maxAttempts, methodName, t.getMessage(), currentDelay);

                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw t;
                    }

                    // 다음 지연 계산 (지수 백오프)
                    currentDelay = (long) (currentDelay * multiplier);
                    if (maxDelay > 0 && currentDelay > maxDelay) {
                        currentDelay = maxDelay;
                    }
                }
            }
        }

        log.error("Retry exhausted after {} attempts for {}", maxAttempts, methodName);

        // 폴백 처리
        String fallbackMethod = retry.fallbackMethod();
        if (!fallbackMethod.isEmpty()) {
            return invokeFallback(joinPoint, fallbackMethod, lastException);
        }

        throw new RetryExhaustedException(maxAttempts, maxAttempts, lastException);
    }

    private boolean shouldRetry(Throwable t, Set<Class<? extends Throwable>> retryOn) {
        for (Class<? extends Throwable> clazz : retryOn) {
            if (clazz.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldIgnore(Throwable t, Set<Class<? extends Throwable>> ignoreOn) {
        for (Class<? extends Throwable> clazz : ignoreOn) {
            if (clazz.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String fallbackMethod, Throwable cause) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method originalMethod = signature.getMethod();

        try {
            // Throwable 파라미터 포함 버전 시도
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
