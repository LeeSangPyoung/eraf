# ERAF Scheduler

분산 환경을 지원하는 스케줄 작업 관리 라이브러리

## 개요

ERAF Scheduler는 Spring Scheduling과 ShedLock을 통합하여 분산 환경에서 안전하게 스케줄 작업을 실행할 수 있는 기능을 제공합니다.

### 주요 기능

- **선언적 스케줄 정의**: `@ErafScheduled` 어노테이션 기반
- **분산 락**: ShedLock 기반 클러스터 환경에서 중복 실행 방지
- **다양한 스케줄 타입**: Cron, Fixed Rate, Fixed Delay 지원
- **작업 레지스트리**: 실행 중인 작업 관리 및 모니터링
- **실행 이력 추적**: 작업 실행 결과 및 시간 기록
- **동적 활성화**: SpEL 표현식으로 조건부 작업 실행
- **Redis/JDBC 락**: Redis 또는 JDBC 기반 분산 락 지원
- **작업 그룹화**: 관련 작업을 그룹으로 관리

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-scheduler</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Redis 사용 시 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- JDBC 사용 시 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

## 빠른 시작

### 1. 기본 스케줄 작업

```java
import com.eraf.scheduler.ErafScheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyJobs {

    /**
     * 매일 오전 3시 실행
     */
    @ErafScheduled(
        name = "cleanup-temp-files",
        cron = "0 0 3 * * ?",
        description = "임시 파일 정리"
    )
    public void cleanupTempFiles() {
        log.info("Cleaning up temporary files...");
        fileService.cleanupTempFiles();
    }

    /**
     * 매 5분마다 실행
     */
    @ErafScheduled(
        name = "sync-data",
        cron = "0 */5 * * * ?",
        description = "데이터 동기화",
        group = "sync"
    )
    public void syncData() {
        log.info("Syncing data from external system...");
        dataService.syncFromExternal();
    }

    /**
     * 10초마다 실행 (Fixed Rate)
     */
    @ErafScheduled(
        name = "health-check",
        fixedRate = 10000,
        description = "헬스 체크",
        lockEnabled = false  // 분산 락 비활성화
    )
    public void healthCheck() {
        healthService.check();
    }

    /**
     * 이전 작업 완료 후 15초 대기 (Fixed Delay)
     */
    @ErafScheduled(
        name = "process-queue",
        fixedDelay = 15000,
        initialDelay = 5000,
        description = "큐 처리"
    )
    public void processQueue() {
        queueService.process();
    }
}
```

### 2. 분산 락 설정

```java
@Component
public class ClusterJobs {

    /**
     * 클러스터 환경에서 하나의 인스턴스만 실행
     */
    @ErafScheduled(
        name = "generate-report",
        cron = "0 0 1 * * ?",
        description = "일일 리포트 생성",
        lockEnabled = true,           // 분산 락 활성화
        lockAtMostFor = "PT10M",      // 최대 10분 동안 락 유지
        lockAtLeastFor = "PT1M"       // 최소 1분 동안 락 유지
    )
    public void generateDailyReport() {
        log.info("Generating daily report (only one instance in cluster)...");
        reportService.generateDailyReport();
    }

    /**
     * 장시간 실행 작업
     */
    @ErafScheduled(
        name = "batch-processing",
        cron = "0 0 2 * * ?",
        description = "배치 처리",
        lockAtMostFor = "PT2H"  // 최대 2시간
    )
    public void batchProcessing() {
        log.info("Starting batch processing...");
        batchService.process();
    }
}
```

### 3. 조건부 활성화

