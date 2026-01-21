package com.eraf.starter.scheduler;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.sql.DataSource;

/**
 * ERAF 스케줄러 Auto Configuration
 */
@AutoConfiguration
@ConditionalOnProperty(name = "eraf.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafSchedulerProperties.class)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class ErafSchedulerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolTaskScheduler taskScheduler(ErafSchedulerProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix("eraf-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    /**
     * 작업 레지스트리
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafJobRegistry erafJobRegistry() {
        return new ErafJobRegistry();
    }

    /**
     * 작업 이력 관리
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafJobHistory erafJobHistory(ErafSchedulerProperties properties) {
        return new ErafJobHistory(properties.getMaxHistoryPerJob());
    }

    /**
     * @ErafScheduled 어노테이션 처리
     */
    @Bean
    @ConditionalOnMissingBean
    public ErafScheduledBeanPostProcessor erafScheduledBeanPostProcessor(
            ErafJobRegistry jobRegistry, ErafJobHistory jobHistory) {
        return new ErafScheduledBeanPostProcessor(jobRegistry, jobHistory);
    }

    /**
     * Redis 기반 락 프로바이더
     */
    @Configuration
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "eraf.scheduler.distributed-lock-enabled", havingValue = "true", matchIfMissing = true)
    public static class RedisLockConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockProvider.class)
        public LockProvider redisLockProvider(RedisConnectionFactory connectionFactory) {
            return new RedisLockProvider(connectionFactory);
        }
    }

    /**
     * JDBC 기반 락 프로바이더
     */
    @Configuration
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(name = "eraf.scheduler.distributed-lock-enabled", havingValue = "true", matchIfMissing = true)
    public static class JdbcLockConfiguration {

        @Bean
        public LockProvider jdbcLockProvider(DataSource dataSource, ErafSchedulerProperties properties) {
            return new JdbcTemplateLockProvider(
                    JdbcTemplateLockProvider.Configuration.builder()
                            .withJdbcTemplate(new org.springframework.jdbc.core.JdbcTemplate(dataSource))
                            .withTableName(properties.getTableName())
                            .usingDbTime()
                            .build()
            );
        }
    }
}
