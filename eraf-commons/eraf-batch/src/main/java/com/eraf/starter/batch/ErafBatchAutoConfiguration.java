package com.eraf.starter.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ERAF Batch Auto Configuration
 * Spring Batch 자동 구성
 */
@AutoConfiguration
@ConditionalOnClass(JobLauncher.class)
@EnableConfigurationProperties(ErafBatchProperties.class)
public class ErafBatchAutoConfiguration {

    /**
     * ERAF 배치 잡 빌더 빈 등록
     */
    @Bean
    @ConditionalOnBean({JobRepository.class, PlatformTransactionManager.class})
    @ConditionalOnMissingBean(ErafBatchJobBuilder.class)
    public ErafBatchJobBuilder erafBatchJobBuilder(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ErafBatchProperties properties) {
        return new ErafBatchJobBuilder(jobRepository, transactionManager, properties);
    }

    /**
     * 기본 잡 리스너 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean(ErafJobListener.class)
    public ErafJobListener erafJobListener() {
        return new ErafJobListener();
    }

    /**
     * 기본 스텝 리스너 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean(ErafStepListener.class)
    public ErafStepListener erafStepListener() {
        return new ErafStepListener();
    }
}
