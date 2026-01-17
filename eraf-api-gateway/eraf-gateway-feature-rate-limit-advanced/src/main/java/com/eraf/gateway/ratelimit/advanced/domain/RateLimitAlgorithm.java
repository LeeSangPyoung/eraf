package com.eraf.gateway.ratelimit.advanced.domain;

/**
 * Rate Limiting 알고리즘 타입
 * Kong Gateway와 유사한 다양한 알고리즘 지원
 */
public enum RateLimitAlgorithm {

    /**
     * Token Bucket (토큰 버킷)
     * - Kong의 기본 알고리즘
     * - 토큰이 일정 속도로 버킷에 추가됨
     * - 버스트 트래픽 허용 (버킷이 가득 찬 경우)
     * - 장점: 버스트 허용, 유연함
     * - 단점: 메모리 사용량이 약간 높음
     */
    TOKEN_BUCKET,

    /**
     * Leaky Bucket (리키 버킷)
     * - 일정한 속도로 요청 처리
     * - 버스트 트래픽을 평탄화
     * - 장점: 일정한 처리율 보장
     * - 단점: 버스트 트래픽에 덜 유연
     */
    LEAKY_BUCKET,

    /**
     * Sliding Window (슬라이딩 윈도우)
     * - 시간 윈도우가 요청마다 이동
     * - 정확한 요청 수 제한
     * - 장점: 정확한 제한, 버스트 방지
     * - 단점: 메모리 사용량 높음
     */
    SLIDING_WINDOW,

    /**
     * Fixed Window (고정 윈도우)
     * - 고정된 시간 윈도우 사용
     * - 간단하고 효율적
     * - 장점: 낮은 메모리 사용, 빠름
     * - 단점: 윈도우 경계에서 버스트 가능
     */
    FIXED_WINDOW
}
