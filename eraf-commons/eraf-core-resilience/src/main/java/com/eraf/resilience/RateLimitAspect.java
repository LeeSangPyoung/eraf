package com.eraf.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * Rate Limit AOP Aspect
 */
@Aspect
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimiter.Registry registry;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimitAspect(RateLimiter.Registry registry) {
        this.registry = registry;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String name = buildKey(joinPoint, rateLimit);

        RateLimiter limiter = registry.getOrCreate(
                name,
                rateLimit.permitsPerSecond(),
                rateLimit.maxBurstSize()
        );

        if (rateLimit.blocking()) {
            try {
                limiter.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate limiter interrupted", e);
            }
        } else {
            if (!limiter.tryAcquire()) {
                log.warn("Rate limit exceeded for [{}]", name);
                return handleLimitExceeded(joinPoint, rateLimit, limiter);
            }
        }

        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String baseKey = rateLimit.name();
        String keyExpression = rateLimit.key();

        if (keyExpression.isEmpty()) {
            return baseKey;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new MethodBasedEvaluationContext(
                    joinPoint.getTarget(), method, args, parameterNameDiscoverer
            );

            Object keyValue = parser.parseExpression(keyExpression).getValue(context);
            return baseKey + ":" + keyValue;
        } catch (Exception e) {
            log.warn("Failed to evaluate key expression '{}', using base key", keyExpression);
            return baseKey;
        }
    }

    private Object handleLimitExceeded(ProceedingJoinPoint joinPoint, RateLimit rateLimit, RateLimiter limiter) throws Throwable {
        String fallbackMethod = rateLimit.fallbackMethod();

        if (!fallbackMethod.isEmpty()) {
            return invokeFallback(joinPoint, fallbackMethod);
        }

        throw new RateLimitExceededException(rateLimit.name(), limiter.getPermitsPerSecond());
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
            throw new RateLimitExceededException(
                    ((RateLimit) signature.getMethod().getAnnotation(RateLimit.class)).name(),
                    0
            );
        }
    }
}
