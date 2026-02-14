package com.eraf.http;

import feign.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @ErafClient 어노테이션 속성을 Feign 클라이언트에 적용하는 BeanPostProcessor
 *
 * <p>각 Feign 클라이언트 프록시가 생성된 후, 해당 인터페이스의 @ErafClient 어노테이션에서
 * timeout, retry 등의 속성을 읽어 Feign 설정에 반영합니다.</p>
 *
 * <p>전역 기본값은 ErafHttpProperties에서 설정하고,
 * @ErafClient 어노테이션이 있으면 해당 클라이언트에 대해 오버라이드합니다.</p>
 */
public class ErafClientBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ErafClientBeanPostProcessor.class);

    private final ErafHttpProperties properties;

    public ErafClientBeanPostProcessor(ErafHttpProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = resolveTargetClass(bean);
        if (targetClass == null) {
            return bean;
        }

        ErafClient erafClient = targetClass.getAnnotation(ErafClient.class);
        if (erafClient == null) {
            return bean;
        }

        logClientConfiguration(beanName, erafClient);
        return bean;
    }

    /**
     * Feign 프록시 빈에서 원본 인터페이스를 찾습니다.
     */
    private Class<?> resolveTargetClass(Object bean) {
        Class<?>[] interfaces = bean.getClass().getInterfaces();
        for (Class<?> iface : interfaces) {
            if (iface.isAnnotationPresent(ErafClient.class)) {
                return iface;
            }
        }
        return null;
    }

    /**
     * @ErafClient 속성과 ErafHttpProperties의 매핑을 로그로 출력합니다.
     *
     * Feign 클라이언트의 timeout, retry 등은 Spring Cloud OpenFeign의
     * application.yml 설정(spring.cloud.openfeign.client.config)으로 관리됩니다.
     * @ErafClient 어노테이션의 속성값은 해당 설정의 기본값 가이드 역할을 합니다.
     */
    private void logClientConfiguration(String beanName, ErafClient erafClient) {
        long effectiveTimeout = erafClient.timeout() != 30000
                ? erafClient.timeout()
                : properties.getTimeout().toMillis();
        int effectiveRetry = erafClient.retry() != 3
                ? erafClient.retry()
                : properties.getRetryCount();
        boolean effectiveCircuitBreaker = erafClient.circuitBreaker()
                && properties.isCircuitBreakerEnabled();

        log.debug("ERAF Client [{}] configured: url={}, path={}, timeout={}ms, retry={}, circuitBreaker={}",
                beanName,
                erafClient.url().isEmpty() ? "(service-discovery)" : erafClient.url(),
                erafClient.path().isEmpty() ? "/" : erafClient.path(),
                effectiveTimeout,
                effectiveRetry,
                effectiveCircuitBreaker);
    }

    /**
     * @ErafClient 속성에서 Feign Request.Options를 생성합니다.
     * 사용자가 커스텀 Feign 설정을 정의할 때 활용할 수 있습니다.
     *
     * @param erafClient 어노테이션 인스턴스
     * @return Feign Request.Options
     */
    public Request.Options createRequestOptions(ErafClient erafClient) {
        long timeout = erafClient.timeout() != 30000
                ? erafClient.timeout()
                : properties.getTimeout().toMillis();

        return new Request.Options(
                timeout, TimeUnit.MILLISECONDS,
                timeout, TimeUnit.MILLISECONDS,
                true
        );
    }
}
