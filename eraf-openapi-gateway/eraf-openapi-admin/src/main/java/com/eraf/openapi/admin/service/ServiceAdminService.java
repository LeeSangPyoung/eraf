package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.ServiceRequest;
import com.eraf.openapi.admin.dto.ServiceResponse;
import com.eraf.openapi.admin.mapper.ServiceMapper;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.repository.GatewayApiRepository;
import com.eraf.openapi.core.repository.GatewayRouteRepository;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import com.eraf.openapi.core.repository.GatewayTargetRepository;
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
 * Service 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceAdminService {

    private final GatewayServiceRepository serviceRepository;
    private final GatewayRouteRepository routeRepository;
    private final GatewayTargetRepository targetRepository;
    private final GatewayApiRepository apiRepository;
    private final ServiceMapper serviceMapper;

    /**
     * 모든 Service 조회
     */
    public List<ServiceResponse> findAll() {
        return serviceRepository.findAll().stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Service 조회
     */
    public List<ServiceResponse> findAllEnabled() {
        return serviceRepository.findByEnabledTrue().stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Service 조회
     */
    public ServiceResponse findById(Long id) {
        GatewayService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + id));
        return serviceMapper.toResponse(service);
    }

    /**
     * 이름으로 Service 조회
     */
    public ServiceResponse findByName(String name) {
        GatewayService service = serviceRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + name));
        return serviceMapper.toResponse(service);
    }

    /**
     * Service 생성
     */
    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        log.info("Creating service: {}", request.getName());

        // 이름 중복 확인
        if (serviceRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Service name " + request.getName());
        }

        // Service 생성 및 저장
        GatewayService service = serviceMapper.toEntity(request);
        GatewayService saved = serviceRepository.save(service);

        log.info("Service created: {} (id={})", saved.getName(), saved.getId());
        return serviceMapper.toResponse(saved);
    }

    /**
     * Service 수정
     */
    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        log.info("Updating service: {} (id={})", request.getName(), id);

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + id));

        // 이름 중복 확인 (본인 제외)
        serviceRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Service name " + request.getName());
                    }
                });

        // Service 업데이트
        serviceMapper.updateEntity(service, request);
        GatewayService updated = serviceRepository.save(service);

        log.info("Service updated: {} (id={})", updated.getName(), updated.getId());
        return serviceMapper.toResponse(updated);
    }

    /**
     * Service 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting service: id={}", id);

        GatewayService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + id));

        // FK 의존성 체크
        if (!routeRepository.findByServiceId(id).isEmpty()) {
            throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED,
                    "Cannot delete service '" + service.getName() + "': has dependent routes");
        }
        if (!targetRepository.findByServiceId(id).isEmpty()) {
            throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED,
                    "Cannot delete service '" + service.getName() + "': has dependent targets");
        }
        if (!apiRepository.findByServiceId(id).isEmpty()) {
            throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED,
                    "Cannot delete service '" + service.getName() + "': has dependent APIs");
        }

        serviceRepository.delete(service);
        log.info("Service deleted: {} (id={})", service.getName(), id);
    }

    /**
     * Service 활성화/비활성화
     */
    @Transactional
    public ServiceResponse toggleEnabled(Long id) {
        log.info("Toggling service enabled status: id={}", id);

        GatewayService service = serviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + id));

        service.setEnabled(!service.getEnabled());
        GatewayService updated = serviceRepository.save(service);

        log.info("Service enabled status toggled: {} -> {}", id, updated.getEnabled());
        return serviceMapper.toResponse(updated);
    }

    /**
     * Service 통계
     */
    public Map<String, Object> getStats() {
        long total = serviceRepository.count();
        long enabled = serviceRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServices", total);
        stats.put("enabledServices", enabled);
        return stats;
    }
}
