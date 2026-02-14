package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.TargetRequest;
import com.eraf.openapi.admin.dto.TargetResponse;
import com.eraf.openapi.admin.mapper.TargetMapper;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.domain.GatewayTarget;
import com.eraf.openapi.core.domain.GatewayUpstream;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import com.eraf.openapi.core.repository.GatewayTargetRepository;
import com.eraf.openapi.core.repository.GatewayUpstreamRepository;
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
 * Target 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TargetAdminService {

    private final GatewayTargetRepository targetRepository;
    private final GatewayServiceRepository serviceRepository;
    private final GatewayUpstreamRepository upstreamRepository;
    private final TargetMapper targetMapper;

    /**
     * 모든 Target 조회
     */
    public List<TargetResponse> findAll() {
        return targetRepository.findAll().stream()
                .map(targetMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Target 조회
     */
    public TargetResponse findById(Long id) {
        GatewayTarget target = targetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Target " + id));
        return targetMapper.toResponse(target);
    }

    /**
     * Service ID로 Target 조회
     */
    public List<TargetResponse> findByServiceId(Long serviceId) {
        return targetRepository.findByServiceId(serviceId).stream()
                .map(targetMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Service ID로 Healthy Target 조회
     */
    public List<TargetResponse> findHealthyTargetsByServiceId(Long serviceId) {
        return targetRepository.findHealthyTargetsByServiceId(serviceId).stream()
                .map(targetMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Target 생성
     */
    @Transactional
    public TargetResponse create(TargetRequest request) {
        log.info("Creating target: {}:{} for service {}", request.getHost(), request.getPort(), request.getServiceId());

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Upstream 존재 확인 (선택사항)
        GatewayUpstream upstream = null;
        if (request.getUpstreamId() != null) {
            upstream = upstreamRepository.findById(request.getUpstreamId())
                    .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + request.getUpstreamId()));
        }

        // Target 생성 및 저장
        GatewayTarget target = targetMapper.toEntity(request, service, upstream);
        GatewayTarget saved = targetRepository.save(target);

        log.info("Target created: {}:{} (id={})", saved.getHost(), saved.getPort(), saved.getId());
        return targetMapper.toResponse(saved);
    }

    /**
     * Target 수정
     */
    @Transactional
    public TargetResponse update(Long id, TargetRequest request) {
        log.info("Updating target: id={}", id);

        // Target 존재 확인
        GatewayTarget target = targetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Target " + id));

        // Service 존재 확인
        GatewayService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));

        // Upstream 존재 확인 (선택사항)
        GatewayUpstream upstream = null;
        if (request.getUpstreamId() != null) {
            upstream = upstreamRepository.findById(request.getUpstreamId())
                    .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + request.getUpstreamId()));
        }

        // Target 업데이트
        targetMapper.updateEntity(target, request, service, upstream);
        GatewayTarget updated = targetRepository.save(target);

        log.info("Target updated: {}:{} (id={})", updated.getHost(), updated.getPort(), updated.getId());
        return targetMapper.toResponse(updated);
    }

    /**
     * Target 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting target: id={}", id);

        if (!targetRepository.existsById(id)) {
            throw new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Target " + id);
        }

        targetRepository.deleteById(id);
        log.info("Target deleted: id={}", id);
    }

    /**
     * Target 활성화/비활성화
     */
    @Transactional
    public TargetResponse toggleEnabled(Long id) {
        log.info("Toggling target enabled status: id={}", id);

        GatewayTarget target = targetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Target " + id));

        target.setEnabled(!target.getEnabled());
        GatewayTarget updated = targetRepository.save(target);

        log.info("Target enabled status toggled: {} -> {}", id, updated.getEnabled());
        return targetMapper.toResponse(updated);
    }

    /**
     * 활성화된 Target 조회
     */
    public List<TargetResponse> findAllEnabled() {
        return targetRepository.findByEnabledTrue().stream()
                .map(targetMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Target 통계
     */
    public Map<String, Object> getStats() {
        long total = targetRepository.count();
        long enabled = targetRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTargets", total);
        stats.put("enabledTargets", enabled);
        return stats;
    }
}