```java
@Component
public class ConditionalJobs {

    /**
     * 프로퍼티 기반 활성화
     */
    @ErafScheduled(
        name = "backup-database",
        cron = "0 0 4 * * ?",
        description = "데이터베이스 백업",
        enabled = "${scheduler.backup.enabled:true}"
    )
    public void backupDatabase() {
        backupService.backup();
    }

    /**
     * SpEL 표현식으로 조건부 활성화
     */
    @ErafScheduled(
        name = "premium-user-notification",
        cron = "0 0 9 * * ?",
        description = "프리미엄 사용자 알림",
        enabled = "#{environment.getProperty('spring.profiles.active') == 'production'}"
    )
    public void sendPremiumNotifications() {
        notificationService.sendToPremiumUsers();
    }
}
```

### 4. 작업 레지스트리 활용

```java
import com.eraf.scheduler.ErafJobRegistry;
import com.eraf.scheduler.ErafJobInfo;
import org.springframework.stereotype.Service;

@Service
public class SchedulerManagementService {

    private final ErafJobRegistry jobRegistry;

    public SchedulerManagementService(ErafJobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public List<JobSummary> getAllJobs() {
        return jobRegistry.getAllJobs().stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    public Optional<ErafJobInfo> getJobInfo(String jobName) {
        return jobRegistry.getJob(jobName);
    }

    public List<ErafJobInfo> getJobsByGroup(String group) {
        return jobRegistry.getJobsByGroup(group).stream()
            .collect(Collectors.toList());
    }

    private JobSummary toSummary(ErafJobInfo job) {
        return new JobSummary(
            job.getName(),
            job.getGroup(),
            job.getDescription(),
            job.getStatus(),
            job.getLastExecutionTime(),
            job.getNextExecutionTime()
        );
    }
}
```

### 5. REST API로 작업 모니터링

```java
@RestController
@RequestMapping("/admin/scheduler")
public class SchedulerController {

    private final ErafJobRegistry jobRegistry;
    private final ErafJobHistory jobHistory;

    @GetMapping("/jobs")
    public List<JobInfo> getAllJobs() {
        return jobRegistry.getAllJobs().stream()
            .map(this::toJobInfo)
            .collect(Collectors.toList());
    }

    @GetMapping("/jobs/{name}")
    public ResponseEntity<JobDetail> getJob(@PathVariable String name) {
        return jobRegistry.getJob(name)
            .map(job -> {
                List<ExecutionRecord> history = jobHistory.getHistory(name);
                return new JobDetail(job, history);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/groups/{group}")
    public List<JobInfo> getJobsByGroup(@PathVariable String group) {
        return jobRegistry.getJobsByGroup(group).stream()
            .map(this::toJobInfo)
            .collect(Collectors.toList());
    }

    private JobInfo toJobInfo(ErafJobInfo job) {
        return new JobInfo(
            job.getName(),
            job.getGroup(),
            job.getDescription(),
            job.getCron(),
            job.getStatus().name(),
            job.getLastExecutionTime(),
            job.getLastExecutionResult()
        );
    }
}
```

## 설정

### application.yml

```yaml
eraf:
  scheduler:
    # 스케줄러 활성화 (기본: true)
    enabled: true

    # 스레드 풀 크기
    pool-size: 10

    # 분산 락 활성화 (기본: true)
    distributed-lock-enabled: true

    # ShedLock 테이블 이름 (JDBC 사용 시)
    table-name: shedlock

    # 작업 이력 보관 개수 (작업당)
    max-history-per-job: 100
```

### Redis 락 사용

```yaml
eraf:
  scheduler:
    distributed-lock-enabled: true

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### JDBC 락 사용

```yaml
eraf:
  scheduler:
    distributed-lock-enabled: true
    table-name: shedlock

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
```

#### ShedLock 테이블 생성 (JDBC)

```sql
-- PostgreSQL
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

-- MySQL
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

## 어노테이션

### @ErafScheduled

메서드에 적용하여 스케줄 작업을 정의합니다.

