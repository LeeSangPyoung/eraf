package com.eraf.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Virtual Thread Executor Auto Configuration
 * Java 21+ 가상 스레드 지원
 */
@AutoConfiguration
@EnableAsync
@ConditionalOnJava(JavaVersion.TWENTY_ONE)
@ConditionalOnProperty(name = "eraf.async.virtual-threads.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(VirtualThreadProperties.class)
public class VirtualThreadExecutorAutoConfiguration implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadExecutorAutoConfiguration.class);

    private final VirtualThreadProperties properties;

    public VirtualThreadExecutorAutoConfiguration(VirtualThreadProperties properties) {
        this.properties = properties;
        log.info("✅ Virtual Thread support enabled (Java 21+)");
    }

    /**
     * 기본 Virtual Thread Executor
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Virtual Thread Factory Bean
     */
    @Bean
    public Thread.Builder.OfVirtual virtualThreadBuilder() {
        Thread.Builder.OfVirtual builder = Thread.ofVirtual();

        if (properties.getNamePrefix() != null) {
            builder.name(properties.getNamePrefix(), 0);
        }

        return builder;
    }

    /**
     * 커스텀 이름을 가진 Virtual Thread Executor
     */
    @Bean(name = "namedVirtualThreadExecutor")
    public Executor namedVirtualThreadExecutor(Thread.Builder.OfVirtual builder) {
        return task -> builder.start(task);
    }
}
