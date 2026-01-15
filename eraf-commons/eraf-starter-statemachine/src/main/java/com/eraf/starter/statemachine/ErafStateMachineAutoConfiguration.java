package com.eraf.starter.statemachine;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * ERAF StateMachine Auto Configuration
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
     * 상태 머신 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafStateMachineService erafStateMachineService(
            ErafStateMachineRegistry registry, ApplicationEventPublisher eventPublisher) {
        return new ErafStateMachineService(registry, eventPublisher);
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
