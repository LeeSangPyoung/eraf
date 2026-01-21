package com.eraf.starter.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ERAF 배치 잡 빌더
 * 표준화된 잡/스텝 생성을 위한 헬퍼 클래스
 */
public class ErafBatchJobBuilder {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ErafBatchProperties properties;

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
     */
    public <I, O> Step createChunkStep(String stepName,
                                        ItemReader<I> reader,
                                        ItemProcessor<I, O> processor,
                                        ItemWriter<O> writer,
                                        int chunkSize) {
        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new ErafStepListener())
                .build();
    }

    /**
     * 간단한 청크 스텝 생성 (프로세서 없음)
     */
    public <T> Step createSimpleChunkStep(String stepName,
                                           ItemReader<T> reader,
                                           ItemWriter<T> writer) {
        return new StepBuilder(stepName, jobRepository)
                .<T, T>chunk(properties.getChunkSize(), transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(new ErafStepListener())
                .build();
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
