package com.eraf.starter.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * @ErafScheduled 어노테이션 처리를 위한 BeanPostProcessor
 */
public class ErafScheduledBeanPostProcessor implements BeanPostProcessor {

    private final ErafJobRegistry jobRegistry;
    private final ErafJobHistory jobHistory;

    public ErafScheduledBeanPostProcessor(ErafJobRegistry jobRegistry, ErafJobHistory jobHistory) {
        this.jobRegistry = jobRegistry;
        this.jobHistory = jobHistory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();

        for (Method method : targetClass.getDeclaredMethods()) {
            ErafScheduled annotation = method.getAnnotation(ErafScheduled.class);
            if (annotation != null) {
                registerJob(annotation);
            }
        }

        return bean;
    }

    private void registerJob(ErafScheduled annotation) {
        ErafJobInfo jobInfo = new ErafJobInfo();
        jobInfo.setName(annotation.name());
        jobInfo.setGroup(annotation.group());
        jobInfo.setDescription(annotation.description());
        jobInfo.setLockEnabled(annotation.lockEnabled());
        jobInfo.setLockAtMostFor(annotation.lockAtMostFor());
        jobInfo.setLockAtLeastFor(annotation.lockAtLeastFor());
        jobInfo.setStatus(ErafJobInfo.JobStatus.SCHEDULED);

        if (!annotation.cron().isEmpty()) {
            jobInfo.setCron(annotation.cron());
        }
        if (annotation.fixedDelay() > 0) {
            jobInfo.setFixedDelay(annotation.fixedDelay());
        }
        if (annotation.fixedRate() > 0) {
            jobInfo.setFixedRate(annotation.fixedRate());
        }

        jobRegistry.register(jobInfo);
    }
}
