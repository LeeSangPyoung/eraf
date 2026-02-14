package com.eraf.jpa.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * DataSource 라우팅 AOP Aspect
 *
 * {@literal @}DataSourceRouting 어노테이션 기반으로 DataSource를 전환합니다.
 */
@Aspect
public class DataSourceRoutingAspect {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRoutingAspect.class);

    @Around("@annotation(com.eraf.jpa.datasource.DataSourceRouting) || @within(com.eraf.jpa.datasource.DataSourceRouting)")
    public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceRouting routing = getAnnotation(joinPoint);
        if (routing == null) {
            return joinPoint.proceed();
        }

        String previous = DataSourceContextHolder.getCurrentDataSource();
        try {
            DataSourceContextHolder.setDataSource(routing.value());
            return joinPoint.proceed();
        } finally {
            if (previous != null) {
                DataSourceContextHolder.setDataSource(previous);
            } else {
                DataSourceContextHolder.clear();
            }
        }
    }

    private DataSourceRouting getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        DataSourceRouting annotation = method.getAnnotation(DataSourceRouting.class);
        if (annotation != null) {
            return annotation;
        }
        return joinPoint.getTarget().getClass().getAnnotation(DataSourceRouting.class);
    }
}
