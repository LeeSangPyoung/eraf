package com.eraf.grpc.config;

import com.eraf.grpc.GrpcClientFactory;
import com.eraf.grpc.interceptor.LoggingInterceptor;
import com.eraf.grpc.interceptor.MetadataPropagationInterceptor;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF gRPC 자동 구성
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.grpc.Server")
@EnableConfigurationProperties(ErafGrpcProperties.class)
public class ErafGrpcAutoConfiguration {

    /**
     * 전역 로깅 인터셉터
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    /**
     * 메타데이터 전파 인터셉터
     */
    @Bean
    @GrpcGlobalServerInterceptor
    public ServerInterceptor metadataPropagationInterceptor() {
        return new MetadataPropagationInterceptor();
    }

    /**
     * gRPC 클라이언트 팩토리
     */
    @Bean
    @ConditionalOnMissingBean
    public GrpcClientFactory grpcClientFactory(ErafGrpcProperties properties) {
        return new GrpcClientFactory(properties);
    }
}
