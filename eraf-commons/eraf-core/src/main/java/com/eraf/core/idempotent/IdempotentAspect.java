package com.eraf.core.idempotent;

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
import java.time.Duration;
import java.util.Optional;

/**
 * 멱등성 보장 AOP Aspect
 * @Idempotent 어노테이션을 처리하여 중복 요청 방지
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private final IdempotencyStore idempotencyStore;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public IdempotentAspect(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String idempotencyKey = generateKey(joinPoint, idempotent);
        Duration timeout = Duration.of(idempotent.timeout(), idempotent.timeUnit().toChronoUnit());

        // 이미 처리된 요청인지 확인
        if (idempotencyStore.exists(idempotencyKey)) {
            log.debug("중복 요청 감지: key={}", idempotencyKey);

            if (idempotent.throwOnDuplicate()) {
                throw new IdempotentException(idempotent.message(), idempotencyKey);
            }

            // 이전 결과 반환 시도
            Optional<Object> cachedResult = idempotencyStore.getResult(idempotencyKey);
            if (cachedResult.isPresent()) {
                log.debug("캐시된 결과 반환: key={}", idempotencyKey);
                return cachedResult.get();
            }

            // 캐시된 결과가 없으면 null 반환 (처리 중일 수 있음)
            return null;
        }

        // 새로운 요청 - 처리 시작 마킹
        boolean acquired = idempotencyStore.setIfAbsent(idempotencyKey, timeout);
        if (!acquired) {
            // 동시 요청으로 인한 경합 상황
            log.debug("동시 요청 경합: key={}", idempotencyKey);
            if (idempotent.throwOnDuplicate()) {
                throw new IdempotentException(idempotent.message(), idempotencyKey);
            }
            return null;
        }

        try {
            // 실제 메서드 실행
            Object result = joinPoint.proceed();

            // 결과 저장
            idempotencyStore.saveResult(idempotencyKey, result, timeout);
            log.debug("멱등성 처리 완료: key={}", idempotencyKey);

            return result;
        } catch (Exception e) {
            // 실패 시 키 삭제 (재시도 허용)
            idempotencyStore.delete(idempotencyKey);
            log.debug("멱등성 처리 실패, 키 삭제: key={}", idempotencyKey);
            throw e;
        }
    }

    private String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String keyExpression = idempotent.key();

        // 커스텀 키가 없으면 메서드 기반으로 생성
        if (keyExpression == null || keyExpression.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return IdempotencyKeyGenerator.generate(signature.getMethod(), joinPoint.getArgs());
        }

        // SpEL 표현식 평가
        if (keyExpression.contains("#")) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = method.getParameters();

            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < parameters.length; i++) {
                context.setVariable(parameters[i].getName(), args[i]);
                context.setVariable("p" + i, args[i]);
                context.setVariable("a" + i, args[i]);
            }

            try {
                String evaluatedKey = expressionParser.parseExpression(keyExpression)
                        .getValue(context, String.class);
                return IdempotencyKeyGenerator.generate(evaluatedKey);
            } catch (Exception e) {
                log.warn("SpEL 표현식 평가 실패: {}", keyExpression, e);
            }
        }

        return IdempotencyKeyGenerator.generate(keyExpression);
    }
}
