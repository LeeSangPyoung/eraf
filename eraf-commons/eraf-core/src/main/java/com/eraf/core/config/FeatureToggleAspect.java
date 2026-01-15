package com.eraf.core.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 기능 토글 AOP Aspect
 * @Feature 어노테이션이 붙은 메서드의 실행을 기능 활성화 여부에 따라 제어
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class FeatureToggleAspect {

    private static final Logger log = LoggerFactory.getLogger(FeatureToggleAspect.class);
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private final FeatureToggle featureToggle;
    private final BeanFactory beanFactory;

    public FeatureToggleAspect(FeatureToggle featureToggle) {
        this(featureToggle, null);
    }

    public FeatureToggleAspect(FeatureToggle featureToggle, BeanFactory beanFactory) {
        this.featureToggle = featureToggle;
        this.beanFactory = beanFactory;
    }

    /**
     * @Feature 어노테이션이 붙은 메서드 인터셉트
     */
    @Around("@annotation(feature)")
    public Object aroundMethod(ProceedingJoinPoint joinPoint, Feature feature) throws Throwable {
        return handleFeatureCheck(joinPoint, feature);
    }

    /**
     * @Feature 어노테이션이 붙은 클래스의 모든 public 메서드 인터셉트
     */
    @Around("@within(feature) && execution(public * *(..))")
    public Object aroundClass(ProceedingJoinPoint joinPoint, Feature feature) throws Throwable {
        // 메서드에 직접 @Feature가 있으면 메서드 레벨 어노테이션 우선
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(Feature.class)) {
            return joinPoint.proceed(); // 메서드 레벨 aspect에서 처리
        }
        return handleFeatureCheck(joinPoint, feature);
    }

    private Object handleFeatureCheck(ProceedingJoinPoint joinPoint, Feature feature) throws Throwable {
        String featureName = feature.value();

        if (featureToggle.isEnabled(featureName)) {
            log.debug("Feature '{}' is enabled, proceeding with method execution", featureName);
            return joinPoint.proceed();
        }

        log.debug("Feature '{}' is disabled", featureName);

        // 폴백 처리
        String fallback = feature.fallback();
        if (fallback != null && !fallback.isEmpty()) {
            return evaluateFallback(joinPoint, fallback);
        }

        // 폴백이 없으면 예외 발생
        throw new FeatureDisabledException(featureName);
    }

    /**
     * SpEL 표현식으로 폴백 값 계산
     */
    private Object evaluateFallback(ProceedingJoinPoint joinPoint, String fallback) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // BeanFactory 설정 (빈 참조 가능)
            if (beanFactory != null) {
                context.setBeanResolver(new BeanFactoryResolver(beanFactory));
            }

            // 메서드 파라미터를 변수로 등록
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            // 타겟 객체 등록
            context.setVariable("target", joinPoint.getTarget());
            context.setVariable("method", signature.getMethod().getName());

            return PARSER.parseExpression(fallback).getValue(context);
        } catch (Exception e) {
            log.warn("Failed to evaluate fallback expression '{}': {}", fallback, e.getMessage());
            return null;
        }
    }
}
