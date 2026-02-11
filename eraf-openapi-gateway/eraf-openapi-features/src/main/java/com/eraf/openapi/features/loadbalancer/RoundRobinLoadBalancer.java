package com.eraf.openapi.features.loadbalancer;

import com.eraf.openapi.core.domain.GatewayTarget;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round Robin Load Balancer
 * 순차적으로 타겟을 선택하여 균등하게 분배
 */
@Component
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String getAlgorithm() {
        return "ROUND_ROBIN";
    }

    @Override
    public GatewayTarget select(List<GatewayTarget> targets, String clientIp) {
        int index = Math.abs(counter.getAndIncrement()) % targets.size();
        return targets.get(index);
    }
}
