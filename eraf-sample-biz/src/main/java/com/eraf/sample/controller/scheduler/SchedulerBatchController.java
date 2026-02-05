package com.eraf.sample.controller.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 스케줄러/배치 컨트롤러
 * #56 스케줄러 작업 목록, #57 스케줄러 수동 실행
 * #58 배치 Job 목록, #59 배치 Job 실행/모니터링
 */
@Controller
@RequestMapping("/scheduler")
public class SchedulerBatchController {

    private static final Logger log = LoggerFactory.getLogger(SchedulerBatchController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // In-memory stores for demo
    private final Map<String, SchedulerJob> schedulerJobs = new ConcurrentHashMap<>();
    private final Map<String, BatchJob> batchJobs = new ConcurrentHashMap<>();
    private final List<JobExecution> jobExecutions = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void init() {
        // Register sample scheduler jobs
        registerSchedulerJob("daily-report", "report", "0 0 6 * * *", "Daily report generation at 6 AM");
        registerSchedulerJob("hourly-sync", "sync", "0 0 * * * *", "Hourly data synchronization");
        registerSchedulerJob("cache-cleanup", "maintenance", "0 30 2 * * *", "Cache cleanup at 2:30 AM");
        registerSchedulerJob("health-check", "monitoring", "0 */5 * * * *", "Health check every 5 minutes");
        registerSchedulerJob("data-backup", "backup", "0 0 3 * * SUN", "Weekly backup on Sunday at 3 AM");

        // Register sample batch jobs
        registerBatchJob("user-export", "Export all users to CSV", 3);
        registerBatchJob("order-process", "Process pending orders", 5);
        registerBatchJob("report-generate", "Generate monthly reports", 4);
        registerBatchJob("data-migration", "Migrate data to new schema", 10);
        registerBatchJob("cleanup-old-data", "Clean up data older than 1 year", 2);
    }

    private void registerSchedulerJob(String name, String group, String cron, String description) {
        SchedulerJob job = new SchedulerJob();
        job.setName(name);
        job.setGroup(group);
        job.setCron(cron);
        job.setDescription(description);
        job.setStatus("SCHEDULED");
        job.setEnabled(true);
        job.setLockEnabled(true);
        job.setNextExecution(calculateNextExecution(cron));
        schedulerJobs.put(name, job);
    }

    private void registerBatchJob(String name, String description, int stepCount) {
        BatchJob job = new BatchJob();
        job.setName(name);
        job.setDescription(description);
        job.setStepCount(stepCount);
        job.setChunkSize(100);
        job.setStatus("READY");
        batchJobs.put(name, job);
    }

    private LocalDateTime calculateNextExecution(String cron) {
        // Simplified: just add some random minutes for demo
        return LocalDateTime.now().plusMinutes(new Random().nextInt(60) + 5);
    }

    @GetMapping
    public String schedulerPage() {
        return "scheduler/scheduler";
    }

    // ============ #56 스케줄러 작업 목록 ============

    @GetMapping("/jobs")
    @ResponseBody
    public Map<String, Object> getSchedulerJobs(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String status) {

        List<SchedulerJob> filtered = schedulerJobs.values().stream()
                .filter(j -> group == null || group.isEmpty() || group.equals(j.getGroup()))
                .filter(j -> status == null || status.isEmpty() || status.equals(j.getStatus()))
                .sorted(Comparator.comparing(SchedulerJob::getName))
                .toList();

        Set<String> groups = new TreeSet<>();
        schedulerJobs.values().forEach(j -> groups.add(j.getGroup()));

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", (long) schedulerJobs.size());
        stats.put("scheduled", schedulerJobs.values().stream().filter(j -> "SCHEDULED".equals(j.getStatus())).count());
        stats.put("running", schedulerJobs.values().stream().filter(j -> "RUNNING".equals(j.getStatus())).count());
        stats.put("paused", schedulerJobs.values().stream().filter(j -> "PAUSED".equals(j.getStatus())).count());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats);
        result.put("groups", groups);
        result.put("jobs", filtered);

        return result;
    }

