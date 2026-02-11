package com.eraf.openapi.features.loadbalancer;

import com.eraf.core.crypto.Hash;
import com.eraf.openapi.core.domain.GatewayTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IP Hash Load Balancer
 * 클라이언트 IP를 해시하여 항상 같은 타겟으로 라우팅 (세션 고정)
 *
 * 동일한 클라이언트는 항상 같은 백엔드 서버로 연결되므로
 * 세션 기반 애플리케이션에 적합
 */
@Slf4j
@Component
public class IpHashLoadBalancer implements LoadBalancer {

    @Override
    public String getAlgorithm() {
        return "IP_HASH";
    }

    @Override
    public GatewayTarget select(List<GatewayTarget> targets, String clientIp) {
        // SHA-256 기반 해싱으로 균등한 분포 보장 (eraf-commons Hash 활용)
        String hashHex = Hash.hash(clientIp != null ? clientIp : "unknown");
        int hash = Math.abs(hashHex.hashCode());
        int index = hash % targets.size();

        GatewayTarget selected = targets.get(index);

        log.debug("IP hash selected: {} -> {}:{} (hash={}, index={})",
                clientIp, selected.getHost(), selected.getPort(), hash, index);

        return selected;
    }
}
