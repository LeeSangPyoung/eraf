package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.UpstreamRequest;
import com.eraf.openapi.admin.dto.UpstreamResponse;
import com.eraf.openapi.admin.mapper.UpstreamMapper;
import com.eraf.openapi.core.domain.GatewayUpstream;
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
 * Upstream 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpstreamAdminService {

    private final GatewayUpstreamRepository upstreamRepository;
    private final UpstreamMapper upstreamMapper;

    /**
     * 모든 Upstream 조회
     */
    public List<UpstreamResponse> findAll() {
        return upstreamRepository.findAll().stream()
                .map(upstreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Upstream 조회
     */
    public List<UpstreamResponse> findAllEnabled() {
        return upstreamRepository.findByEnabledTrue().stream()
                .map(upstreamMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Upstream 조회
     */
    public UpstreamResponse findById(Long id) {
        GatewayUpstream upstream = upstreamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + id));
        return upstreamMapper.toResponse(upstream);
    }

    /**
     * Name으로 Upstream 조회
     */
    public UpstreamResponse findByName(String name) {
        GatewayUpstream upstream = upstreamRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + name));
        return upstreamMapper.toResponse(upstream);
    }

    /**
     * Upstream 생성
     */
    @Transactional
    public UpstreamResponse create(UpstreamRequest request) {
        log.info("Creating upstream: {}", request.getName());

        if (upstreamRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Upstream name " + request.getName());
        }

        GatewayUpstream upstream = upstreamMapper.toEntity(request);
        GatewayUpstream saved = upstreamRepository.save(upstream);

        log.info("Upstream created: {} (id={})", saved.getName(), saved.getId());
        return upstreamMapper.toResponse(saved);
    }

    /**
     * Upstream 수정
     */
    @Transactional
    public UpstreamResponse update(Long id, UpstreamRequest request) {
        log.info("Updating upstream: {} (id={})", request.getName(), id);

        GatewayUpstream upstream = upstreamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + id));

        // Name 중복 확인 (본인 제외)
        upstreamRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Upstream name " + request.getName());
                    }
                });

        upstreamMapper.updateEntity(upstream, request);
        GatewayUpstream updated = upstreamRepository.save(upstream);

        log.info("Upstream updated: {} (id={})", updated.getName(), updated.getId());
        return upstreamMapper.toResponse(updated);
    }

    /**
     * Upstream 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting upstream: {}", id);

        GatewayUpstream upstream = upstreamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + id));

        upstreamRepository.delete(upstream);
        log.info("Upstream deleted: {} (id={})", upstream.getName(), id);
    }

    /**
     * Upstream 활성화/비활성화 토글
     */
    @Transactional
    public UpstreamResponse toggleEnabled(Long id) {
        log.info("Toggling upstream: {}", id);

        GatewayUpstream upstream = upstreamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Upstream " + id));

        upstream.setEnabled(!upstream.getEnabled());
        GatewayUpstream updated = upstreamRepository.save(upstream);

        log.info("Upstream toggled: {} (id={}, enabled={})", updated.getName(), updated.getId(), updated.getEnabled());
        return upstreamMapper.toResponse(updated);
    }

    /**
     * Upstream 통계
     */
    public Map<String, Object> getStats() {
        long total = upstreamRepository.count();
        long enabled = upstreamRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUpstreams", total);
        stats.put("enabledUpstreams", enabled);
        stats.put("disabledUpstreams", total - enabled);

        return stats;
    }
}