```java
@ErafScheduled(
    name = "job-name",                  // 작업 이름 (필수, 고유 식별자)

    // 스케줄 타입 (하나만 선택)
    cron = "0 0 3 * * ?",              // Cron 표현식
    fixedRate = 60000,                  // 고정 간격 (ms)
    fixedDelay = 30000,                 // 고정 지연 (ms)

    initialDelay = 0,                   // 초기 지연 시간 (ms)

    // 분산 락 설정
    lockEnabled = true,                 // 분산 락 활성화 (기본: true)
    lockAtMostFor = "PT5M",            // 최대 락 유지 시간 (ISO-8601)
    lockAtLeastFor = "PT30S",          // 최소 락 유지 시간 (ISO-8601)

    group = "default",                  // 작업 그룹
    description = "",                   // 작업 설명
    enabled = "true"                    // 활성화 여부 (SpEL 지원)
)
```

## Cron 표현식

### 형식

```
초 분 시 일 월 요일
```

### 예제

```java
// 매일 오전 3시
cron = "0 0 3 * * ?"

// 매 5분마다
cron = "0 */5 * * * ?"

// 평일 오전 9시
cron = "0 0 9 ? * MON-FRI"

// 매월 1일 자정
cron = "0 0 0 1 * ?"

// 매주 월요일 오전 10시
cron = "0 0 10 ? * MON"

// 30초마다
cron = "*/30 * * * * ?"
```

## 락 타임 형식 (ISO-8601 Duration)

```java
// 30초
lockAtLeastFor = "PT30S"

// 1분
lockAtMostFor = "PT1M"

// 5분
lockAtMostFor = "PT5M"

// 10분
lockAtMostFor = "PT10M"

// 1시간
lockAtMostFor = "PT1H"

// 2시간
lockAtMostFor = "PT2H"

// 1일
lockAtMostFor = "P1D"
```

## 스케줄 타입 비교

### Cron

특정 시간에 실행

```java
@ErafScheduled(
    name = "daily-report",
    cron = "0 0 3 * * ?"  // 매일 오전 3시
)
public void generateDailyReport() {
    // 정확한 시간에 실행
}
```

### Fixed Rate

고정된 간격으로 실행 (이전 작업 종료 시간 무관)

```java
@ErafScheduled(
    name = "health-check",
    fixedRate = 10000  // 10초마다 실행
)
public void healthCheck() {
    // 이전 작업이 완료되지 않아도 10초 후 다시 실행
}
```

### Fixed Delay

고정된 지연 후 실행 (이전 작업 완료 후 대기)

```java
@ErafScheduled(
    name = "process-queue",
    fixedDelay = 15000  // 이전 작업 완료 후 15초 대기
)
public void processQueue() {
    // 이전 작업이 완료된 후 15초 대기하고 실행
}
```

## 실전 예제

### 데이터베이스 백업

```java
@Component
public class BackupJobs {

    private final BackupService backupService;

    @ErafScheduled(
        name = "daily-backup",
        cron = "0 0 2 * * ?",
        description = "매일 오전 2시 데이터베이스 백업",
        lockEnabled = true,
        lockAtMostFor = "PT2H",
        enabled = "${backup.enabled:true}"
    )
    public void dailyBackup() {
        try {
            log.info("Starting daily backup...");
            backupService.backupDatabase();
            log.info("Daily backup completed successfully");
        } catch (Exception e) {
            log.error("Daily backup failed", e);
            alertService.sendBackupFailureAlert(e);
        }
    }
}
```

### 데이터 동기화

```java
@Component
public class SyncJobs {

    @ErafScheduled(
        name = "sync-user-data",
        fixedRate = 300000,  // 5분마다
        description = "사용자 데이터 동기화",
        group = "sync",
        lockEnabled = true,
        lockAtMostFor = "PT10M"
    )
    public void syncUserData() {
        int synced = userSyncService.syncFromLegacySystem();
        log.info("Synced {} users", synced);
    }

    @ErafScheduled(
        name = "sync-product-data",
        cron = "0 */10 * * * ?",
        description = "상품 데이터 동기화",
        group = "sync",
        lockEnabled = true
    )
    public void syncProductData() {
        productSyncService.syncFromExternalApi();
    }
}
```

### 정리 작업

