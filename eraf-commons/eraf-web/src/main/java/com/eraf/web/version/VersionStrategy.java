package com.eraf.web.version;

/**
 * API 버전 매칭 전략
 */
public enum VersionStrategy {

    /**
     * URI 경로에서 버전 추출
     * 예: /v1/users, /api/v2/products
     */
    URI,

    /**
     * HTTP 헤더에서 버전 추출
     * 예: X-API-Version: 1, API-Version: 2.0
     */
    HEADER,

    /**
     * 쿼리 파라미터에서 버전 추출
     * 예: ?version=1, ?api-version=2
     */
    PARAM,

    /**
     * Accept 헤더의 미디어 타입에서 버전 추출
     * 예: Accept: application/vnd.myapi.v1+json
     */
    MEDIA_TYPE
}
