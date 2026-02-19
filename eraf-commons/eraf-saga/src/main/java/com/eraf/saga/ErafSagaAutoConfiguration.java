package com.eraf.saga;

import com.eraf.messaging.MessagePublisher;
import com.eraf.saga.core.SagaOrchestrator;
import com.eraf.saga.core.SagaRegistrar;
import com.eraf.saga.event.MessagingSagaEventPublisher;
import com.eraf.saga.event.SagaEventPublisher;
import com.eraf.saga.event.SpringSagaEventPublisher;
import com.eraf.saga.recovery.SagaRecoveryService;
import com.eraf.saga.repository.InMemorySagaRepository;
import com.eraf.saga.repository.SagaRepository;
import com.eraf.saga.repository.jpa.JpaSagaRepository;
import com.eraf.saga.repository.jpa.SagaExecutionJpaRepository;
import com.eraf.saga.web.SagaController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ERAF Saga Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafSagaProperties.class)
@ConditionalOnProperty(name = "eraf.saga.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class ErafSagaAutoConfiguration {

    /**
     * InMemory Saga Repository (기본)
     */
    @Bean
    @ConditionalOnMissingBean(SagaRepository.class)
    @ConditionalOnProperty(name = "eraf.saga.repository-type", havingValue = "memory", matchIfMissing = true)
    public SagaRepository inMemorySagaRepository() {
        return new InMemorySagaRepository();
    }

    /**
     * Saga Orchestrator
     */
    @Bean
    @ConditionalOnMissingBean(SagaOrchestrator.class)
    public SagaOrchestrator sagaOrchestrator(SagaRepository sagaRepository,
                                              org.springframework.beans.factory.ObjectProvider<SagaEventPublisher> eventPublisherProvider) {
        SagaOrchestrator orchestrator = new SagaOrchestrator(sagaRepository);
        eventPublisherProvider.ifAvailable(orchestrator::setEventPublisher);
        return orchestrator;
    }

    /**
     * Saga Registrar (BeanPostProcessor)
     */
    @Bean
    public SagaRegistrar sagaRegistrar(SagaOrchestrator sagaOrchestrator) {
        return new SagaRegistrar(sagaOrchestrator);
    }

    /**
     * Saga 클린업 스케줄러
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.saga.cleanup-enabled", havingValue = "true", matchIfMissing = true)
    public SagaCleanupScheduler sagaCleanupScheduler(SagaRepository sagaRepository,
                                                      ErafSagaProperties properties) {
        return new SagaCleanupScheduler(sagaRepository, properties);
    }

    /**
     * Saga 복구 서비스
     */
    @Bean
    @ConditionalOnMissingBean(SagaRecoveryService.class)
    public SagaRecoveryService sagaRecoveryService(SagaRepository sagaRepository,
                                                    SagaOrchestrator sagaOrchestrator,
                                                    ErafSagaProperties properties) {
        return new SagaRecoveryService(sagaRepository, sagaOrchestrator, properties);
    }

    /**
     * Spring ApplicationEvent 기반 이벤트 발행자 (기본)
     */
    @Bean
    @ConditionalOnMissingBean(SagaEventPublisher.class)
    @ConditionalOnProperty(name = "eraf.saga.event-publisher-type", havingValue = "spring", matchIfMissing = true)
    public SagaEventPublisher springSagaEventPublisher(ApplicationEventPublisher eventPublisher) {
        return new SpringSagaEventPublisher(eventPublisher);
    }

    /**
     * REST API Controller
     */
    @Bean
    @ConditionalOnProperty(name = "eraf.saga.api-enabled", havingValue = "true", matchIfMissing = true)
    public SagaController sagaController(SagaOrchestrator sagaOrchestrator,
                                          SagaRepository sagaRepository,
                                          SagaRecoveryService recoveryService) {
        return new SagaController(sagaOrchestrator, sagaRepository, recoveryService);
    }

    /**
     * JPA Configuration
     */
    @Configuration
    @ConditionalOnClass(name = "jakarta.persistence.EntityManager")
    @ConditionalOnProperty(name = "eraf.saga.repository-type", havingValue = "jpa")
    public static class JpaRepositoryConfiguration {

        @Bean
        @ConditionalOnBean(SagaExecutionJpaRepository.class)
        public SagaRepository jpaSagaRepository(SagaExecutionJpaRepository jpaRepository,
                                                 ObjectMapper objectMapper) {
            return new JpaSagaRepository(jpaRepository, objectMapper);
        }
    }

    /**
     * Messaging Event Publisher Configuration
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.saga.event-publisher-type", havingValue = "messaging")
    public static class MessagingEventPublisherConfiguration {

        @Bean
        @ConditionalOnBean(MessagePublisher.class)
        public SagaEventPublisher messagingSagaEventPublisher(MessagePublisher messagePublisher,
                                                               ErafSagaProperties properties) {
            return new MessagingSagaEventPublisher(messagePublisher, properties);
        }
    }
}
