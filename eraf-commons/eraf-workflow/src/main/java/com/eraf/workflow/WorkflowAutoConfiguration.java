package com.eraf.workflow;

import com.eraf.workflow.repository.InMemoryWorkflowRepository;
import com.eraf.workflow.repository.WorkflowRepository;
import com.eraf.workflow.repository.jpa.JpaWorkflowRepository;
import com.eraf.workflow.repository.jpa.WorkflowInstanceJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ERAF Workflow Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(WorkflowProperties.class)
@ConditionalOnProperty(name = "eraf.workflow.enabled", havingValue = "true")
public class WorkflowAutoConfiguration {

    /**
     * Workflow Engine Bean
     */
    @Bean
    @ConditionalOnMissingBean(WorkflowEngine.class)
    public WorkflowEngine workflowEngine(WorkflowProperties properties) {
        return new WorkflowEngine(properties);
    }

    /**
     * In-Memory Repository (JPA 미사용 시 기본값)
     */
    @Bean
    @ConditionalOnMissingBean(WorkflowRepository.class)
    public WorkflowRepository inMemoryWorkflowRepository() {
        return new InMemoryWorkflowRepository();
    }

    /**
     * JPA Repository (JPA 사용 시)
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "jakarta.persistence.EntityManager")
    @ConditionalOnBean(WorkflowInstanceJpaRepository.class)
    static class JpaWorkflowRepositoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(WorkflowRepository.class)
        public WorkflowRepository jpaWorkflowRepository(
                WorkflowInstanceJpaRepository jpaRepository,
                ObjectMapper objectMapper) {
            return new JpaWorkflowRepository(jpaRepository, objectMapper);
        }
    }

    /**
     * Graceful Shutdown: WorkflowEngine SmartLifecycle
     * 애플리케이션 종료 시 진행 중인 워크플로우를 안전하게 종료합니다.
     */
    @Bean
    @ConditionalOnBean(WorkflowEngine.class)
    public SmartLifecycle workflowEngineLifecycle(WorkflowEngine engine) {
        return new WorkflowEngineLifecycle(engine);
    }

    /**
     * WorkflowEngine Graceful Shutdown을 위한 SmartLifecycle 구현
     */
    static class WorkflowEngineLifecycle implements SmartLifecycle {

        private static final Logger log = LoggerFactory.getLogger(WorkflowEngineLifecycle.class);

        private final WorkflowEngine engine;
        private volatile boolean running = false;

        WorkflowEngineLifecycle(WorkflowEngine engine) {
            this.engine = engine;
        }

        @Override
        public void start() {
            running = true;
            log.info("WorkflowEngine lifecycle started");
        }

        @Override
        public void stop() {
            running = false;
            log.info("Shutting down WorkflowEngine gracefully...");
            engine.close();
            log.info("WorkflowEngine shutdown complete");
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public int getPhase() {
            return Integer.MAX_VALUE - 100; // 늦게 시작, 일찍 종료
        }
    }
}
