package com.eraf.saga.web;

import com.eraf.saga.core.SagaOrchestrator;
import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.recovery.SagaRecoveryService;
import com.eraf.saga.repository.SagaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Saga 모니터링 및 관리 REST API
 */
@RestController
@RequestMapping("${eraf.saga.api-path:/api/saga}")
@ConditionalOnProperty(name = "eraf.saga.api-enabled", havingValue = "true", matchIfMissing = true)
public class SagaController {

    private final SagaOrchestrator orchestrator;
    private final SagaRepository repository;
    private final SagaRecoveryService recoveryService;

    public SagaController(SagaOrchestrator orchestrator,
                          SagaRepository repository,
                          SagaRecoveryService recoveryService) {
        this.orchestrator = orchestrator;
        this.repository = repository;
        this.recoveryService = recoveryService;
    }

    /**
     * Saga 실행 상태 조회
     */
    @GetMapping("/executions/{id}")
    public ResponseEntity<SagaExecutionDto> getExecution(@PathVariable String id) {
        return repository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * TraceId로 Saga 실행 목록 조회
     */
    @GetMapping("/executions")
    public ResponseEntity<List<SagaExecutionDto>> getExecutions(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String sagaName,
            @RequestParam(required = false) SagaStatus status) {

        List<SagaExecution> executions;

        if (traceId != null) {
            executions = repository.findByTraceId(traceId);
        } else if (sagaName != null && status != null) {
            executions = repository.findBySagaNameAndStatus(sagaName, status);
        } else if (status != null) {
            executions = repository.findByStatus(status);
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<SagaExecutionDto> dtos = executions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * 등록된 Saga 정의 목록 조회
     */
    @GetMapping("/definitions")
    public ResponseEntity<List<Map<String, Object>>> getDefinitions() {
        List<Map<String, Object>> definitions = orchestrator.getDefinitions().stream()
                .map(def -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", def.getName());
                    info.put("description", def.getDescription());
                    info.put("totalSteps", def.getTotalSteps());
                    info.put("maxRetries", def.getMaxRetries());
                    info.put("timeout", def.getTimeout());
                    info.put("steps", def.getSteps().stream()
                            .map(step -> {
                                Map<String, Object> stepInfo = new HashMap<>();
                                stepInfo.put("order", step.getOrder());
                                stepInfo.put("name", step.getName());
                                stepInfo.put("hasCompensation", step.hasCompensation());
                                return stepInfo;
                            })
                            .collect(Collectors.toList()));
                    return info;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(definitions);
    }

    /**
     * Saga 상태 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        for (SagaStatus status : SagaStatus.values()) {
            List<SagaExecution> executions = repository.findByStatus(status);
            stats.put(status.name().toLowerCase() + "Count", executions.size());
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * 실패한 Saga 재시도
     */
    @PostMapping("/executions/{id}/retry")
    public ResponseEntity<SagaExecutionDto> retry(@PathVariable String id) {
        try {
            SagaExecution execution = recoveryService.retry(id);
            return ResponseEntity.ok(toDto(execution));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Saga 복구
     */
    @PostMapping("/executions/{id}/recover")
    public ResponseEntity<SagaExecutionDto> recover(@PathVariable String id) {
        try {
            SagaExecution execution = recoveryService.recover(id);
            return ResponseEntity.ok(toDto(execution));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Saga 취소
     */
    @PostMapping("/executions/{id}/cancel")
    public ResponseEntity<SagaExecutionDto> cancel(@PathVariable String id) {
        try {
            SagaExecution execution = recoveryService.cancel(id);
            return ResponseEntity.ok(toDto(execution));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private SagaExecutionDto toDto(SagaExecution execution) {
        SagaExecutionDto dto = new SagaExecutionDto();
        dto.setId(execution.getId());
        dto.setSagaName(execution.getSagaName());
        dto.setTraceId(execution.getTraceId());
        dto.setStatus(execution.getStatus());
        dto.setCurrentStep(execution.getCurrentStep());
        dto.setTotalSteps(execution.getSteps().size());
        dto.setStartedAt(execution.getStartedAt() != null ? execution.getStartedAt().toString() : null);
        dto.setCompletedAt(execution.getCompletedAt() != null ? execution.getCompletedAt().toString() : null);
        dto.setFailureReason(execution.getFailureReason());
        dto.setRetryCount(execution.getRetryCount());

        // Step 요약
        dto.setSteps(execution.getSteps().stream()
                .map(step -> {
                    Map<String, Object> stepInfo = new HashMap<>();
                    stepInfo.put("order", step.getOrder());
                    stepInfo.put("name", step.getName());
                    stepInfo.put("status", step.getStatus());
                    stepInfo.put("retryCount", step.getRetryCount());
                    if (step.getErrorMessage() != null) {
                        stepInfo.put("error", step.getErrorMessage());
                    }
                    return stepInfo;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Saga 실행 DTO
     */
    public static class SagaExecutionDto {
        private String id;
        private String sagaName;
        private String traceId;
        private SagaStatus status;
        private int currentStep;
        private int totalSteps;
        private String startedAt;
        private String completedAt;
        private String failureReason;
        private int retryCount;
        private List<Map<String, Object>> steps;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSagaName() { return sagaName; }
        public void setSagaName(String sagaName) { this.sagaName = sagaName; }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public SagaStatus getStatus() { return status; }
        public void setStatus(SagaStatus status) { this.status = status; }
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        public int getTotalSteps() { return totalSteps; }
        public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
        public String getStartedAt() { return startedAt; }
        public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
        public String getCompletedAt() { return completedAt; }
        public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
        public List<Map<String, Object>> getSteps() { return steps; }
        public void setSteps(List<Map<String, Object>> steps) { this.steps = steps; }
    }
}
