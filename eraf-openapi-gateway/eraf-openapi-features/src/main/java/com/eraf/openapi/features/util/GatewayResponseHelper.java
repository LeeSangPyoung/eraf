package com.eraf.openapi.features.util;

import com.eraf.core.converter.JsonConverter;
import com.eraf.core.exception.ErrorCode;
import com.eraf.core.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway 필터에서 에러 응답을 반환하기 위한 공통 유틸리티
 * eraf-commons의 ApiResponse + JsonConverter를 활용
 */
public final class GatewayResponseHelper {

    private GatewayResponseHelper() {
    }

    /**
     * ErrorCode 기반 에러 응답 반환
     */
    public static Mono<Void> sendError(ServerWebExchange exchange, ErrorCode errorCode) {
        return sendError(exchange, errorCode, errorCode.getMessage());
    }

    /**
     * ErrorCode 기반 에러 응답 반환 (커스텀 메시지)
     */
    public static Mono<Void> sendError(ServerWebExchange exchange, ErrorCode errorCode, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorCode.getStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> response = ApiResponse.error(errorCode, message);
        byte[] bytes = JsonConverter.toJson(response).getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}
