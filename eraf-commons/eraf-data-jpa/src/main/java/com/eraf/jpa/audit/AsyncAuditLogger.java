package com.eraf.jpa.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 비동기 감사 로거
 * 메인 트랜잭션에 영향을 주지 않고 감사 로그를 저장
 */
@Component
public class AsyncAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AsyncAuditLogger.class);

    private final AuditLogJpaRepository auditLogRepository;

    public AsyncAuditLogger(AuditLogJpaRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 감사 로그를 비동기로 저장
     * 새로운 트랜잭션에서 실행되어 메인 트랜잭션 실패에 영향받지 않음
     *
     * @param auditLog 감사 로그 엔티티
     * @return CompletableFuture (저장 완료 시 ID 반환)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Long> logAsync(AuditLogEntity auditLog) {
        try {
            if (auditLog.getTimestamp() == null) {
                auditLog.setTimestamp(Instant.now());
            }

            AuditLogEntity saved = auditLogRepository.save(auditLog);
            log.trace("Audit log saved asynchronously: id={}, action={}, resource={}",
                    saved.getId(), saved.getAction(), saved.getResource());

            return CompletableFuture.completedFuture(saved.getId());
        } catch (Exception e) {
            log.error("Failed to save audit log asynchronously: action={}, resource={}, error={}",
                    auditLog.getAction(), auditLog.getResource(), e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 여러 감사 로그를 비동기 배치로 저장
     *
     * @param auditLogs 감사 로그 리스트
     * @return CompletableFuture (저장 완료 시 저장된 개수 반환)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Integer> logBatchAsync(List<AuditLogEntity> auditLogs) {
        try {
            Instant now = Instant.now();
            auditLogs.forEach(log -> {
                if (log.getTimestamp() == null) {
                    log.setTimestamp(now);
                }
            });

            List<AuditLogEntity> saved = auditLogRepository.saveAll(auditLogs);
            log.debug("Audit logs saved asynchronously in batch: count={}", saved.size());

            return CompletableFuture.completedFuture(saved.size());
        } catch (Exception e) {
            log.error("Failed to save audit logs in batch asynchronously: count={}, error={}",
                    auditLogs.size(), e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 표준 성공 로그를 비동기로 저장
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Long> logSuccess(String action, String resource, String resourceId,
                                               String userId, String username, String clientIp) {
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(userId)
                .username(username)
                .clientIp(clientIp)
                .build();

        return logAsync(auditLog);
    }

    /**
     * 표준 실패 로그를 비동기로 저장
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Long> logFailure(String action, String resource, String resourceId,
                                               String userId, String username, String clientIp,
                                               String errorDetails) {
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(AuditEventStandard.Result.FAILURE)
                .details(errorDetails)
                .userId(userId)
                .username(username)
                .clientIp(clientIp)
                .build();

        return logAsync(auditLog);
    }

    /**
     * 접근 거부 로그를 비동기로 저장
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Long> logDenied(String action, String resource, String resourceId,
                                              String userId, String username, String clientIp) {
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .result(AuditEventStandard.Result.DENIED)
                .userId(userId)
                .username(username)
                .clientIp(clientIp)
                .build();

        return logAsync(auditLog);
    }

    /**
     * Fire-and-forget 방식의 감사 로그 저장
     * 결과를 기다리지 않고 비동기로 저장만 함
     */
    public void logAndForget(AuditLogEntity auditLog) {
        logAsync(auditLog)
                .exceptionally(throwable -> {
                    log.warn("Fire-and-forget audit log failed: {}", throwable.getMessage());
                    return null;
                });
    }

    /**
     * Fire-and-forget 방식의 성공 로그
     */
    public void logSuccessAndForget(String action, String resource, String resourceId,
                                     String userId, String username, String clientIp) {
        logSuccess(action, resource, resourceId, userId, username, clientIp)
                .exceptionally(throwable -> {
                    log.warn("Fire-and-forget success audit log failed: {}", throwable.getMessage());
                    return null;
                });
    }

    /**
     * Fire-and-forget 방식의 실패 로그
     */
    public void logFailureAndForget(String action, String resource, String resourceId,
                                     String userId, String username, String clientIp,
                                     String errorDetails) {
        logFailure(action, resource, resourceId, userId, username, clientIp, errorDetails)
                .exceptionally(throwable -> {
                    log.warn("Fire-and-forget failure audit log failed: {}", throwable.getMessage());
                    return null;
                });
    }
}