```java
@Component
public class CleanupJobs {

    @ErafScheduled(
        name = "cleanup-expired-sessions",
        cron = "0 0 4 * * ?",
        description = "만료된 세션 정리",
        lockEnabled = true
    )
    public void cleanupExpiredSessions() {
        int deleted = sessionService.deleteExpired();
        log.info("Deleted {} expired sessions", deleted);
    }

    @ErafScheduled(
        name = "cleanup-old-logs",
        cron = "0 0 5 * * ?",
        description = "오래된 로그 정리",
        lockEnabled = true,
        lockAtMostFor = "PT1H"
    )
    public void cleanupOldLogs() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        logService.deleteBefore(cutoffDate);
    }

    @ErafScheduled(
        name = "cleanup-temp-files",
        fixedDelay = 3600000,  // 1시간마다
        initialDelay = 300000,  // 시작 후 5분 대기
        description = "임시 파일 정리",
        lockEnabled = false  // 각 인스턴스가 독립적으로 정리
    )
    public void cleanupTempFiles() {
        fileService.cleanupTempDirectory();
    }
}
```

### 알림 발송

```java
@Component
public class NotificationJobs {

    @ErafScheduled(
        name = "send-daily-summary",
        cron = "0 0 9 * * ?",
        description = "일일 요약 이메일 발송",
        group = "notification",
        lockEnabled = true,
        enabled = "${notification.daily-summary.enabled:true}"
    )
    public void sendDailySummary() {
        List<User> subscribers = userService.findDailySummarySubscribers();
        for (User user : subscribers) {
            emailService.sendDailySummary(user);
        }
        log.info("Sent daily summary to {} users", subscribers.size());
    }

    @ErafScheduled(
        name = "send-weekly-report",
        cron = "0 0 10 ? * MON",
        description = "주간 리포트 발송 (매주 월요일)",
        group = "notification",
        lockEnabled = true
    )
    public void sendWeeklyReport() {
        reportService.generateAndSendWeeklyReport();
    }
}
```

### 모니터링 및 헬스 체크

```java
@Component
public class MonitoringJobs {

    @ErafScheduled(
        name = "check-system-health",
        fixedRate = 30000,  // 30초마다
        description = "시스템 헬스 체크",
        lockEnabled = false  // 각 인스턴스가 독립적으로 체크
    )
    public void checkSystemHealth() {
        HealthStatus status = healthService.check();
        if (status.hasIssues()) {
            log.warn("Health check found issues: {}", status.getIssues());
        }
    }

    @ErafScheduled(
        name = "collect-metrics",
        fixedRate = 60000,  // 1분마다
        description = "메트릭 수집",
        lockEnabled = false
    )
    public void collectMetrics() {
        metricsService.collect();
    }
}
```

## 분산 환경에서의 동작

### lockEnabled = true (기본)

```java
@ErafScheduled(
    name = "send-report",
    cron = "0 0 9 * * ?",
    lockEnabled = true
)
public void sendReport() {
    // 클러스터 환경에서 하나의 인스턴스만 실행
    // ShedLock이 분산 락을 획득한 인스턴스만 실행
}
```

- 여러 서버 인스턴스 중 **하나만** 실행
- 락을 획득한 인스턴스가 작업 수행
- 중복 실행 방지

### lockEnabled = false

```java
@ErafScheduled(
    name = "health-check",
    fixedRate = 10000,
    lockEnabled = false
)
public void healthCheck() {
    // 모든 인스턴스에서 독립적으로 실행
}
```

- **모든 인스턴스**에서 실행
- 각 서버의 로컬 작업에 적합

## 작업 이력 조회

