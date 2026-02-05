package com.eraf.sample.controller.saga;

import com.eraf.saga.core.*;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.execution.StepExecution;
import com.eraf.saga.repository.InMemorySagaRepository;
import com.eraf.saga.repository.SagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Saga 데모 컨트롤러 (#35~#42)
 *
 * #35 주문 Saga 정의
 * #36 Saga 실행 시작
 * #37 Step별 진행 상태 조회
 * #38 보상 트랜잭션 시뮬레이션
 * #39 Saga 실행 이력 조회
 * #40 실패 Saga 재시도
 * #41 Saga 타임아웃 처리
 * #42 분산 환경 시뮬레이션
 */
@Slf4j
@Controller
@RequestMapping("/saga")
public class SagaDemoController {

    private final SagaRepository sagaRepository;
    private final SagaOrchestrator orchestrator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // 실행 중인 Saga 추적 (UI 업데이트용)
    private final Map<String, SagaExecutionProgress> executionProgress = new ConcurrentHashMap<>();

    // 시뮬레이션 실패 확률 설정
    private final Map<String, Integer> stepFailureRates = new ConcurrentHashMap<>();

    public SagaDemoController() {
        this.sagaRepository = new InMemorySagaRepository();
        this.orchestrator = new SagaOrchestrator(sagaRepository);
        initializeSagas();
    }

    private void initializeSagas() {
        // #35 주문 Saga 정의
        registerOrderSaga();
        registerPaymentSaga();
        registerShippingSaga();
    }

    private void registerOrderSaga() {
        SagaDefinition def = new SagaDefinition("order-saga", SagaDemoController.class);
        def.setDescription("Order Processing Saga");
        def.setTimeout(30000);
        def.setMaxRetries(2);
        def.setSagaInstance(this);

        try {
            // Step 1: Validate Order
            SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition();
            step1.setOrder(1);
            step1.setName("validateOrder");
            step1.setMethod(getMethod("stepValidateOrder"));
            step1.setCompensateMethod(getMethod("compensateValidateOrder"));
            step1.setRetries(1);
            step1.setRetryDelay(500);
            def.addStep(step1);

            // Step 2: Reserve Inventory
            SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition();
            step2.setOrder(2);
            step2.setName("reserveInventory");
            step2.setMethod(getMethod("stepReserveInventory"));
            step2.setCompensateMethod(getMethod("compensateReserveInventory"));
            step2.setRetries(2);
            step2.setRetryDelay(1000);
            def.addStep(step2);

            // Step 3: Process Payment
            SagaDefinition.StepDefinition step3 = new SagaDefinition.StepDefinition();
            step3.setOrder(3);
            step3.setName("processPayment");
            step3.setMethod(getMethod("stepProcessPayment"));
            step3.setCompensateMethod(getMethod("compensateProcessPayment"));
            step3.setRetries(2);
            step3.setRetryDelay(1000);
            def.addStep(step3);

            // Step 4: Create Shipment
            SagaDefinition.StepDefinition step4 = new SagaDefinition.StepDefinition();
            step4.setOrder(4);
            step4.setName("createShipment");
            step4.setMethod(getMethod("stepCreateShipment"));
            step4.setCompensateMethod(getMethod("compensateCreateShipment"));
            step4.setRetries(1);
            step4.setRetryDelay(500);
            def.addStep(step4);

            // Step 5: Send Notification
            SagaDefinition.StepDefinition step5 = new SagaDefinition.StepDefinition();
            step5.setOrder(5);
            step5.setName("sendNotification");
            step5.setMethod(getMethod("stepSendNotification"));
            // 알림은 보상 없음
            def.addStep(step5);

            orchestrator.register(def);
        } catch (Exception e) {
            log.error("Failed to register order saga", e);
        }
    }

    private void registerPaymentSaga() {
        SagaDefinition def = new SagaDefinition("payment-saga", SagaDemoController.class);
        def.setDescription("Payment Processing Saga");
        def.setTimeout(20000);
        def.setMaxRetries(3);
        def.setSagaInstance(this);

        try {
            SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition();
            step1.setOrder(1);
            step1.setName("validateCard");
            step1.setMethod(getMethod("stepValidateCard"));
            step1.setCompensateMethod(getMethod("compensateValidateCard"));
            def.addStep(step1);

            SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition();
            step2.setOrder(2);
            step2.setName("authorizePayment");
            step2.setMethod(getMethod("stepAuthorizePayment"));
            step2.setCompensateMethod(getMethod("compensateAuthorizePayment"));
            def.addStep(step2);

            SagaDefinition.StepDefinition step3 = new SagaDefinition.StepDefinition();
            step3.setOrder(3);
            step3.setName("capturePayment");
            step3.setMethod(getMethod("stepCapturePayment"));
            step3.setCompensateMethod(getMethod("compensateCapturePayment"));
            def.addStep(step3);

            orchestrator.register(def);
        } catch (Exception e) {
            log.error("Failed to register payment saga", e);
        }
    }

    private void registerShippingSaga() {
        SagaDefinition def = new SagaDefinition("shipping-saga", SagaDemoController.class);
        def.setDescription("Shipping Saga");
        def.setTimeout(15000);
        def.setMaxRetries(2);
        def.setSagaInstance(this);

        try {
            SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition();
            step1.setOrder(1);
            step1.setName("pickItems");
            step1.setMethod(getMethod("stepPickItems"));
            step1.setCompensateMethod(getMethod("compensatePickItems"));
            def.addStep(step1);

            SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition();
            step2.setOrder(2);
            step2.setName("packItems");
            step2.setMethod(getMethod("stepPackItems"));
            step2.setCompensateMethod(getMethod("compensatePackItems"));
            def.addStep(step2);

            SagaDefinition.StepDefinition step3 = new SagaDefinition.StepDefinition();
            step3.setOrder(3);
            step3.setName("shipPackage");
            step3.setMethod(getMethod("stepShipPackage"));
            step3.setCompensateMethod(getMethod("compensateShipPackage"));
            def.addStep(step3);

            orchestrator.register(def);
        } catch (Exception e) {
            log.error("Failed to register shipping saga", e);
        }
    }

    private Method getMethod(String name) throws NoSuchMethodException {
        return SagaDemoController.class.getMethod(name, SagaContext.class);
    }

    @GetMapping
    public String sagaPage(Model model) {
        return "saga/saga";
    }

    // ===== #35 Saga 정의 조회 =====
    @GetMapping("/definitions")
    @ResponseBody
    public Map<String, Object> getDefinitions() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> definitions = new ArrayList<>();
        for (String name : List.of("order-saga", "payment-saga", "shipping-saga")) {
            SagaDefinition def = orchestrator.getDefinition(name);
            if (def != null) {
                definitions.add(definitionToMap(def));
            }
        }

        result.put("definitions", definitions);
        return result;
    }

    // ===== #36 Saga 실행 시작 =====
    @PostMapping("/execute")
    @ResponseBody
    public Map<String, Object> executeSaga(@RequestBody ExecuteRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String traceId = UUID.randomUUID().toString().substring(0, 8);

            // 실패 확률 설정
            if (request.failureRates() != null) {
                stepFailureRates.putAll(request.failureRates());
            }

            // 비동기 실행
            CompletableFuture<SagaExecution> future = orchestrator.executeAsync(
                    request.sagaName(),
                    request.input(),
                    traceId
            );

            // 진행 상황 추적
            String executionId = null;

            // 짧은 대기 후 실행 ID 획득
            try {
                SagaExecution execution = future.get(500, TimeUnit.MILLISECONDS);
                executionId = execution.getId();
            } catch (TimeoutException e) {
                // 아직 실행 중 - ID는 나중에 가져옴
            }

            result.put("success", true);
            result.put("traceId", traceId);
            result.put("message", "Saga execution started");

            // 완료 대기 및 결과 반환
            try {
                SagaExecution finalExecution = future.get(30, TimeUnit.SECONDS);
                result.put("executionId", finalExecution.getId());
                result.put("execution", executionToMap(finalExecution));
            } catch (TimeoutException e) {
                result.put("timeout", true);
                result.put("message", "Saga execution timed out");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #37 Step별 진행 상태 조회 =====
    @GetMapping("/execution/{executionId}")
    @ResponseBody
    public Map<String, Object> getExecution(@PathVariable String executionId) {
        SagaExecution execution = orchestrator.getExecution(executionId);
        if (execution == null) {
            return Map.of("found", false, "error", "Execution not found");
        }
        return executionToMap(execution);
    }

    // ===== #38 보상 트랜잭션 시뮬레이션 (실패 확률 설정) =====
    @PostMapping("/failure-rate")
    @ResponseBody
    public Map<String, Object> setFailureRate(@RequestBody FailureRateRequest request) {
        stepFailureRates.put(request.stepName(), request.failureRate());
        return Map.of("success", true, "stepName", request.stepName(), "failureRate", request.failureRate());
    }

    @GetMapping("/failure-rates")
    @ResponseBody
    public Map<String, Object> getFailureRates() {
        return Map.of("failureRates", new HashMap<>(stepFailureRates));
    }

    @DeleteMapping("/failure-rates")
    @ResponseBody
    public Map<String, Object> resetFailureRates() {
        stepFailureRates.clear();
        return Map.of("success", true, "message", "Failure rates reset");
    }

    // ===== #39 Saga 실행 이력 조회 =====
    @GetMapping("/executions")
    @ResponseBody
    public Map<String, Object> getExecutions(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();

        List<SagaExecution> executions = sagaRepository.findAll();
        List<Map<String, Object>> executionList = executions.stream()
                .sorted(Comparator.comparing(SagaExecution::getStartedAt).reversed())
                .limit(limit)
                .map(this::executionToMap)
                .toList();

        result.put("executions", executionList);
        result.put("total", executions.size());

        return result;
    }

    @GetMapping("/executions/by-status/{status}")
    @ResponseBody
    public Map<String, Object> getExecutionsByStatus(@PathVariable String status) {
        Map<String, Object> result = new HashMap<>();

        SagaStatus sagaStatus = SagaStatus.valueOf(status.toUpperCase());
        List<SagaExecution> executions = sagaRepository.findByStatus(sagaStatus);

        result.put("status", status);
        result.put("executions", executions.stream().map(this::executionToMap).toList());
        result.put("count", executions.size());

        return result;
    }

    // ===== #40 실패 Saga 재시도 =====
    @PostMapping("/retry/{executionId}")
    @ResponseBody
    public Map<String, Object> retrySaga(@PathVariable String executionId) {
        Map<String, Object> result = new HashMap<>();

        SagaExecution execution = sagaRepository.findById(executionId).orElse(null);
        if (execution == null) {
            result.put("success", false);
            result.put("error", "Execution not found");
            return result;
        }

        if (execution.getStatus() != SagaStatus.COMPENSATED && execution.getStatus() != SagaStatus.FAILED) {
            result.put("success", false);
            result.put("error", "Can only retry failed or compensated sagas");
            return result;
        }

        // 새로운 실행 시작
        try {
            CompletableFuture<SagaExecution> future = orchestrator.executeAsync(
                    execution.getSagaName(),
                    execution.getContext().get("input"),
                    execution.getTraceId() + "-retry"
            );

            SagaExecution newExecution = future.get(30, TimeUnit.SECONDS);

            result.put("success", true);
            result.put("originalExecutionId", executionId);
            result.put("newExecutionId", newExecution.getId());
            result.put("execution", executionToMap(newExecution));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #41 Saga 타임아웃 처리 =====
    @PostMapping("/execute-with-timeout")
    @ResponseBody
    public Map<String, Object> executeWithTimeout(@RequestBody TimeoutRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            CompletableFuture<SagaExecution> future = orchestrator.executeAsync(
                    request.sagaName(),
                    request.input(),
                    "timeout-test-" + System.currentTimeMillis()
            );

            SagaExecution execution = future.get(request.timeout(), TimeUnit.MILLISECONDS);
            result.put("success", true);
            result.put("execution", executionToMap(execution));

        } catch (TimeoutException e) {
            result.put("success", false);
            result.put("timeout", true);
            result.put("error", "Saga execution timed out after " + request.timeout() + "ms");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #42 분산 환경 시뮬레이션 =====
    @PostMapping("/distributed-simulation")
    @ResponseBody
    public Map<String, Object> distributedSimulation(@RequestBody DistributedRequest request) {
        Map<String, Object> result = new HashMap<>();

        List<CompletableFuture<SagaExecution>> futures = new ArrayList<>();
        List<String> executionIds = new ArrayList<>();

        // 여러 Saga 동시 실행
        for (int i = 0; i < request.count(); i++) {
            final int index = i;
            CompletableFuture<SagaExecution> future = orchestrator.executeAsync(
                    request.sagaName(),
                    Map.of("orderId", "ORD-" + (1000 + i), "amount", 100 + i * 10),
                    "dist-" + System.currentTimeMillis() + "-" + i
            );
            futures.add(future);
        }

        // 모든 실행 완료 대기
        List<Map<String, Object>> results = new ArrayList<>();
        int completed = 0, failed = 0, compensated = 0;

        for (int i = 0; i < futures.size(); i++) {
            try {
                SagaExecution execution = futures.get(i).get(60, TimeUnit.SECONDS);
                Map<String, Object> execResult = new HashMap<>();
                execResult.put("index", i);
                execResult.put("executionId", execution.getId());
                execResult.put("status", execution.getStatus().name());

                switch (execution.getStatus()) {
                    case COMPLETED -> completed++;
                    case FAILED -> failed++;
                    case COMPENSATED -> compensated++;
                }

                results.add(execResult);
            } catch (Exception e) {
                results.add(Map.of("index", i, "error", e.getMessage()));
                failed++;
            }
        }

        result.put("success", true);
        result.put("totalCount", request.count());
        result.put("completed", completed);
        result.put("failed", failed);
        result.put("compensated", compensated);
        result.put("results", results);

        return result;
    }

    // ===== Saga Step Methods =====
    public String stepValidateOrder(SagaContext context) throws Exception {
        simulateStep("validateOrder", 300);
        log.info("Step: Validate Order - {}", (Object) context.getInput());
        return "Order validated";
    }

    public void compensateValidateOrder(SagaContext context) {
        log.info("Compensate: Validate Order cancelled");
    }

    public String stepReserveInventory(SagaContext context) throws Exception {
        simulateStep("reserveInventory", 500);
        log.info("Step: Reserve Inventory");
        return "Inventory reserved";
    }

    public void compensateReserveInventory(SagaContext context) {
        log.info("Compensate: Inventory released");
    }

    public String stepProcessPayment(SagaContext context) throws Exception {
        simulateStep("processPayment", 700);
        log.info("Step: Process Payment");
        return "Payment processed";
    }

    public void compensateProcessPayment(SagaContext context) {
        log.info("Compensate: Payment refunded");
    }

    public String stepCreateShipment(SagaContext context) throws Exception {
        simulateStep("createShipment", 400);
        log.info("Step: Create Shipment");
        return "Shipment created";
    }

    public void compensateCreateShipment(SagaContext context) {
        log.info("Compensate: Shipment cancelled");
    }

    public String stepSendNotification(SagaContext context) throws Exception {
        simulateStep("sendNotification", 200);
        log.info("Step: Send Notification");
        return "Notification sent";
    }

    public String stepValidateCard(SagaContext context) throws Exception {
        simulateStep("validateCard", 300);
        return "Card validated";
    }

    public void compensateValidateCard(SagaContext context) {
        log.info("Compensate: Card validation reverted");
    }

    public String stepAuthorizePayment(SagaContext context) throws Exception {
        simulateStep("authorizePayment", 500);
        return "Payment authorized";
    }

    public void compensateAuthorizePayment(SagaContext context) {
        log.info("Compensate: Authorization voided");
    }

    public String stepCapturePayment(SagaContext context) throws Exception {
        simulateStep("capturePayment", 400);
        return "Payment captured";
    }

    public void compensateCapturePayment(SagaContext context) {
        log.info("Compensate: Capture reversed");
    }

    public String stepPickItems(SagaContext context) throws Exception {
        simulateStep("pickItems", 300);
        return "Items picked";
    }

    public void compensatePickItems(SagaContext context) {
        log.info("Compensate: Items returned to shelf");
    }

    public String stepPackItems(SagaContext context) throws Exception {
        simulateStep("packItems", 400);
        return "Items packed";
    }

    public void compensatePackItems(SagaContext context) {
        log.info("Compensate: Package unpacked");
    }

    public String stepShipPackage(SagaContext context) throws Exception {
        simulateStep("shipPackage", 500);
        return "Package shipped";
    }

    public void compensateShipPackage(SagaContext context) {
        log.info("Compensate: Shipment recalled");
    }

    private void simulateStep(String stepName, long baseDelayMs) throws Exception {
        Thread.sleep(baseDelayMs + (long)(Math.random() * 200));

        Integer failureRate = stepFailureRates.get(stepName);
        if (failureRate != null && Math.random() * 100 < failureRate) {
            throw new RuntimeException("Simulated failure in " + stepName);
        }
    }

    // ===== Helper Methods =====
    private Map<String, Object> definitionToMap(SagaDefinition def) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", def.getName());
        map.put("description", def.getDescription());
        map.put("timeout", def.getTimeout());
        map.put("maxRetries", def.getMaxRetries());
        map.put("totalSteps", def.getTotalSteps());

        List<Map<String, Object>> steps = def.getSteps().stream()
                .map(s -> Map.of(
                        "order", (Object)s.getOrder(),
                        "name", s.getName(),
                        "hasCompensation", s.hasCompensation(),
                        "retries", s.getRetries(),
                        "retryDelay", s.getRetryDelay()
                ))
                .toList();
        map.put("steps", steps);

        return map;
    }

    private Map<String, Object> executionToMap(SagaExecution exec) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", exec.getId());
        map.put("sagaName", exec.getSagaName());
        map.put("traceId", exec.getTraceId());
        map.put("status", exec.getStatus().name());
        map.put("currentStep", exec.getCurrentStep());
        map.put("startedAt", exec.getStartedAt() != null ? exec.getStartedAt().toString() : null);
        map.put("completedAt", exec.getCompletedAt() != null ? exec.getCompletedAt().toString() : null);
        map.put("failureReason", exec.getFailureReason());
        map.put("retryCount", exec.getRetryCount());

        List<Map<String, Object>> steps = exec.getSteps().stream()
                .map(s -> {
                    Map<String, Object> stepMap = new HashMap<>();
                    stepMap.put("order", s.getOrder());
                    stepMap.put("name", s.getName());
                    stepMap.put("status", s.getStatus().name());
                    stepMap.put("output", s.getOutput());
                    stepMap.put("errorMessage", s.getErrorMessage());
                    stepMap.put("retryCount", s.getRetryCount());
                    stepMap.put("startedAt", s.getStartedAt() != null ? s.getStartedAt().toString() : null);
                    stepMap.put("completedAt", s.getCompletedAt() != null ? s.getCompletedAt().toString() : null);
                    return stepMap;
                })
                .toList();
        map.put("steps", steps);

        return map;
    }

    // ===== Request DTOs =====
    record ExecuteRequest(String sagaName, Map<String, Object> input, Map<String, Integer> failureRates) {}
    record FailureRateRequest(String stepName, int failureRate) {}
    record TimeoutRequest(String sagaName, Map<String, Object> input, long timeout) {}
    record DistributedRequest(String sagaName, int count) {}
    record SagaExecutionProgress(String executionId, int currentStep, int totalSteps, String status) {}
}
