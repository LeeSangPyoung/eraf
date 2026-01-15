package com.eraf.core.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 분산 락 AOP Aspect
 * @DistributedLock 어노테이션을 처리
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);
    private static final String LOCK_PREFIX = "eraf:lock:";

    private final LockProvider lockProvider;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public DistributedLockAspect(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = generateLockKey(joinPoint, distributedLock);

        boolean acquired = false;
        try {
            acquired = lockProvider.tryLock(
                    lockKey,
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                log.warn("락 획득 실패: key={}, waitTime={}{}",
                        lockKey, distributedLock.waitTime(), distributedLock.timeUnit());

                if (distributedLock.failOnTimeout()) {
                    throw LockException.timeout(lockKey, distributedLock.message());
                }
                return null;
            }

            log.debug("락 획득 성공: key={}", lockKey);
            return joinPoint.proceed();

        } finally {
            if (acquired) {
                try {
                    lockProvider.unlock(lockKey);
                    log.debug("락 해제 성공: key={}", lockKey);
                } catch (Exception e) {
                    log.error("락 해제 실패: key={}", lockKey, e);
                }
            }
        }
    }

    private String generateLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        String keyExpression = distributedLock.key();

        // SpEL 표현식이 아닌 경우 그대로 사용
        if (!keyExpression.contains("#")) {
            return LOCK_PREFIX + keyExpression;
        }

        // SpEL 표현식 평가
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
            // 인덱스 기반 접근도 지원
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
        }

        try {
            String evaluatedKey = expressionParser.parseExpression(keyExpression)
                    .getValue(context, String.class);
            return LOCK_PREFIX + evaluatedKey;
        } catch (Exception e) {
            log.warn("SpEL 표현식 평가 실패, 원본 키 사용: {}", keyExpression, e);
            return LOCK_PREFIX + keyExpression;
        }
    }
}
