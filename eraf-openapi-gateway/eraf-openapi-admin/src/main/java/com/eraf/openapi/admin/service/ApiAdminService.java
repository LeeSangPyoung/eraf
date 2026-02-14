package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.ApiRequest;
import com.eraf.openapi.admin.dto.ApiResponse;
import com.eraf.openapi.admin.mapper.ApiMapper;
import com.eraf.openapi.core.domain.GatewayApi;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.repository.GatewayApiRepository;
import com.eraf.openapi.core.repository.GatewayRouteRepository;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import com.eraf.exception.BusinessException;
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
 * API 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiAdminService {

    private final GatewayApiRepository apiRepository;
    private final GatewayServiceRepository serviceRepository;
    private final GatewayRouteRepository routeRepository;
    private final ApiMapper apiMapper;

    /**
     * 모든 API 조회
     */
    public List<ApiResponse> findAll() {
        return apiRepository.findAll().stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 API 조회
     */
    public List<ApiResponse> findAllEnabled() {
        return apiRepository.findByEnabledTrueOrderByPriorityAsc().stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 API 조회
     */
    public ApiResponse findById(Long id) {
        GatewayApi api = apiRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "API " + id));
        return apiMapper.toResponse(api);
    }

    /**
     * Service ID로 API 조회
     */
    public List<ApiResponse> findByServiceId(Long serviceId) {
        return apiRepository.findByServiceId(serviceId).stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Route ID로 API 조회
     */
    public List<ApiResponse> findByRouteId(Long routeId) {
        return apiRepository.findByRouteId(routeId).stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * API 생성
     */
    @Transactional
    public ApiResponse create(ApiRequest request) {
        log.info("Creating API: {} {} (service={})", request.getMethod(), request.getPath(), request.getServiceId());

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Route 존재 확인 (선택사항)
        GatewayRoute route = null;
        if (request.getRouteId() != null) {
            route = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + request.getRouteId()));
        }

        // 중복 확인 (path + method + service 조합은 unique)
        if (apiRepository.existsByPathAndMethodAndService(
                request.getPath(),
                request.getMethod(),
                request.getServiceId(),
                null)) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE,
                    String.format("API %s %s (service=%s)",
                            request.getMethod(), request.getPath(), request.getServiceId()));
        }

        // Name 중복 확인
        if (apiRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "API name " + request.getName());
        }

        // API 생성 및 저장
        GatewayApi api = apiMapper.toEntity(request, service, route);
        GatewayApi saved = apiRepository.save(api);

        log.info("API created: {} (id={})", saved.getName(), saved.getId());
        return apiMapper.toResponse(saved);
    }

    /**
     * API 수정
     */
    @Transactional
    public ApiResponse update(Long id, ApiRequest request) {
        log.info("Updating API: {} (id={})", request.getName(), id);

        // API 존재 확인
        GatewayApi api = apiRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "API " + id));

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Route 존재 확인 (선택사항)
        GatewayRoute route = null;
        if (request.getRouteId() != null) {
            route = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + request.getRouteId()));
        }

        // 중복 확인 (자신 제외)
        if (apiRepository.existsByPathAndMethodAndService(
                request.getPath(),
                request.getMethod(),
                request.getServiceId(),
                id)) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE,
                    String.format("API %s %s (service=%s)",
                            request.getMethod(), request.getPath(), request.getServiceId()));
        }

        // Name 중복 확인 (자신 제외)
        if (!api.getName().equals(request.getName()) && apiRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "API name " + request.getName());
        }

        // API 업데이트
        apiMapper.updateEntity(api, request, service, route);
        GatewayApi updated = apiRepository.save(api);

        log.info("API updated: {} (id={})", updated.getName(), updated.getId());
        return apiMapper.toResponse(updated);
    }

    /**
     * API 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting API: id={}", id);

        if (!apiRepository.existsById(id)) {
            throw new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "API " + id);
        }

        apiRepository.deleteById(id);
        log.info("API deleted: id={}", id);
    }

    /**
     * API 활성화/비활성화
     */
    @Transactional
    public ApiResponse toggleEnabled(Long id) {
        log.info("Toggling API enabled status: id={}", id);

        GatewayApi api = apiRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "API " + id));

        api.setEnabled(!api.getEnabled());
        GatewayApi updated = apiRepository.save(api);

        log.info("API enabled status toggled: {} -> {}", id, updated.getEnabled());
        return apiMapper.toResponse(updated);
    }

    /**
     * 경로와 메서드로 API 조회 (정확한 매칭)
     */
    public ApiResponse findByPathAndMethod(String path, String method) {
        return apiRepository.findByPathAndMethod(path, method)
                .map(apiMapper::toResponse)
                .orElse(null);
    }

    /**
     * API 통계 정보 조회
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApis", apiRepository.count());
        stats.put("enabledApis", apiRepository.countByEnabledTrue());
        stats.put("withAuth", apiRepository.countByAuthRequiredTrue());
        stats.put("withValidation", apiRepository.countByValidationEnabledTrue());
        stats.put("withRateLimit", apiRepository.countByRateLimitEnabledTrue());
        stats.put("withCache", apiRepository.countByCacheEnabledTrue());
        return stats;
    }
}
