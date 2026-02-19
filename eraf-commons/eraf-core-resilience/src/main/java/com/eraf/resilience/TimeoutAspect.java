package com.eraf.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * Timeout AOP Aspect
 */
@Aspect
public class TimeoutAspect implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(TimeoutAspect.class);

    private final ExecutorService executor;

    public TimeoutAspect() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "timeout-executor");
            t.setDaemon(true);
            return t;
        });
    }

    public TimeoutAspect(ExecutorService executor) {
        this.executor = executor;
    }

    @Around("@annotation(timeout)")
    public Object around(ProceedingJoinPoint joinPoint, Timeout timeout) throws Throwable {
        long timeoutMs = timeout.unit().toMillis(timeout.value());

        Future<Object> future = executor.submit(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                if (t instanceof Exception) {
                    throw (Exception) t;
                }
                throw new RuntimeException(t);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            log.warn("Operation timed out after {}ms: {}.{}",
                    timeoutMs,
                    joinPoint.getTarget().getClass().getSimpleName(),
                    ((MethodSignature) joinPoint.getSignature()).getMethod().getName());

            String fallbackMethod = timeout.fallbackMethod();
            if (!fallbackMethod.isEmpty()) {
                return invokeFallback(joinPoint, fallbackMethod, new TimeoutException(timeoutMs));
            }
            throw new TimeoutException(timeoutMs, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeoutException(timeoutMs, e);
        }
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

    @Override
    public void destroy() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
