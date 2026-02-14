package com.eraf.redis.admin;

import com.eraf.response.ApiResponse;
import com.eraf.redis.RedisLockProvider;
import com.eraf.redis.lock.LockStatistics;
import com.eraf.redis.lock.LockWatchdog;
import com.eraf.redis.lock.RedisReadWriteLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 분산 락 모니터링 및 관리 API
 */
@RestController
@RequestMapping("/admin/locks")
@ConditionalOnBean(RedisLockProvider.class)
@ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
public class LockAdminController {

    private final RedisLockProvider lockProvider;
    private final RedisReadWriteLock readWriteLock;

    public LockAdminController(
            RedisLockProvider lockProvider,
            RedisReadWriteLock readWriteLock) {
        this.lockProvider = lockProvider;
        this.readWriteLock = readWriteLock;
    }

    /**
     * 전역 락 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        LockStatistics stats = lockProvider.getStatistics();
        if (stats == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", false)));
        }
        return ResponseEntity.ok(ApiResponse.success(stats.getGlobalStatistics()));
    }

    /**
     * 특정 키 통계 조회
     */
    @GetMapping("/statistics/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getKeyStatistics(@PathVariable String key) {
        LockStatistics stats = lockProvider.getStatistics();
        if (stats == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", false)));
        }
        return ResponseEntity.ok(ApiResponse.success(stats.getKeyStatistics(key)));
    }

    /**
     * 현재 활성 락 목록 조회
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveLocks() {
        LockStatistics stats = lockProvider.getStatistics();
        if (stats == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(stats.getActiveLocks()));
    }

    /**
     * 특정 락 상태 조회
     */
    @GetMapping("/status/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLockStatus(@PathVariable String key) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("key", key);
        status.put("locked", lockProvider.isLocked(key));
        status.put("heldByCurrentThread", lockProvider.isHeldByCurrentThread(key));
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * ReadWrite Lock 상태 조회
     */
    @GetMapping("/rwlock/{key}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRWLockStatus(@PathVariable String key) {
        String info = readWriteLock.getLockInfo(key);
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("key", key);

        if ("NONE".equals(info)) {
            status.put("mode", "NONE");
            status.put("locked", false);
        } else if (info != null && info.startsWith("READ:")) {
            status.put("mode", "READ");
            status.put("readers", Integer.parseInt(info.substring(5)));
            status.put("locked", true);
        } else if (info != null && info.startsWith("WRITE:")) {
            status.put("mode", "WRITE");
            status.put("owner", info.substring(6));
            status.put("locked", true);
        }

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * Watchdog 상태 조회
     */
    @GetMapping("/watchdog")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWatchdogStatus() {
        LockWatchdog watchdog = lockProvider.getWatchdog();
        Map<String, Object> status = new LinkedHashMap<>();

        if (watchdog == null) {
            status.put("enabled", false);
        } else {
            status.put("enabled", true);
            status.put("watchedCount", watchdog.getWatchedCount());
            status.put("watchedKeys", watchdog.getWatchedKeys());
        }

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * 강제 락 해제 (관리 용도)
     */
    @DeleteMapping("/force-unlock/{key}")
    public ResponseEntity<ApiResponse<String>> forceUnlock(@PathVariable String key) {
        lockProvider.unlock(key);
        return ResponseEntity.ok(ApiResponse.success("Lock force-released: " + key));
    }

    /**
     * ReadWrite Lock 강제 해제
     */
    @DeleteMapping("/rwlock/force-unlock/{key}")
    public ResponseEntity<ApiResponse<String>> forceRWUnlock(@PathVariable String key) {
        boolean result = readWriteLock.forceUnlock(key);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("RW lock force-released: " + key));
        }
        return ResponseEntity.ok(ApiResponse.success("RW lock not found: " + key));
    }

    /**
     * 통계 초기화
     */
    @DeleteMapping("/statistics")
    public ResponseEntity<ApiResponse<String>> resetStatistics() {
        LockStatistics stats = lockProvider.getStatistics();
        if (stats != null) {
            stats.reset();
        }
        return ResponseEntity.ok(ApiResponse.success("Lock statistics reset"));
    }

    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        LockStatistics stats = lockProvider.getStatistics();
        LockWatchdog watchdog = lockProvider.getWatchdog();

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("statisticsEnabled", stats != null);
        health.put("watchdogEnabled", watchdog != null);

        if (stats != null) {
            health.put("activeLockCount", stats.getActiveLocks().size());
        }
        if (watchdog != null) {
            health.put("watchedLockCount", watchdog.getWatchedCount());
        }

        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
