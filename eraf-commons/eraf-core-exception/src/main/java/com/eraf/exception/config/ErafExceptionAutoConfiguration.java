package com.eraf.exception.config;

import com.eraf.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Exception Auto-Configuration
 *
 * Spring Boot 3.x 모범 사례에 따라 GlobalExceptionHandler를 자동 구성합니다.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(GlobalExceptionHandler.class)
public class ErafExceptionAutoConfiguration {

    /**
     * GlobalExceptionHandler를 빈으로 등록
     * 애플리케이션에서 커스텀 ExceptionHandler가 정의되지 않은 경우에만 등록
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
