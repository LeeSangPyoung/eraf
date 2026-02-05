package com.eraf.saga.core;

import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.execution.StepExecution;
import com.eraf.saga.repository.SagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga Orchestrator
 * Saga 실행을 중앙에서 조율합니다.
 */
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final Map<String, SagaDefinition> sagaDefinitions = new ConcurrentHashMap<>();
    private final SagaRepository repository;

    public SagaOrchestrator(SagaRepository repository) {
        this.repository = repository;
    }

    /**
     * Saga 정의 등록
     */
    public void register(SagaDefinition definition) {
        sagaDefinitions.put(definition.getName(), definition);
        log.info("Saga registered: {} with {} steps", definition.getName(), definition.getTotalSteps());
    }

    /**
     * Saga 정의 조회
     */
    public SagaDefinition getDefinition(String sagaName) {
        return sagaDefinitions.get(sagaName);
    }

    /**
     * Saga 동기 실행
     */
    public SagaExecution execute(String sagaName, Object input) {
        return execute(sagaName, input, null);
    }

    /**
     * Saga 동기 실행 (traceId 포함)
     */
    public SagaExecution execute(String sagaName, Object input, String traceId) {
        SagaDefinition definition = sagaDefinitions.get(sagaName);
        if (definition == null) {
            throw new SagaException("Saga not found: " + sagaName);
        }

        SagaExecution execution = new SagaExecution(sagaName, traceId);
        initializeSteps(execution, definition);
        repository.save(execution);

        SagaContext context = new SagaContext(execution, input);
        SagaContext.setCurrent(context);

        try {
            execution.setStatus(SagaStatus.IN_PROGRESS);
            repository.save(execution);

            // 각 Step 순차 실행
            for (SagaDefinition.StepDefinition step : definition.getSteps()) {
                execution.setCurrentStep(step.getOrder());
                repository.save(execution);

                boolean success = executeStep(definition, step, context, execution);
                if (!success) {
                    // 실패 시 보상 트랜잭션 실행
                    compensate(definition, execution, context);
                    return execution;
                }
            }

            // 모든 Step 성공
            execution.markCompleted();
            repository.save(execution);
            log.info("Saga completed: {} (id={})", sagaName, execution.getId());

        } catch (Exception e) {
            log.error("Saga execution error: {} (id={})", sagaName, execution.getId(), e);
            execution.markFailed(e.getMessage());
            repository.save(execution);
            compensate(definition, execution, context);
        } finally {
            SagaContext.clear();
        }

        return execution;
    }

    /**
     * Saga 비동기 실행
     */
    public CompletableFuture<SagaExecution> executeAsync(String sagaName, Object input) {
        return executeAsync(sagaName, input, null);
    }

    /**
     * Saga 비동기 실행 (traceId 포함)
     */
    public CompletableFuture<SagaExecution> executeAsync(String sagaName, Object input, String traceId) {
        return CompletableFuture.supplyAsync(() -> execute(sagaName, input, traceId));
    }

    /**
     * Step 실행
     */
    private boolean executeStep(SagaDefinition definition, SagaDefinition.StepDefinition step,
                                SagaContext context, SagaExecution execution) {
        StepExecution stepExecution = execution.getSteps().get(step.getOrder() - 1);
        stepExecution.markRunning();
        repository.save(execution);

        int maxRetries = step.getRetries() > 0 ? step.getRetries() : definition.getMaxRetries();
        long retryDelay = step.getRetryDelay();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                log.debug("Executing step {}/{}: {} (attempt {})",
                        step.getOrder(), definition.getTotalSteps(), step.getName(), attempt + 1);

                Object result = invokeMethod(definition.getSagaInstance(), step.getMethod(), context);
                String output = result != null ? result.toString() : null;
                stepExecution.markSuccess(output);
                repository.save(execution);

                log.debug("Step {} completed successfully", step.getName());
                return true;

            } catch (Exception e) {
                log.warn("Step {} failed (attempt {}): {}", step.getName(), attempt + 1, e.getMessage());
                stepExecution.incrementRetryCount();

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    stepExecution.markFailed(e.getMessage());
                    execution.markFailed("Step " + step.getName() + " failed: " + e.getMessage());
                    repository.save(execution);
                }
            }
        }

        return false;
    }

    /**
     * 보상 트랜잭션 실행
     */
    private void compensate(SagaDefinition definition, SagaExecution execution, SagaContext context) {
        log.info("Starting compensation for saga: {} (id={})", definition.getName(), execution.getId());

        // 역순으로 보상 실행
        for (int i = execution.getCurrentStep() - 1; i >= 0; i--) {
            SagaDefinition.StepDefinition step = definition.getSteps().get(i);
            StepExecution stepExecution = execution.getSteps().get(i);

            if (stepExecution.getStatus() != StepStatus.SUCCESS) {
                continue;
            }

            if (!step.hasCompensation()) {
                log.warn("No compensation defined for step: {}", step.getName());
                continue;
            }

            try {
                stepExecution.markCompensating();
                repository.save(execution);

                log.debug("Compensating step {}: {}", step.getOrder(), step.getName());
                invokeMethod(definition.getSagaInstance(), step.getCompensateMethod(), context);

                stepExecution.markCompensated();
                repository.save(execution);
                log.debug("Step {} compensated successfully", step.getName());

            } catch (Exception e) {
                log.error("Compensation failed for step {}: {}", step.getName(), e.getMessage(), e);
                execution.markCompensationFailed();
                repository.save(execution);
                return;
            }
        }

        execution.markCompensated();
        repository.save(execution);
        log.info("Compensation completed for saga: {} (id={})", definition.getName(), execution.getId());
    }

    /**
     * Step 실행 초기화
     */
    private void initializeSteps(SagaExecution execution, SagaDefinition definition) {
        for (SagaDefinition.StepDefinition step : definition.getSteps()) {
            StepExecution stepExecution = new StepExecution(step.getOrder(), step.getName());
            execution.addStep(stepExecution);
        }
    }

    /**
     * 메서드 호출
     */
    private Object invokeMethod(Object instance, Method method, SagaContext context) throws Exception {
        method.setAccessible(true);

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return method.invoke(instance);
        } else if (paramTypes.length == 1) {
            if (SagaContext.class.isAssignableFrom(paramTypes[0])) {
                return method.invoke(instance, context);
            } else {
                return method.invoke(instance, context.getInput());
            }
        } else {
            // 첫 번째 파라미터: input, 두 번째: context
            return method.invoke(instance, context.getInput(), context);
        }
    }

    /**
     * 실행 상태 조회
     */
    public SagaExecution getExecution(String executionId) {
        return repository.findById(executionId).orElse(null);
    }
}
