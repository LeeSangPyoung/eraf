package com.eraf.openapi.features.loadbalancer;

import com.eraf.openapi.core.domain.GatewayTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Least Connections Load Balancer
 * 현재 활성 연결이 가장 적은 타겟을 선택
 *
 * 인메모리 카운터로 활성 연결 수를 추적하며,
 * 요청 시작 시 increment, 완료 시 decrement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeastConnectionsLoadBalancer implements LoadBalancer {

    /**
     * Target별 활성 연결 수 추적
     * Key: "host:port"
     */
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    @Override
    public String getAlgorithm() {
        return "LEAST_CONNECTIONS";
    }

    @Override
    public GatewayTarget select(List<GatewayTarget> targets, String clientIp) {
        // 가장 연결 수가 적은 타겟 선택
        GatewayTarget selected = targets.stream()
                .min(Comparator.comparingInt(t -> getConnectionCount(t)))
                .orElse(targets.get(0));

        // 연결 수 증가
        incrementConnection(selected);

        log.debug("Least connections selected: {}:{} (active: {})",
                selected.getHost(), selected.getPort(), getConnectionCount(selected));

        return selected;
    }

    /**
     * 연결 수 증가 (요청 시작)
     */
    public void incrementConnection(GatewayTarget target) {
        String key = target.getHost() + ":" + target.getPort();
        connectionCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * 연결 수 감소 (요청 완료)
     */
    public void decrementConnection(GatewayTarget target) {
        String key = target.getHost() + ":" + target.getPort();
        AtomicInteger count = connectionCounts.get(key);
        if (count != null && count.get() > 0) {
            count.decrementAndGet();
        }
    }

    /**
     * 연결 수 감소 (host:port 문자열)
     */
    public void decrementConnection(String hostPort) {
        AtomicInteger count = connectionCounts.get(hostPort);
        if (count != null && count.get() > 0) {
            count.decrementAndGet();
        }
    }

    /**
     * 현재 연결 수 조회
     */
    public int getConnectionCount(GatewayTarget target) {
        String key = target.getHost() + ":" + target.getPort();
        AtomicInteger count = connectionCounts.get(key);
        return count != null ? count.get() : 0;
    }

    /**
     * 모든 연결 수 조회 (모니터링용)
     */
    public Map<String, Integer> getAllConnectionCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        connectionCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
