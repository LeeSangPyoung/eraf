package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.RouteRequest;
import com.eraf.openapi.admin.dto.RouteResponse;
import com.eraf.openapi.admin.mapper.RouteMapper;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.repository.GatewayRouteRepository;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import com.eraf.core.exception.BusinessException;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Route 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteAdminService {

    private final GatewayRouteRepository routeRepository;
    private final GatewayServiceRepository serviceRepository;
    private final RouteMapper routeMapper;

    /**
     * 모든 Route 조회
     */
    public List<RouteResponse> findAll() {
        return routeRepository.findAll().stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Route 조회
     */
    public List<RouteResponse> findAllEnabled() {
        return routeRepository.findByEnabledTrueOrderByPriorityAsc().stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Route 조회
     */
    public RouteResponse findById(Long id) {
        GatewayRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + id));
        return routeMapper.toResponse(route);
    }

    /**
     * Service ID로 Route 조회
     */
    public List<RouteResponse> findByServiceId(Long serviceId) {
        return routeRepository.findByServiceId(serviceId).stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Route 생성
     */
    @Transactional
    public RouteResponse create(RouteRequest request) {
        log.info("Creating route: {}", request.getName());

        // Route 이름 중복 확인
        if (routeRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Route name " + request.getName());
        }

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Route 생성 및 저장
        GatewayRoute route = routeMapper.toEntity(request, service);
        GatewayRoute saved = routeRepository.save(route);

        log.info("Route created: {} (id={})", saved.getName(), saved.getId());
        return routeMapper.toResponse(saved);
    }

    /**
     * Route 수정
     */
    @Transactional
    public RouteResponse update(Long id, RouteRequest request) {
        log.info("Updating route: {} (id={})", request.getName(), id);

        // Route 존재 확인
        GatewayRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + id));

        // Route 이름 중복 확인 (본인 제외)
        routeRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Route name " + request.getName());
                    }
                });

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Route 업데이트
        routeMapper.updateEntity(route, request, service);
        GatewayRoute updated = routeRepository.save(route);

        log.info("Route updated: {} (id={})", updated.getName(), updated.getId());
        return routeMapper.toResponse(updated);
    }

    /**
     * Route 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting route: id={}", id);

        if (!routeRepository.existsById(id)) {
            throw new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + id);
        }

        routeRepository.deleteById(id);
        log.info("Route deleted: id={}", id);
    }

    /**
     * Route 활성화/비활성화
     */
    @Transactional
    public RouteResponse toggleEnabled(Long id) {
        log.info("Toggling route enabled status: id={}", id);

        GatewayRoute route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + id));

        route.setEnabled(!route.getEnabled());
        GatewayRoute updated = routeRepository.save(route);

        log.info("Route enabled status toggled: {} -> {}", id, updated.getEnabled());
        return routeMapper.toResponse(updated);
    }

    /**
     * Route 통계
     */
    public Map<String, Object> getStats() {
        long total = routeRepository.count();
        long enabled = routeRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", total);
        stats.put("enabledRoutes", enabled);
        return stats;
    }
}
