package com.eraf.openapi.features.loadbalancer;

import com.eraf.openapi.core.domain.GatewayTarget;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted Round Robin Load Balancer
 * Target의 weight 값에 비례하여 트래픽 분배
 *
 * 예: Target A(weight=70), Target B(weight=30)
 * → A에 70%, B에 30% 트래픽
 */
@Component
public class WeightedRoundRobinLoadBalancer implements LoadBalancer {

    @Override
    public String getAlgorithm() {
        return "WEIGHTED_ROUND_ROBIN";
    }

    @Override
    public GatewayTarget select(List<GatewayTarget> targets, String clientIp) {
        int totalWeight = targets.stream()
                .mapToInt(GatewayTarget::getWeight)
                .sum();

        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (GatewayTarget target : targets) {
            currentWeight += target.getWeight();
            if (randomWeight < currentWeight) {
                return target;
            }
        }

        return targets.get(0);
    }
}
