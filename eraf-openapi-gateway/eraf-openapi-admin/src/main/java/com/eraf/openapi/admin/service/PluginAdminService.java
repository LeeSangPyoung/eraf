package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.PluginRequest;
import com.eraf.openapi.admin.dto.PluginResponse;
import com.eraf.openapi.admin.mapper.PluginMapper;
import com.eraf.openapi.core.domain.GatewayConsumerGroup;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.repository.GatewayConsumerGroupRepository;
import com.eraf.openapi.core.repository.GatewayPluginRepository;
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
 * Plugin 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PluginAdminService {

    private final GatewayPluginRepository pluginRepository;
    private final GatewayRouteRepository routeRepository;
    private final GatewayServiceRepository serviceRepository;
    private final GatewayConsumerGroupRepository consumerGroupRepository;
    private final PluginMapper pluginMapper;

    /**
     * 모든 Plugin 조회
     */
    public List<PluginResponse> findAll() {
        return pluginRepository.findAll().stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Plugin 조회
     */
    public PluginResponse findById(Long id) {
        GatewayPlugin plugin = pluginRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Plugin " + id));
        return pluginMapper.toResponse(plugin);
    }

    /**
     * Route ID로 Plugin 조회
     */
    public List<PluginResponse> findByRouteId(Long routeId) {
        return pluginRepository.findByRouteIdAndEnabledTrueOrderByPriority(routeId).stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Service ID로 Plugin 조회
     */
    public List<PluginResponse> findByServiceId(Long serviceId) {
        return pluginRepository.findByServiceIdAndEnabledTrueOrderByPriority(serviceId).stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Global Plugin 조회
     */
    public List<PluginResponse> findGlobalPlugins() {
        return pluginRepository.findGlobalPluginsOrderByPriority().stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Plugin 이름으로 조회
     */
    public List<PluginResponse> findByName(String name) {
        return pluginRepository.findByNameAndEnabledTrue(name).stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Plugin 생성
     */
    @Transactional
    public PluginResponse create(PluginRequest request) {
        log.info("Creating plugin: {} (scope={})", request.getName(), request.getScope());

        // Scope 검증 및 연관 엔티티 조회
        ScopeEntities scope = resolveScopeEntities(request);

        // 동일 scope+entity 내 중복 확인
        if (pluginRepository.existsDuplicatePlugin(
                request.getName(), request.getScope(),
                request.getRouteId(), request.getServiceId(), request.getConsumerGroupId())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE,
                    "Plugin '" + request.getName() + "' already exists in " + request.getScope() + " scope");
        }

        // Plugin 생성 및 저장
        GatewayPlugin plugin = pluginMapper.toEntity(request, scope.route, scope.service, scope.consumerGroup);
        GatewayPlugin saved = pluginRepository.save(plugin);

        log.info("Plugin created: {} (id={}, scope={})", saved.getName(), saved.getId(), saved.getScope());
        return pluginMapper.toResponse(saved);
    }

    /**
     * Plugin 수정
     */
    @Transactional
    public PluginResponse update(Long id, PluginRequest request) {
        log.info("Updating plugin: id={}", id);

        // Plugin 존재 확인
        GatewayPlugin plugin = pluginRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Plugin " + id));

        // Scope 검증 및 연관 엔티티 조회
        ScopeEntities scope = resolveScopeEntities(request);

        // 동일 scope+entity 내 중복 확인 (자신 제외)
        if (!plugin.getName().equals(request.getName()) || !plugin.getScope().equals(request.getScope())) {
            if (pluginRepository.existsDuplicatePlugin(
                    request.getName(), request.getScope(),
                    request.getRouteId(), request.getServiceId(), request.getConsumerGroupId())) {
                throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE,
                        "Plugin '" + request.getName() + "' already exists in " + request.getScope() + " scope");
            }
        }

        // Plugin 업데이트
        pluginMapper.updateEntity(plugin, request, scope.route, scope.service, scope.consumerGroup);
        GatewayPlugin updated = pluginRepository.save(plugin);

        log.info("Plugin updated: {} (id={}, scope={})", updated.getName(), updated.getId(), updated.getScope());
        return pluginMapper.toResponse(updated);
    }

    /**
     * Scope에 따른 연관 엔티티 조회
     */
    private ScopeEntities resolveScopeEntities(PluginRequest request) {
        GatewayRoute route = null;
        GatewayService service = null;
        GatewayConsumerGroup consumerGroup = null;

        switch (request.getScope()) {
            case "ROUTE":
                if (request.getRouteId() == null) {
                    throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED, "Route ID is required for ROUTE scope plugin");
                }
                route = routeRepository.findById(request.getRouteId())
                        .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Route " + request.getRouteId()));
                break;

            case "SERVICE":
                if (request.getServiceId() == null) {
                    throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED, "Service ID is required for SERVICE scope plugin");
                }
                service = serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Service " + request.getServiceId()));
                break;

            case "CONSUMER_GROUP":
                if (request.getConsumerGroupId() == null) {
                    throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED, "Consumer Group ID is required for CONSUMER_GROUP scope plugin");
                }
                consumerGroup = consumerGroupRepository.findById(request.getConsumerGroupId())
                        .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + request.getConsumerGroupId()));
                break;

            case "GLOBAL":
                break;

            default:
                throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED, "Invalid scope: " + request.getScope());
        }

        return new ScopeEntities(route, service, consumerGroup);
    }

    private record ScopeEntities(GatewayRoute route, GatewayService service, GatewayConsumerGroup consumerGroup) {}

    /**
     * Plugin 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting plugin: id={}", id);

        if (!pluginRepository.existsById(id)) {
            throw new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Plugin " + id);
        }

        pluginRepository.deleteById(id);
        log.info("Plugin deleted: id={}", id);
    }

    /**
     * Plugin 활성화/비활성화
     */
    @Transactional
    public PluginResponse toggleEnabled(Long id) {
        log.info("Toggling plugin enabled status: id={}", id);

        GatewayPlugin plugin = pluginRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Plugin " + id));

        plugin.setEnabled(!plugin.getEnabled());
        GatewayPlugin updated = pluginRepository.save(plugin);

        log.info("Plugin enabled status toggled: {} -> {}", id, updated.getEnabled());
        return pluginMapper.toResponse(updated);
    }

    /**
     * 활성화된 Plugin 조회
     */
    public List<PluginResponse> findAllEnabled() {
        return pluginRepository.findByEnabledTrue().stream()
                .map(pluginMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Plugin 통계
     */
    public Map<String, Object> getStats() {
        long total = pluginRepository.count();
        long enabled = pluginRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPlugins", total);
        stats.put("enabledPlugins", enabled);
        return stats;
    }
}
