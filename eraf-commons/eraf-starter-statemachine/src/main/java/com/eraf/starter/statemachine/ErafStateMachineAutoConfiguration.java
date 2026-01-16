package com.eraf.starter.statemachine;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * ERAF StateMachine Auto Configuration
 *
 * StateStore 우선순위:
 * 1. eraf.statemachine.store-type=redis이고 Redis가 있으면 RedisStateStore
 * 2. eraf.statemachine.store-type=jdbc이고 DataSource가 있으면 JpaStateStore
 * 3. 기본값: InMemoryStateStore
 */
@AutoConfiguration
@ConditionalOnProperty(name = "eraf.statemachine.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafStateMachineProperties.class)
public class ErafStateMachineAutoConfiguration {

    /**
     * 상태 머신 레지스트리
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafStateMachineRegistry erafStateMachineRegistry() {
        return new ErafStateMachineRegistry();
    }

    /**
     * Redis 상태 저장소 (Redis가 있고 store-type=redis인 경우)
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(name = "eraf.statemachine.store-type", havingValue = "redis")
    @ConditionalOnMissingBean(StateStore.class)
    public StateStore redisStateStore(RedisTemplate<String, String> redisTemplate,
                                       ErafStateMachineProperties properties) {
        Duration ttl = properties.getStateTtl() != null ? properties.getStateTtl() : Duration.ofDays(7);
        return new RedisStateStore(redisTemplate, ttl);
    }

    /**
     * JDBC 상태 저장소 (DataSource가 있고 store-type=jdbc인 경우)
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "eraf.statemachine.store-type", havingValue = "jdbc")
    @ConditionalOnMissingBean(StateStore.class)
    public StateStore jdbcStateStore(DataSource dataSource, ErafStateMachineProperties properties) {
        return new JpaStateStore(dataSource, properties.isAutoCreateTable());
    }

    /**
     * 인메모리 상태 저장소 (기본값)
     */
    @Bean
    @ConditionalOnMissingBean(StateStore.class)
    public StateStore inMemoryStateStore() {
        return new InMemoryStateStore();
    }

    /**
     * 상태 머신 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafStateMachineService erafStateMachineService(
            ErafStateMachineRegistry registry,
            ApplicationEventPublisher eventPublisher,
            StateStore stateStore) {
        return new ErafStateMachineService(registry, eventPublisher, stateStore);
    }

    /**
     * @StateMachine 어노테이션 처리
     */
    @Bean
    @ConditionalOnMissingBean
    public StateMachineBeanPostProcessor stateMachineBeanPostProcessor(ErafStateMachineRegistry registry) {
        return new StateMachineBeanPostProcessor(registry);
    }
}
