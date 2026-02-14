package com.eraf.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Workflow 엔진
 * <p>
 * 워크플로우 정의 등록, 인스턴스 생성/실행, 재개/취소 등 핵심 기능을 제공합니다.
 * In-memory 방식으로 ConcurrentHashMap을 사용하여 인스턴스를 저장합니다.
 *
 * <pre>{@code
 * // 1. 엔진 생성
 * WorkflowEngine engine = new WorkflowEngine();
 *
 * // 2. 핸들러 등록
 * engine.registerHandler("submit", context -> {
 *     context.setVariable("submittedBy", "user1");
 *     return WorkflowStepResult.completed();
 * });
 *
 * // 3. 워크플로우 시작
 * WorkflowInstance instance = engine.startWorkflow(definition, Map.of("docId", "DOC-001"));
 * }</pre>
 */
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInstance> instances = new ConcurrentHashMap<>();
    private final Map<String, WorkflowStepHandler> handlers = new ConcurrentHashMap<>();

    private final WorkflowProperties properties;

    public WorkflowEngine() {
        this(new WorkflowProperties());
    }

    public WorkflowEngine(WorkflowProperties properties) {
        this.properties = properties;
    }

    // --- 정의 관리 ---

    /**
     * Workflow 정의 등록
     */
    public void registerDefinition(WorkflowDefinition definition) {
        definitions.put(definition.getName(), definition);
        log.info("Workflow definition registered: {} ({} steps)",
                definition.getName(), definition.getStepCount());
    }

    /**
     * Workflow 정의 조회
     */
    public WorkflowDefinition getDefinition(String name) {
        return definitions.get(name);
    }

    /**
     * 등록된 모든 Workflow 정의 이름 조회
     */
    public List<String> getDefinitionNames() {
        return new ArrayList<>(definitions.keySet());
    }

    // --- 핸들러 관리 ---

    /**
     * Step 핸들러 등록
     *
     * @param stepName Step 이름
     * @param handler  실행 핸들러
     */
    public void registerHandler(String stepName, WorkflowStepHandler handler) {
        handlers.put(stepName, handler);
        log.debug("Handler registered for step: {}", stepName);
    }

    // --- Workflow 실행 ---

    /**
     * Workflow 시작
     *
     * @param definition 워크플로우 정의
     * @param variables  초기 변수
     * @return 생성된 워크플로우 인스턴스
     */
    public WorkflowInstance startWorkflow(WorkflowDefinition definition, Map<String, Object> variables) {
        // 정의 등록 (아직 안 되어 있다면)
        if (!definitions.containsKey(definition.getName())) {
            registerDefinition(definition);
        }

        // 동시 실행 제한 확인
        if (properties.getMaxConcurrentWorkflows() > 0) {
            long activeCount = instances.values().stream()
                    .filter(i -> i.getStatus() == WorkflowStatus.RUNNING
                              || i.getStatus() == WorkflowStatus.SUSPENDED)
                    .count();
            if (activeCount >= properties.getMaxConcurrentWorkflows()) {
                throw new WorkflowException(
                        "Maximum concurrent workflows reached: " + properties.getMaxConcurrentWorkflows());
            }
        }

        // 인스턴스 생성
        WorkflowInstance instance = new WorkflowInstance(definition.getName(), variables);
        instances.put(instance.getId(), instance);
        log.info("Workflow started: {} (id={})", definition.getName(), instance.getId());

        // 첫 번째 Step부터 실행
        WorkflowStep firstStep = definition.getFirstStep();
        if (firstStep != null) {
            instance.markRunning(firstStep.getName());
            executeSteps(instance, definition, firstStep.getName());
        } else {
            instance.markCompleted();
        }

        return instance;
    }

    /**
     * Workflow 시작 (정의 이름으로)
     */
    public WorkflowInstance startWorkflow(String definitionName, Map<String, Object> variables) {
        WorkflowDefinition definition = definitions.get(definitionName);
        if (definition == null) {
            throw new WorkflowException("Workflow definition not found: " + definitionName);
        }
        return startWorkflow(definition, variables);
    }

    /**
     * 중단된 Workflow 재개
     * <p>
     * SUSPENDED 상태의 워크플로우를 다음 Step부터 재개합니다.
     * 주로 APPROVAL Step 이후 승인 완료 시 호출합니다.
     *
     * @param instanceId 인스턴스 ID
     * @return 재개된 워크플로우 인스턴스
     */
    public WorkflowInstance resumeWorkflow(String instanceId) {
        return resumeWorkflow(instanceId, null);
    }

    /**
     * 중단된 Workflow 재개 (추가 변수 포함)
     *
     * @param instanceId         인스턴스 ID
     * @param additionalVariables 추가 변수 (승인자 정보 등)
     * @return 재개된 워크플로우 인스턴스
     */
    public WorkflowInstance resumeWorkflow(String instanceId, Map<String, Object> additionalVariables) {
        WorkflowInstance instance = getInstance(instanceId);
        if (instance.getStatus() != WorkflowStatus.SUSPENDED) {
            throw new WorkflowException(
                    "Workflow is not suspended (current status: " + instance.getStatus() + ")",
                    instanceId);
        }

        WorkflowDefinition definition = definitions.get(instance.getWorkflowName());
        if (definition == null) {
            throw new WorkflowException(
                    "Workflow definition not found: " + instance.getWorkflowName(), instanceId);
        }

        // 추가 변수 반영
        if (additionalVariables != null) {
            instance.putVariables(additionalVariables);
        }

        // 현재 Step의 다음 Step부터 재개
        String currentStep = instance.getCurrentStep();
        WorkflowStep nextStep = definition.getNextStep(currentStep);

        if (nextStep != null) {
            instance.markRunning(nextStep.getName());
            log.info("Workflow resumed: {} (id={}) from step '{}'",
                    instance.getWorkflowName(), instanceId, nextStep.getName());
            executeSteps(instance, definition, nextStep.getName());
        } else {
            // 마지막 Step이었다면 완료 처리
            instance.markCompleted();
            log.info("Workflow completed after resume: {} (id={})",
                    instance.getWorkflowName(), instanceId);
        }

        return instance;
    }

    /**
     * Workflow 취소
     *
     * @param instanceId 인스턴스 ID
     * @return 취소된 워크플로우 인스턴스
     */
    public WorkflowInstance cancelWorkflow(String instanceId) {
        WorkflowInstance instance = getInstance(instanceId);
        if (instance.isTerminal()) {
            throw new WorkflowException(
                    "Workflow already in terminal state: " + instance.getStatus(), instanceId);
        }

        instance.markCancelled();
        log.info("Workflow cancelled: {} (id={})", instance.getWorkflowName(), instanceId);
        return instance;
    }

    // --- 상태 조회 ---

    /**
     * 인스턴스 조회
     */
    public WorkflowInstance getInstance(String instanceId) {
        WorkflowInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new WorkflowException("Workflow instance not found: " + instanceId);
        }
        return instance;
    }

    /**
     * 인스턴스 상태 조회
     */
    public WorkflowStatus getStatus(String instanceId) {
        return getInstance(instanceId).getStatus();
    }

    /**
     * 모든 인스턴스 조회 (읽기 전용)
     */
    public List<WorkflowInstance> getAllInstances() {
        return Collections.unmodifiableList(new ArrayList<>(instances.values()));
    }

    /**
     * 상태별 인스턴스 조회
     */
    public List<WorkflowInstance> getInstancesByStatus(WorkflowStatus status) {
        return instances.values().stream()
                .filter(i -> i.getStatus() == status)
                .toList();
    }

    // --- 내부 실행 로직 ---

    /**
     * 지정된 Step부터 순차 실행
     */
    private void executeSteps(WorkflowInstance instance, WorkflowDefinition definition, String startStepName) {
        String currentStepName = startStepName;

        while (currentStepName != null && !instance.isTerminal()) {
            WorkflowStep step = definition.getStep(currentStepName);
            if (step == null) {
                instance.markFailed("Step not found in definition: " + currentStepName);
                log.error("Step not found: '{}' in workflow '{}'", currentStepName, definition.getName());
                return;
            }

            instance.setCurrentStep(currentStepName);

            // Step 실행
            WorkflowStepResult result = executeStep(instance, step);

            // 결과에 따라 다음 동작 결정
            if (result.isCompleted()) {
                // 출력 데이터를 변수에 병합
                if (!result.getOutputData().isEmpty()) {
                    instance.putVariables(result.getOutputData());
                }

                // 다음 Step 결정
                if (result.getNextStep() != null) {
                    // 조건 분기: result에서 지정한 다음 Step
                    currentStepName = result.getNextStep();
                } else if (!step.getNextSteps().isEmpty()) {
                    // Step 정의에 지정된 다음 Step
                    currentStepName = step.getNextSteps().get(0);
                } else {
                    // 순서상 다음 Step
                    WorkflowStep nextStep = definition.getNextStep(currentStepName);
                    currentStepName = nextStep != null ? nextStep.getName() : null;
                }

                // 다음 Step이 없으면 완료
                if (currentStepName == null) {
                    instance.markCompleted();
                    log.info("Workflow completed: {} (id={})",
                            instance.getWorkflowName(), instance.getId());
                }

            } else if (result.isWaiting()) {
                // APPROVAL Step 등: SUSPENDED 상태로 전환 후 루프 종료
                if (!result.getOutputData().isEmpty()) {
                    instance.putVariables(result.getOutputData());
                }
                instance.markSuspended();
                log.info("Workflow suspended at step '{}': {} (id={})",
                        currentStepName, instance.getWorkflowName(), instance.getId());
                return;

            } else if (result.isFailed()) {
                instance.markFailed(result.getErrorMessage());
                log.warn("Workflow failed at step '{}': {} (id={}) - {}",
                        currentStepName, instance.getWorkflowName(), instance.getId(),
                        result.getErrorMessage());
                return;
            }
        }
    }

    /**
     * 단일 Step 실행
     */
    private WorkflowStepResult executeStep(WorkflowInstance instance, WorkflowStep step) {
        // 핸들러 찾기
        WorkflowStepHandler handler = handlers.get(step.getName());
        if (handler == null) {
            String errorMsg = "No handler registered for step: " + step.getName();
            log.error(errorMsg);
            WorkflowStepExecution execution = new WorkflowStepExecution(
                    step.getName(), WorkflowStepResultStatus.FAILED, null, errorMsg);
            instance.addStepExecution(execution);
            return WorkflowStepResult.failed(errorMsg);
        }

        // 컨텍스트 생성
        WorkflowContext context = new WorkflowContext(
                instance.getId(), step.getName(), instance.getVariables());

        // 재시도 로직 포함 실행
        int maxAttempts = step.isRetryable() ? step.getMaxRetries() + 1 : 1;
        WorkflowStepResult result = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Executing step '{}' (attempt {}/{})", step.getName(), attempt, maxAttempts);
                result = handler.execute(context);

                // 컨텍스트에서 변경된 변수를 인스턴스에 반영
                syncVariables(instance, context);

                // 실행 이력 기록
                WorkflowStepExecution execution = new WorkflowStepExecution(
                        step.getName(), result.getStatus(), result.getOutputData(), result.getErrorMessage());
                instance.addStepExecution(execution);

                if (!result.isFailed() || attempt == maxAttempts) {
                    return result;
                }

                log.warn("Step '{}' failed (attempt {}/{}): {}", step.getName(), attempt, maxAttempts,
                        result.getErrorMessage());

            } catch (Exception e) {
                log.warn("Step '{}' threw exception (attempt {}/{}): {}",
                        step.getName(), attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) {
                    String errorMsg = "Step '" + step.getName() + "' failed after " + maxAttempts
                            + " attempts: " + e.getMessage();
                    WorkflowStepExecution execution = new WorkflowStepExecution(
                            step.getName(), WorkflowStepResultStatus.FAILED, null, errorMsg);
                    instance.addStepExecution(execution);
                    return WorkflowStepResult.failed(errorMsg);
                }
            }
        }

        // 여기 도달할 수 없지만 방어적 코딩
        return WorkflowStepResult.failed("Unexpected execution error for step: " + step.getName());
    }

    /**
     * 컨텍스트 변수 변경 사항을 인스턴스에 동기화
     */
    private void syncVariables(WorkflowInstance instance, WorkflowContext context) {
        for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
            instance.putVariable(entry.getKey(), entry.getValue());
        }
    }
}
