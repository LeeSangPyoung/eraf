package com.eraf.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

/**
 * @ErafScheduled 어노테이션 처리를 위한 BeanPostProcessor
 * 작업 등록 + 실제 스케줄링 실행 + ShedLock 분산 락 통합
 */
public class ErafScheduledBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ErafScheduledBeanPostProcessor.class);

    private final ErafJobRegistry jobRegistry;
    private final ErafJobHistory jobHistory;
    private final TaskScheduler taskScheduler;
    private final Object lockProvider; // ShedLock LockProvider (optional, reflection으로 사용)

    public ErafScheduledBeanPostProcessor(ErafJobRegistry jobRegistry, ErafJobHistory jobHistory,
                                          TaskScheduler taskScheduler) {
        this(jobRegistry, jobHistory, taskScheduler, null);
    }

    public ErafScheduledBeanPostProcessor(ErafJobRegistry jobRegistry, ErafJobHistory jobHistory,
                                          TaskScheduler taskScheduler, Object lockProvider) {
        this.jobRegistry = jobRegistry;
        this.jobHistory = jobHistory;
        this.taskScheduler = taskScheduler;
        this.lockProvider = lockProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();

        for (Method method : targetClass.getDeclaredMethods()) {
            ErafScheduled annotation = method.getAnnotation(ErafScheduled.class);
            if (annotation != null) {
                registerJob(annotation);
                scheduleJob(bean, method, annotation);
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

    private void scheduleJob(Object bean, Method method, ErafScheduled annotation) {
        if (taskScheduler == null) {
            log.warn("TaskScheduler not available, skipping scheduling for: {}", annotation.name());
            return;
        }

        // enabled 체크
        if ("false".equalsIgnoreCase(annotation.enabled())) {
            log.info("Job '{}' is disabled, skipping scheduling", annotation.name());
            return;
        }

        method.setAccessible(true);
        Runnable task = createTask(bean, method, annotation);

        if (!annotation.cron().isEmpty()) {
            // Cron 스케줄
            ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(annotation.cron()));
            log.info("Scheduled job '{}' with cron: {}", annotation.name(), annotation.cron());
        } else if (annotation.fixedRate() > 0) {
            // Fixed Rate
            Instant startTime = Instant.now().plusMillis(annotation.initialDelay());
            ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, startTime,
                    Duration.ofMillis(annotation.fixedRate()));
            log.info("Scheduled job '{}' with fixedRate: {}ms", annotation.name(), annotation.fixedRate());
        } else if (annotation.fixedDelay() > 0) {
            // Fixed Delay
            ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(task,
                    Instant.now().plusMillis(annotation.initialDelay()),
                    Duration.ofMillis(annotation.fixedDelay()));
            log.info("Scheduled job '{}' with fixedDelay: {}ms", annotation.name(), annotation.fixedDelay());
        }
    }

    /**
     * ShedLock 분산 락을 포함하는 실행 태스크 생성
     */
    private Runnable createTask(Object bean, Method method, ErafScheduled annotation) {
        return () -> {
            String jobName = annotation.name();
            String executionId = jobHistory.recordStart(jobName);
            jobRegistry.updateStatus(jobName, ErafJobInfo.JobStatus.RUNNING);

            try {
                // ShedLock 분산 락 획득 시도
                if (annotation.lockEnabled() && lockProvider != null) {
                    boolean executed = executeWithLock(bean, method, annotation);
                    if (!executed) {
                        // 락 획득 실패 - 다른 인스턴스에서 실행 중
                        jobHistory.recordSuccess(executionId, "SKIPPED (lock held by another instance)");
                        jobRegistry.updateStatus(jobName, ErafJobInfo.JobStatus.SCHEDULED);
                        return;
                    }
                } else {
                    // 락 없이 직접 실행
                    method.invoke(bean);
                }

                jobHistory.recordSuccess(executionId, "SUCCESS");
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log.error("Job '{}' execution failed", jobName, cause);
                jobHistory.recordFailure(executionId, cause);
            } finally {
                jobRegistry.updateStatus(jobName, ErafJobInfo.JobStatus.SCHEDULED);
            }
        };
    }

    /**
     * ShedLock 분산 락으로 보호하여 실행 (Reflection 사용)
     *
     * @return true if lock acquired and executed, false if lock not acquired
     */
    private boolean executeWithLock(Object bean, Method method, ErafScheduled annotation) throws Exception {
        try {
            // ShedLock API: LockProvider.lock(LockConfiguration)
            Class<?> lockConfigClass = Class.forName("net.javacrumbs.shedlock.core.LockConfiguration");
            Class<?> simpleLockClass = Class.forName("net.javacrumbs.shedlock.core.SimpleLock");

            Duration lockAtMostFor = Duration.parse(annotation.lockAtMostFor());
            Duration lockAtLeastFor = Duration.parse(annotation.lockAtLeastFor());
            Instant now = Instant.now();

            // new LockConfiguration(now, name, lockAtMostFor, lockAtLeastFor)
            Object lockConfig = lockConfigClass.getConstructor(
                    Instant.class, String.class, Duration.class, Duration.class
            ).newInstance(now, annotation.name(), lockAtMostFor, lockAtLeastFor);

            // lockProvider.lock(lockConfig) -> Optional<SimpleLock>
            Method lockMethod = lockProvider.getClass().getMethod("lock", lockConfigClass);
            Object optionalLock = lockMethod.invoke(lockProvider, lockConfig);

            // Optional.isPresent()
            Method isPresentMethod = optionalLock.getClass().getMethod("isPresent");
            boolean isPresent = (boolean) isPresentMethod.invoke(optionalLock);

            if (!isPresent) {
                return false; // 다른 인스턴스가 이미 락 보유
            }

            try {
                method.invoke(bean);
            } finally {
                // lock.unlock()
                Method getMethod = optionalLock.getClass().getMethod("get");
                Object lock = getMethod.invoke(optionalLock);
                Method unlockMethod = lock.getClass().getMethod("unlock");
                unlockMethod.invoke(lock);
            }

            return true;
        } catch (ClassNotFoundException e) {
            // ShedLock 라이브러리 없음 - 락 없이 실행
            log.debug("ShedLock not available, executing without lock: {}", annotation.name());
            method.invoke(bean);
            return true;
        }
    }
}