```java
import com.eraf.scheduler.ErafJobHistory;

@Service
public class JobMonitoringService {

    private final ErafJobHistory jobHistory;

    public List<ExecutionRecord> getRecentExecutions(String jobName) {
        return jobHistory.getHistory(jobName);
    }

    public ExecutionRecord getLastExecution(String jobName) {
        List<ExecutionRecord> history = jobHistory.getHistory(jobName);
        return history.isEmpty() ? null : history.get(0);
    }

    public Map<String, Object> getJobStatistics(String jobName) {
        List<ExecutionRecord> history = jobHistory.getHistory(jobName);

        long successCount = history.stream()
            .filter(r -> "SUCCESS".equals(r.getResult()))
            .count();

        long failureCount = history.stream()
            .filter(r -> "FAILURE".equals(r.getResult()))
            .count();

        return Map.of(
            "totalExecutions", history.size(),
            "successCount", successCount,
            "failureCount", failureCount,
            "successRate", history.isEmpty() ? 0 : (double) successCount / history.size()
        );
    }
}
```

## 모범 사례

### 1. 적절한 락 타임 설정

```java
// 짧은 작업 (수초)
@ErafScheduled(
    name = "quick-task",
    cron = "* * * * * ?",
    lockAtMostFor = "PT10S",
    lockAtLeastFor = "PT5S"
)

// 중간 작업 (수분)
@ErafScheduled(
    name = "medium-task",
    cron = "0 */5 * * * ?",
    lockAtMostFor = "PT10M",
    lockAtLeastFor = "PT1M"
)

// 긴 작업 (수십분~수시간)
@ErafScheduled(
    name = "long-task",
    cron = "0 0 2 * * ?",
    lockAtMostFor = "PT2H",
    lockAtLeastFor = "PT10M"
)
```

### 2. 예외 처리

```java
@ErafScheduled(name = "data-processing", cron = "0 0 3 * * ?")
public void processData() {
    try {
        dataService.process();
    } catch (Exception e) {
        log.error("Data processing failed", e);
        alertService.sendAlert("Data processing failed: " + e.getMessage());
        // 예외를 던지지 않으면 다음 스케줄에 다시 실행됨
    }
}
```

### 3. 멱등성 보장

```java
@ErafScheduled(name = "send-notifications", cron = "0 0 9 * * ?")
public void sendNotifications() {
    // 이미 발송된 알림은 스킵 (멱등성)
    List<Notification> pending = notificationService
        .findPendingForToday();

    for (Notification notification : pending) {
        notificationService.sendAndMarkAsSent(notification);
    }
}
```

### 4. 작업 그룹화

```java
// 관련 작업을 그룹으로 관리
@ErafScheduled(name = "sync-users", cron = "0 */5 * * * ?", group = "sync")
@ErafScheduled(name = "sync-products", cron = "0 */10 * * * ?", group = "sync")
@ErafScheduled(name = "sync-orders", cron = "0 */15 * * * ?", group = "sync")

// 그룹별 조회 가능
List<ErafJobInfo> syncJobs = jobRegistry.getJobsByGroup("sync");
```

### 5. 환경별 활성화

```java
// 프로덕션에서만 실행
@ErafScheduled(
    name = "production-only-job",
    cron = "0 0 3 * * ?",
    enabled = "#{environment.getProperty('spring.profiles.active') == 'production'}"
)

// 설정 기반 활성화
@ErafScheduled(
    name = "optional-job",
    cron = "0 0 4 * * ?",
    enabled = "${jobs.backup.enabled:false}"
)
```

## 제약사항

- 스케줄 작업 Bean은 `@Component` 등으로 등록되어야 함
- 메서드는 파라미터가 없어야 함 (또는 Spring이 주입할 수 있는 파라미터만 가능)
- `lockEnabled = true`인 경우 Redis 또는 JDBC 설정 필요
- Cron, fixedRate, fixedDelay 중 하나만 사용 가능

## 참고 자료

- [Spring Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [ShedLock](https://github.com/lukas-krecan/ShedLock)
- [Cron Expression Generator](https://crontab.guru/)
- ERAF Platform 개발 로드맵: `/docs/eraf-platform-roadmap.xlsx`

## 라이선스

Copyright 2024 ERAF Platform
