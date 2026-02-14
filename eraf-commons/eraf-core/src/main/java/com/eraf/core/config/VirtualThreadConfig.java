package com.eraf.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Virtual Thread 설정
 *
 * Java 21+ 환경에서 Virtual Thread 기반 Executor를 제공합니다.
 * eraf.core.virtual-thread.enabled=true 설정 시 활성화됩니다.
 */
@Configuration
@ConditionalOnProperty(name = "eraf.core.virtual-thread.enabled", havingValue = "true")
public class VirtualThreadConfig {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadConfig.class);

    /**
     * Virtual Thread 기반 ExecutorService
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        log.info("Initializing Virtual Thread Executor (Java 21+)");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Virtual Thread 기반 Spring TaskExecutor 어댑터
     */
    @Bean(name = "virtualThreadTaskExecutor")
    public TaskExecutor virtualThreadTaskExecutor() {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        return new VirtualThreadTaskExecutor(executor);
    }

    /**
     * Virtual Thread ExecutorService를 Spring TaskExecutor로 래핑
     */
    private static class VirtualThreadTaskExecutor implements TaskExecutor, AsyncTaskExecutor {

        private final ExecutorService executorService;

        VirtualThreadTaskExecutor(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void execute(Runnable task) {
            executorService.execute(task);
        }

        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            return executorService.submit(task);
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) {
            return executorService.submit(task);
        }
    }
}
