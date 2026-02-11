package com.eraf.openapi.health;

import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.domain.GatewayTarget;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import com.eraf.openapi.core.repository.GatewayTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Health Check 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final GatewayServiceRepository serviceRepository;
    private final GatewayTargetRepository targetRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * 모든 활성화된 Target에 대해 Health Check 수행
     */
    @Transactional
    public void performHealthChecks() {
        log.debug("Starting health checks for all enabled targets");

        List<GatewayTarget> targets = targetRepository.findByEnabledTrue();
        log.info("Performing health checks on {} targets", targets.size());

        for (GatewayTarget target : targets) {
            try {
                performHealthCheck(target);
            } catch (Exception e) {
                log.error("Failed to perform health check for target {}: {}", target.getId(), e.getMessage());
            }
        }
    }

    /**
     * 특정 Target에 대해 Health Check 수행
     */
    @Transactional
    public HealthCheckResult performHealthCheck(Long targetId) {
        GatewayTarget target = targetRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target not found: " + targetId));

        return performHealthCheck(target);
    }

    /**
     * Target Health Check 실제 수행
     */
    private HealthCheckResult performHealthCheck(GatewayTarget target) {
        GatewayService service = target.getService();

        if (service == null || !service.getHealthCheckEnabled()) {
            log.debug("Health check disabled for target {}", target.getId());
            return buildResult(target, "UNKNOWN", null, "Health check disabled");
        }

        String healthCheckPath = service.getHealthCheckPath();
        if (healthCheckPath == null || healthCheckPath.isEmpty()) {
            healthCheckPath = "/actuator/health";
        }

        String url = String.format("%s://%s:%d%s",
                service.getProtocol(),
                target.getHost(),
                target.getPort(),
                healthCheckPath);

        log.debug("Performing health check on target {} at {}", target.getId(), url);

        long startTime = System.currentTimeMillis();
        String healthStatus;
        String errorMessage = null;

        try {
            WebClient webClient = webClientBuilder.build();

            Boolean isHealthy = webClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(service.getHealthCheckTimeout()))
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .onErrorResume(e -> {
                        log.debug("Health check failed for target {}: {}", target.getId(), e.getMessage());
                        return Mono.just(false);
                    })
                    .block();

            if (Boolean.TRUE.equals(isHealthy)) {
                healthStatus = "HEALTHY";
                target.setConsecutiveFailures(0);
                log.debug("Target {} is HEALTHY", target.getId());
            } else {
                healthStatus = "UNHEALTHY";
                target.setConsecutiveFailures(target.getConsecutiveFailures() + 1);
                errorMessage = "Health check endpoint returned non-2xx status";
                log.warn("Target {} is UNHEALTHY (consecutive failures: {})",
                        target.getId(), target.getConsecutiveFailures());
            }

        } catch (Exception e) {
            healthStatus = "UNHEALTHY";
            target.setConsecutiveFailures(target.getConsecutiveFailures() + 1);
            errorMessage = e.getMessage();
            log.error("Health check exception for target {}: {}", target.getId(), e.getMessage());
        }

        long responseTime = System.currentTimeMillis() - startTime;

        // Target 상태 업데이트
        target.setHealthStatus(healthStatus);
        target.setLastHealthCheckAt(LocalDateTime.now());
        targetRepository.save(target);

        return buildResult(target, healthStatus, responseTime, errorMessage);
    }

    /**
     * Service별 Health Check 결과 조회
     */
    @Transactional(readOnly = true)
    public List<HealthCheckResult> getHealthCheckResultsByService(Long serviceId) {
        List<GatewayTarget> targets = targetRepository.findByServiceId(serviceId);
        return targets.stream()
                .map(target -> buildResult(target, target.getHealthStatus(), null, null))
                .collect(Collectors.toList());
    }

    /**
     * 모든 Target의 Health Check 결과 조회
     */
    @Transactional(readOnly = true)
    public List<HealthCheckResult> getAllHealthCheckResults() {
        List<GatewayTarget> targets = targetRepository.findAll();
        return targets.stream()
                .map(target -> buildResult(target, target.getHealthStatus(), null, null))
                .collect(Collectors.toList());
    }

    /**
     * Health Check 통계
     */
    @Transactional(readOnly = true)
    public HealthCheckStats getStats() {
        List<GatewayTarget> targets = targetRepository.findAll();

        long total = targets.size();
        long healthy = targets.stream()
                .filter(t -> "HEALTHY".equals(t.getHealthStatus()))
                .count();
        long unhealthy = targets.stream()
                .filter(t -> "UNHEALTHY".equals(t.getHealthStatus()))
                .count();
        long unknown = total - healthy - unhealthy;

        double healthyPercentage = total > 0 ? (healthy * 100.0 / total) : 0.0;

        return HealthCheckStats.builder()
                .totalTargets(total)
                .healthyTargets(healthy)
                .unhealthyTargets(unhealthy)
                .unknownTargets(unknown)
                .healthyPercentage(healthyPercentage)
                .build();
    }

    /**
     * HealthCheckResult 빌더
     */
    private HealthCheckResult buildResult(GatewayTarget target, String healthStatus,
                                          Long responseTime, String errorMessage) {
        return HealthCheckResult.builder()
                .targetId(target.getId())
                .serviceName(target.getService() != null ? target.getService().getName() : null)
                .host(target.getHost())
                .port(target.getPort())
                .healthStatus(healthStatus)
                .consecutiveFailures(target.getConsecutiveFailures())
                .lastHealthCheckAt(target.getLastHealthCheckAt())
                .responseTimeMs(responseTime)
                .errorMessage(errorMessage)
                .enabled(target.getEnabled())
                .build();
    }

    /**
     * Health Check 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckStats {
        private Long totalTargets;
        private Long healthyTargets;
        private Long unhealthyTargets;
        private Long unknownTargets;
        private Double healthyPercentage;
    }
}
