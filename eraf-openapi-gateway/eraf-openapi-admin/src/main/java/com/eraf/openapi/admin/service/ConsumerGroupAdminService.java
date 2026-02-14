package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.ConsumerGroupRequest;
import com.eraf.openapi.admin.dto.ConsumerGroupResponse;
import com.eraf.openapi.admin.mapper.ConsumerGroupMapper;
import com.eraf.openapi.core.domain.GatewayConsumer;
import com.eraf.openapi.core.domain.GatewayConsumerGroup;
import com.eraf.openapi.core.repository.GatewayConsumerGroupRepository;
import com.eraf.openapi.core.repository.GatewayConsumerRepository;
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
 * Consumer Group 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumerGroupAdminService {

    private final GatewayConsumerGroupRepository consumerGroupRepository;
    private final GatewayConsumerRepository consumerRepository;
    private final ConsumerGroupMapper consumerGroupMapper;

    /**
     * 모든 Consumer Group 조회
     */
    public List<ConsumerGroupResponse> findAll() {
        return consumerGroupRepository.findAll().stream()
                .map(consumerGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Consumer Group 조회
     */
    public List<ConsumerGroupResponse> findAllEnabled() {
        return consumerGroupRepository.findByEnabledTrue().stream()
                .map(consumerGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Consumer Group 조회
     */
    public ConsumerGroupResponse findById(Long id) {
        GatewayConsumerGroup group = consumerGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + id));
        return consumerGroupMapper.toResponse(group);
    }

    /**
     * Name으로 Consumer Group 조회
     */
    public ConsumerGroupResponse findByName(String name) {
        GatewayConsumerGroup group = consumerGroupRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + name));
        return consumerGroupMapper.toResponse(group);
    }

    /**
     * Consumer Group 생성
     */
    @Transactional
    public ConsumerGroupResponse create(ConsumerGroupRequest request) {
        log.info("Creating consumer group: {}", request.getName());

        if (consumerGroupRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer Group name " + request.getName());
        }

        GatewayConsumerGroup group = consumerGroupMapper.toEntity(request);
        GatewayConsumerGroup saved = consumerGroupRepository.save(group);

        log.info("Consumer Group created: {} (id={})", saved.getName(), saved.getId());
        return consumerGroupMapper.toResponse(saved);
    }

    /**
     * Consumer Group 수정
     */
    @Transactional
    public ConsumerGroupResponse update(Long id, ConsumerGroupRequest request) {
        log.info("Updating consumer group: {} (id={})", request.getName(), id);

        GatewayConsumerGroup group = consumerGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + id));

        // Name 중복 확인 (본인 제외)
        consumerGroupRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer Group name " + request.getName());
                    }
                });

        consumerGroupMapper.updateEntity(group, request);
        GatewayConsumerGroup updated = consumerGroupRepository.save(group);

        log.info("Consumer Group updated: {} (id={})", updated.getName(), updated.getId());
        return consumerGroupMapper.toResponse(updated);
    }

    /**
     * Consumer Group 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting consumer group: {}", id);

        GatewayConsumerGroup group = consumerGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + id));

        consumerGroupRepository.delete(group);
        log.info("Consumer Group deleted: {} (id={})", group.getName(), id);
    }

    /**
     * Consumer Group 활성화/비활성화 토글
     */
    @Transactional
    public ConsumerGroupResponse toggleEnabled(Long id) {
        log.info("Toggling consumer group: {}", id);

        GatewayConsumerGroup group = consumerGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + id));

        group.setEnabled(!group.getEnabled());
        GatewayConsumerGroup updated = consumerGroupRepository.save(group);

        log.info("Consumer Group toggled: {} (id={}, enabled={})", updated.getName(), updated.getId(), updated.getEnabled());
        return consumerGroupMapper.toResponse(updated);
    }

    /**
     * Consumer를 그룹에 추가
     */
    @Transactional
    public ConsumerGroupResponse addConsumer(Long groupId, Long consumerId) {
        log.info("Adding consumer {} to group {}", consumerId, groupId);

        GatewayConsumerGroup group = consumerGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + groupId));

        GatewayConsumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + consumerId));

        if (group.getConsumers().contains(consumer)) {
            throw new BusinessException(GatewayErrorCode.CONSUMER_ALREADY_IN_GROUP,
                    consumer.getUsername() + " in " + group.getName());
        }

        group.addConsumer(consumer);
        GatewayConsumerGroup updated = consumerGroupRepository.save(group);

        log.info("Consumer {} added to group {}", consumer.getUsername(), group.getName());
        return consumerGroupMapper.toResponse(updated);
    }

    /**
     * Consumer를 그룹에서 제거
     */
    @Transactional
    public ConsumerGroupResponse removeConsumer(Long groupId, Long consumerId) {
        log.info("Removing consumer {} from group {}", consumerId, groupId);

        GatewayConsumerGroup group = consumerGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer Group " + groupId));

        GatewayConsumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + consumerId));

        if (!group.getConsumers().contains(consumer)) {
            throw new BusinessException(GatewayErrorCode.CONSUMER_NOT_IN_GROUP,
                    consumer.getUsername() + " in " + group.getName());
        }

        group.removeConsumer(consumer);
        GatewayConsumerGroup updated = consumerGroupRepository.save(group);

        log.info("Consumer {} removed from group {}", consumer.getUsername(), group.getName());
        return consumerGroupMapper.toResponse(updated);
    }

    /**
     * Consumer가 속한 그룹 조회
     */
    public List<ConsumerGroupResponse> findGroupsByConsumerId(Long consumerId) {
        // Consumer 존재 확인
        consumerRepository.findById(consumerId)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + consumerId));

        return consumerGroupRepository.findByConsumerId(consumerId).stream()
                .map(consumerGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Consumer Group 통계
     */
    public Map<String, Object> getStats() {
        long total = consumerGroupRepository.count();
        long enabled = consumerGroupRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGroups", total);
        stats.put("enabledGroups", enabled);
        stats.put("disabledGroups", total - enabled);

        return stats;
    }
}