    @GetMapping("/jobs/{name}")
    @ResponseBody
    public Map<String, Object> getSchedulerJob(@PathVariable String name) {
        SchedulerJob job = schedulerJobs.get(name);
        if (job == null) {
            return Map.of("error", "Job not found: " + name);
        }

        // Get execution history
        List<JobExecution> history = jobExecutions.stream()
                .filter(e -> name.equals(e.getJobName()) && "SCHEDULER".equals(e.getJobType()))
                .limit(10)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job", job);
        result.put("history", history);

        return result;
    }

    // ============ #57 스케줄러 수동 실행 ============

    @PostMapping("/jobs/{name}/execute")
    @ResponseBody
    public Map<String, Object> executeSchedulerJob(@PathVariable String name) {
        SchedulerJob job = schedulerJobs.get(name);
        if (job == null) {
            return Map.of("success", false, "error", "Job not found: " + name);
        }

        if ("RUNNING".equals(job.getStatus())) {
            return Map.of("success", false, "error", "Job is already running");
        }

        String executionId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime startTime = LocalDateTime.now();

        job.setStatus("RUNNING");
        job.setLastExecution(startTime);

        log.info("[Scheduler] Executing job: {} (executionId: {})", name, executionId);

        // Simulate async execution
        executor.submit(() -> {
            try {
                // Simulate job execution (1-5 seconds)
                Thread.sleep(1000 + new Random().nextInt(4000));

                job.setStatus("SCHEDULED");
                job.setNextExecution(LocalDateTime.now().plusMinutes(new Random().nextInt(60) + 5));
                job.setExecutionCount(job.getExecutionCount() + 1);

                // Record execution
                JobExecution execution = new JobExecution();
                execution.setExecutionId(executionId);
                execution.setJobName(name);
                execution.setJobType("SCHEDULER");
                execution.setStartTime(startTime);
                execution.setEndTime(LocalDateTime.now());
                execution.setStatus("COMPLETED");
                execution.setDurationMs(Duration.between(startTime, execution.getEndTime()).toMillis());
                jobExecutions.add(0, execution);

                log.info("[Scheduler] Job completed: {} (executionId: {}, duration: {}ms)",
                        name, executionId, execution.getDurationMs());

            } catch (Exception e) {
                job.setStatus("FAILED");
                log.error("[Scheduler] Job failed: {}", name, e);
            }
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("executionId", executionId);
        result.put("jobName", name);
        result.put("startTime", startTime.format(FORMATTER));
        result.put("message", "Job execution started");

        return result;
    }

    @PostMapping("/jobs/{name}/pause")
    @ResponseBody
    public Map<String, Object> pauseSchedulerJob(@PathVariable String name) {
        SchedulerJob job = schedulerJobs.get(name);
        if (job == null) {
            return Map.of("success", false, "error", "Job not found");
        }

        job.setStatus("PAUSED");
        job.setEnabled(false);

        return Map.of("success", true, "jobName", name, "status", "PAUSED");
    }

    @PostMapping("/jobs/{name}/resume")
    @ResponseBody
    public Map<String, Object> resumeSchedulerJob(@PathVariable String name) {
        SchedulerJob job = schedulerJobs.get(name);
        if (job == null) {
            return Map.of("success", false, "error", "Job not found");
        }

        job.setStatus("SCHEDULED");
        job.setEnabled(true);
        job.setNextExecution(LocalDateTime.now().plusMinutes(new Random().nextInt(30) + 5));

        return Map.of("success", true, "jobName", name, "status", "SCHEDULED");
    }

    // ============ #58 배치 Job 목록 ============

    @GetMapping("/batch/jobs")
    @ResponseBody
    public Map<String, Object> getBatchJobs() {
        List<BatchJob> jobs = batchJobs.values().stream()
                .sorted(Comparator.comparing(BatchJob::getName))
                .toList();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", (long) batchJobs.size());
        stats.put("ready", batchJobs.values().stream().filter(j -> "READY".equals(j.getStatus())).count());
        stats.put("running", batchJobs.values().stream().filter(j -> "RUNNING".equals(j.getStatus())).count());
        stats.put("completed", batchJobs.values().stream().filter(j -> "COMPLETED".equals(j.getStatus())).count());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats);
        result.put("jobs", jobs);

        return result;
    }

    @GetMapping("/batch/jobs/{name}")
    @ResponseBody
    public Map<String, Object> getBatchJob(@PathVariable String name) {
        BatchJob job = batchJobs.get(name);
        if (job == null) {
            return Map.of("error", "Batch job not found: " + name);
        }

        // Get execution history
        List<JobExecution> history = jobExecutions.stream()
                .filter(e -> name.equals(e.getJobName()) && "BATCH".equals(e.getJobType()))
                .limit(10)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job", job);
        result.put("history", history);

        return result;
    }

    // ============ #59 배치 Job 실행/모니터링 ============

    @PostMapping("/batch/jobs/{name}/execute")
    @ResponseBody
    public Map<String, Object> executeBatchJob(@PathVariable String name) {
        BatchJob job = batchJobs.get(name);
        if (job == null) {
            return Map.of("success", false, "error", "Batch job not found: " + name);
        }

        if ("RUNNING".equals(job.getStatus())) {
            return Map.of("success", false, "error", "Job is already running");
        }

        String executionId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime startTime = LocalDateTime.now();

        job.setStatus("RUNNING");
        job.setCurrentStep(0);
        job.setProgress(0);
        job.setProcessedItems(0);
        job.setLastExecution(startTime);

        log.info("[Batch] Starting job: {} (executionId: {})", name, executionId);

        // Simulate async batch execution
        executor.submit(() -> {
            try {
                int totalItems = new Random().nextInt(1000) + 100;
                int stepCount = job.getStepCount();

                for (int step = 1; step <= stepCount; step++) {
                    job.setCurrentStep(step);
                    job.setCurrentStepName("Step " + step);

                    // Process items in this step
                    int itemsInStep = totalItems / stepCount;
                    for (int i = 0; i < itemsInStep; i += job.getChunkSize()) {
                        Thread.sleep(100); // Simulate processing
                        job.setProcessedItems(job.getProcessedItems() + Math.min(job.getChunkSize(), itemsInStep - i));
                        job.setProgress((int) ((job.getCurrentStep() - 1) * 100.0 / stepCount + (i + 1) * 100.0 / (stepCount * itemsInStep)));
                    }

                    log.info("[Batch] Job {} - Step {}/{} completed", name, step, stepCount);
                }

                job.setProgress(100);
                job.setStatus("COMPLETED");
                job.setExecutionCount(job.getExecutionCount() + 1);

                // Record execution
                JobExecution execution = new JobExecution();
                execution.setExecutionId(executionId);
                execution.setJobName(name);
                execution.setJobType("BATCH");
                execution.setStartTime(startTime);
                execution.setEndTime(LocalDateTime.now());
                execution.setStatus("COMPLETED");
                execution.setDurationMs(Duration.between(startTime, execution.getEndTime()).toMillis());
                execution.setProcessedItems(job.getProcessedItems());
                jobExecutions.add(0, execution);

                log.info("[Batch] Job completed: {} (executionId: {}, items: {}, duration: {}ms)",
                        name, executionId, job.getProcessedItems(), execution.getDurationMs());

            } catch (Exception e) {
                job.setStatus("FAILED");
                log.error("[Batch] Job failed: {}", name, e);
            }
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("executionId", executionId);
        result.put("jobName", name);
        result.put("startTime", startTime.format(FORMATTER));
        result.put("message", "Batch job execution started");

        return result;
    }

    @GetMapping("/batch/jobs/{name}/progress")
    @ResponseBody
    public Map<String, Object> getBatchJobProgress(@PathVariable String name) {
        BatchJob job = batchJobs.get(name);
        if (job == null) {
            return Map.of("error", "Batch job not found: " + name);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobName", name);
        result.put("status", job.getStatus());
        result.put("progress", job.getProgress());
        result.put("currentStep", job.getCurrentStep());
        result.put("totalSteps", job.getStepCount());
        result.put("currentStepName", job.getCurrentStepName());
        result.put("processedItems", job.getProcessedItems());

        return result;
    }

    @PostMapping("/batch/jobs/{name}/stop")
    @ResponseBody
    public Map<String, Object> stopBatchJob(@PathVariable String name) {
        BatchJob job = batchJobs.get(name);
        if (job == null) {
            return Map.of("success", false, "error", "Batch job not found");
        }

        if (!"RUNNING".equals(job.getStatus())) {
            return Map.of("success", false, "error", "Job is not running");
        }

        job.setStatus("STOPPED");
        log.info("[Batch] Job stopped: {}", name);

        return Map.of("success", true, "jobName", name, "status", "STOPPED");
    }

    @GetMapping("/executions")
    @ResponseBody
    public Map<String, Object> getExecutionHistory(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit) {

        List<JobExecution> filtered = jobExecutions.stream()
                .filter(e -> type == null || type.isEmpty() || type.equals(e.getJobType()))
                .limit(limit)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalExecutions", jobExecutions.size());
        result.put("executions", filtered);

        return result;
    }

    // ============ DTOs ============

    public static class SchedulerJob {
        private String name;
        private String group;
        private String cron;
        private String description;
        private String status;
        private boolean enabled;
        private boolean lockEnabled;
        private LocalDateTime lastExecution;
        private LocalDateTime nextExecution;
        private int executionCount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public String getCron() { return cron; }
        public void setCron(String cron) { this.cron = cron; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isLockEnabled() { return lockEnabled; }
        public void setLockEnabled(boolean lockEnabled) { this.lockEnabled = lockEnabled; }
        public LocalDateTime getLastExecution() { return lastExecution; }
        public void setLastExecution(LocalDateTime lastExecution) { this.lastExecution = lastExecution; }
        public LocalDateTime getNextExecution() { return nextExecution; }
        public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }
        public int getExecutionCount() { return executionCount; }
        public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }
        public String getLastExecutionFormatted() { return lastExecution != null ? lastExecution.format(FORMATTER) : null; }
        public String getNextExecutionFormatted() { return nextExecution != null ? nextExecution.format(FORMATTER) : null; }
    }

    public static class BatchJob {
        private String name;
        private String description;
        private String status;
        private int stepCount;
        private int currentStep;
        private String currentStepName;
        private int chunkSize;
        private int progress;
        private int processedItems;
        private LocalDateTime lastExecution;
        private int executionCount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getStepCount() { return stepCount; }
        public void setStepCount(int stepCount) { this.stepCount = stepCount; }
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        public String getCurrentStepName() { return currentStepName; }
        public void setCurrentStepName(String currentStepName) { this.currentStepName = currentStepName; }
        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public int getProcessedItems() { return processedItems; }
        public void setProcessedItems(int processedItems) { this.processedItems = processedItems; }
        public LocalDateTime getLastExecution() { return lastExecution; }
        public void setLastExecution(LocalDateTime lastExecution) { this.lastExecution = lastExecution; }
        public int getExecutionCount() { return executionCount; }
        public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }
        public String getLastExecutionFormatted() { return lastExecution != null ? lastExecution.format(FORMATTER) : null; }
    }

    public static class JobExecution {
        private String executionId;
        private String jobName;
        private String jobType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private long durationMs;
        private int processedItems;

        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getJobName() { return jobName; }
        public void setJobName(String jobName) { this.jobName = jobName; }
        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public int getProcessedItems() { return processedItems; }
        public void setProcessedItems(int processedItems) { this.processedItems = processedItems; }
        public String getStartTimeFormatted() { return startTime != null ? startTime.format(FORMATTER) : null; }
        public String getEndTimeFormatted() { return endTime != null ? endTime.format(FORMATTER) : null; }
    }
}
