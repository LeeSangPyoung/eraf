package com.eraf.openapi.admin.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.admin.dto.RouteRequest;
import com.eraf.openapi.admin.dto.RouteResponse;
import com.eraf.openapi.admin.service.RouteAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Route 관리 REST API
 */
@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
public class RouteAdminController {

    private final RouteAdminService routeAdminService;

    /**
     * 모든 Route 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getAllRoutes(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<RouteResponse> routes = enabledOnly ?
                routeAdminService.findAllEnabled() :
                routeAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(routes));
    }

    /**
     * Route 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRoute(@PathVariable Long id) {
        RouteResponse route = routeAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(route));
    }

    /**
     * Service ID로 Route 조회
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRoutesByService(@PathVariable Long serviceId) {
        List<RouteResponse> routes = routeAdminService.findByServiceId(serviceId);
        return ResponseEntity.ok(ApiResponse.success(routes));
    }

    /**
     * Route 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse created = routeAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Route 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequest request) {
        RouteResponse updated = routeAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Route 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        routeAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Route 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<RouteResponse>> toggleRouteEnabled(@PathVariable Long id) {
        RouteResponse updated = routeAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Route 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRouteStats() {
        return ResponseEntity.ok(ApiResponse.success(routeAdminService.getStats()));
    }
}
