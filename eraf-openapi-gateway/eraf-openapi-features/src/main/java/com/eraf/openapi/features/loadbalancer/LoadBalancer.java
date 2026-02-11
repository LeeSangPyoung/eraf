package com.eraf.openapi.features.loadbalancer;

import com.eraf.openapi.core.domain.GatewayTarget;

import java.util.List;

/**
 * Load Balancer Interface
 * 다양한 로드밸런싱 전략 구현을 위한 인터페이스
 */
public interface LoadBalancer {

    /**
     * 알고리즘 이름 반환
     */
    String getAlgorithm();

    /**
     * 타겟 리스트에서 하나를 선택
     *
     * @param targets   건강한 타겟 목록
     * @param clientIp  클라이언트 IP (IP_HASH에서 사용)
     * @return 선택된 타겟
     */
    GatewayTarget select(List<GatewayTarget> targets, String clientIp);
}
