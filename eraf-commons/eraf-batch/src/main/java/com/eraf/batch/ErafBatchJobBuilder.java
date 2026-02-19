package com.eraf.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ERAF 배치 잡 빌더
 * 표준화된 잡/스텝 생성을 위한 헬퍼 클래스
 *
 * <p>생성된 ThreadPoolTaskExecutor 인스턴스의 라이프사이클을 관리하며,
 * 빈 소멸 시 자동으로 정리합니다.</p>
 */
public class ErafBatchJobBuilder implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(ErafBatchJobBuilder.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ErafBatchProperties properties;
    private final List<ThreadPoolTaskExecutor> managedExecutors = new ArrayList<>();

    public ErafBatchJobBuilder(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ErafBatchProperties properties) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.properties = properties;
    }

    /**
     * 잡 빌더 생성
     */
    public JobBuilder job(String jobName) {
        return new JobBuilder(jobName, jobRepository)
                .listener(new ErafJobListener());
    }

    /**
     * 스텝 빌더 생성
     */
    public StepBuilder step(String stepName) {
        return new StepBuilder(stepName, jobRepository);
    }

    /**
     * 청크 기반 스텝 생성
     */
    public <I, O> Step createChunkStep(String stepName,
                                        ItemReader<I> reader,
                                        ItemProcessor<I, O> processor,
                                        ItemWriter<O> writer) {
        return createChunkStep(stepName, reader, processor, writer, properties.getChunkSize());
    }

    /**
     * 청크 기반 스텝 생성 (청크 크기 지정)
     * Retry/Skip/Parallel 설정이 properties에 따라 자동 적용됨
     */
    public <I, O> Step createChunkStep(String stepName,
                                        ItemReader<I> reader,
                                        ItemProcessor<I, O> processor,
                                        ItemWriter<O> writer,
                                        int chunkSize) {
        var stepBuilder = new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new ErafStepListener());

        // Fault Tolerance: Retry 설정
        if (properties.getRetry().isEnabled()) {
            stepBuilder = stepBuilder.faultTolerant()
                    .retry(Exception.class)
                    .retryLimit(properties.getRetry().getMaxAttempts());
        }

        // Fault Tolerance: Skip 설정
        if (properties.getSkip().isEnabled()) {
            stepBuilder = stepBuilder.faultTolerant()
                    .skip(Exception.class)
                    .skipLimit(properties.getSkip().getMaxSkips());
        }

        // 병렬 처리: TaskExecutor 설정
        if (properties.getThreadPool().isEnabled()) {
            stepBuilder = stepBuilder.taskExecutor(createManagedExecutor());
        }

        return stepBuilder.build();
    }

    /**
     * 간단한 청크 스텝 생성 (프로세서 없음)
     * Retry/Skip/Parallel 설정이 properties에 따라 자동 적용됨
     */
    public <T> Step createSimpleChunkStep(String stepName,
                                           ItemReader<T> reader,
                                           ItemWriter<T> writer) {
        var stepBuilder = new StepBuilder(stepName, jobRepository)
                .<T, T>chunk(properties.getChunkSize(), transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(new ErafStepListener());

        if (properties.getRetry().isEnabled()) {
            stepBuilder = stepBuilder.faultTolerant()
                    .retry(Exception.class)
                    .retryLimit(properties.getRetry().getMaxAttempts());
        }

        if (properties.getSkip().isEnabled()) {
            stepBuilder = stepBuilder.faultTolerant()
                    .skip(Exception.class)
                    .skipLimit(properties.getSkip().getMaxSkips());
        }

        if (properties.getThreadPool().isEnabled()) {
            stepBuilder = stepBuilder.taskExecutor(createManagedExecutor());
        }

        return stepBuilder.build();
    }

    /**
     * 완전한 잡 생성 (단일 스텝)
     */
    public <I, O> Job createSimpleJob(String jobName,
                                       String stepName,
                                       ItemReader<I> reader,
                                       ItemProcessor<I, O> processor,
                                       ItemWriter<O> writer) {
        Step step = createChunkStep(stepName, reader, processor, writer);
        return job(jobName)
                .start(step)
                .build();
    }

    /**
     * 라이프사이클이 관리되는 ThreadPoolTaskExecutor 생성
     */
    private ThreadPoolTaskExecutor createManagedExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getThreadPool().getCorePoolSize());
        executor.setMaxPoolSize(properties.getThreadPool().getMaxPoolSize());
        executor.setQueueCapacity(properties.getThreadPool().getQueueCapacity());
        executor.setThreadNamePrefix("eraf-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        managedExecutors.add(executor);
        return executor;
    }

    @Override
    public void destroy() {
        log.info("Shutting down {} managed batch executors", managedExecutors.size());
        for (ThreadPoolTaskExecutor executor : managedExecutors) {
            executor.shutdown();
        }
        managedExecutors.clear();
    }

    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public ErafBatchProperties getProperties() {
        return properties;
    }
}
