package com.eraf.starter.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * ERAF 작업 실행 이력
 */
public class ErafJobHistory {

    private final Map<String, Deque<ExecutionRecord>> historyMap = new LinkedHashMap<>();
    private final int maxHistoryPerJob;

    public ErafJobHistory() {
        this(100);
    }

    public ErafJobHistory(int maxHistoryPerJob) {
        this.maxHistoryPerJob = maxHistoryPerJob;
    }

    /**
     * 실행 시작 기록
     */
    public String recordStart(String jobName) {
        String executionId = UUID.randomUUID().toString();

        ExecutionRecord record = new ExecutionRecord();
        record.setExecutionId(executionId);
        record.setJobName(jobName);
        record.setStartTime(Instant.now());
        record.setStatus(ExecutionStatus.RUNNING);

        addRecord(jobName, record);
        return executionId;
    }

    /**
     * 실행 완료 기록
     */
    public void recordSuccess(String executionId, String result) {
        findRecord(executionId).ifPresent(record -> {
            record.setEndTime(Instant.now());
            record.setStatus(ExecutionStatus.SUCCESS);
            record.setResult(result);
            record.setDuration(Duration.between(record.getStartTime(), record.getEndTime()));
        });
    }

    /**
     * 실행 실패 기록
     */
    public void recordFailure(String executionId, Throwable error) {
        findRecord(executionId).ifPresent(record -> {
            record.setEndTime(Instant.now());
            record.setStatus(ExecutionStatus.FAILED);
            record.setErrorMessage(error.getMessage());
            record.setErrorClass(error.getClass().getName());
            record.setDuration(Duration.between(record.getStartTime(), record.getEndTime()));
        });
    }

    /**
     * 작업별 실행 이력 조회
     */
    public List<ExecutionRecord> getHistory(String jobName) {
        Deque<ExecutionRecord> history = historyMap.get(jobName);
        return history != null ? new ArrayList<>(history) : List.of();
    }

    /**
     * 작업별 최근 실행 이력 조회
     */
    public List<ExecutionRecord> getRecentHistory(String jobName, int count) {
        return getHistory(jobName).stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 작업별 마지막 실행 기록 조회
     */
    public Optional<ExecutionRecord> getLastExecution(String jobName) {
        Deque<ExecutionRecord> history = historyMap.get(jobName);
        return history != null && !history.isEmpty()
                ? Optional.of(history.peekFirst())
                : Optional.empty();
    }

    /**
     * 전체 실행 이력 조회
     */
    public List<ExecutionRecord> getAllHistory() {
        return historyMap.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ExecutionRecord::getStartTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 이력 정리
     */
    public void clearHistory(String jobName) {
        historyMap.remove(jobName);
    }

    private void addRecord(String jobName, ExecutionRecord record) {
        historyMap.computeIfAbsent(jobName, k -> new ConcurrentLinkedDeque<>());
        Deque<ExecutionRecord> history = historyMap.get(jobName);
        history.addFirst(record);

        // 최대 이력 수 유지
        while (history.size() > maxHistoryPerJob) {
            history.removeLast();
        }
    }

    private Optional<ExecutionRecord> findRecord(String executionId) {
        return historyMap.values().stream()
                .flatMap(Collection::stream)
                .filter(r -> executionId.equals(r.getExecutionId()))
                .findFirst();
    }

    /**
     * 실행 상태
     */
    public enum ExecutionStatus {
        RUNNING, SUCCESS, FAILED, SKIPPED
    }

    /**
     * 실행 기록
     */
    public static class ExecutionRecord {
        private String executionId;
        private String jobName;
        private Instant startTime;
        private Instant endTime;
        private Duration duration;
        private ExecutionStatus status;
        private String result;
        private String errorMessage;
        private String errorClass;

        // Getters and Setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }

        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }

        public Instant getStartTime() { return startTime; }
        public void setStartTime(Instant startTime) { this.startTime = startTime; }

        public Instant getEndTime() { return endTime; }
        public void setEndTime(Instant endTime) { this.endTime = endTime; }

        public Duration getDuration() { return duration; }
        public void setDuration(Duration duration) { this.duration = duration; }

        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public String getErrorClass() { return errorClass; }
        public void setErrorClass(String errorClass) { this.errorClass = errorClass; }
    }
}
