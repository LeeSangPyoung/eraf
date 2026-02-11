package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.ConsumerRequest;
import com.eraf.openapi.admin.dto.ConsumerResponse;
import com.eraf.openapi.admin.mapper.ConsumerMapper;
import com.eraf.openapi.core.domain.GatewayConsumer;
import com.eraf.openapi.core.repository.GatewayConsumerRepository;
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
 * Consumer 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumerAdminService {

    private final GatewayConsumerRepository consumerRepository;
    private final ConsumerMapper consumerMapper;

    /**
     * 모든 Consumer 조회
     */
    public List<ConsumerResponse> findAll() {
        return consumerRepository.findAll().stream()
                .map(consumerMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Consumer 조회
     */
    public List<ConsumerResponse> findAllEnabled() {
        return consumerRepository.findByEnabledTrue().stream()
                .map(consumerMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Consumer 조회
     */
    public ConsumerResponse findById(Long id) {
        GatewayConsumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + id));
        return consumerMapper.toResponse(consumer);
    }

    /**
     * Username으로 Consumer 조회
     */
    public ConsumerResponse findByUsername(String username) {
        GatewayConsumer consumer = consumerRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + username));
        return consumerMapper.toResponse(consumer);
    }

    /**
     * API Key로 Consumer 조회
     */
    public ConsumerResponse findByApiKey(String apiKey) {
        GatewayConsumer consumer = consumerRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer with API Key"));
        return consumerMapper.toResponse(consumer);
    }

    /**
     * Consumer 생성
     */
    @Transactional
    public ConsumerResponse create(ConsumerRequest request) {
        log.info("Creating consumer: {}", request.getUsername());

        // Username 중복 확인
        if (consumerRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer username " + request.getUsername());
        }

        // Custom ID 중복 확인
        if (request.getCustomId() != null && !request.getCustomId().isEmpty()) {
            consumerRepository.findByCustomId(request.getCustomId())
                    .ifPresent(existing -> {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer with customId " + request.getCustomId());
                    });
        }

        // Consumer 생성 및 저장 (API Key는 Entity의 @PrePersist에서 자동 생성)
        GatewayConsumer consumer = consumerMapper.toEntity(request);
        GatewayConsumer saved = consumerRepository.save(consumer);

        log.info("Consumer created: {} (id={})", saved.getUsername(), saved.getId());
        return consumerMapper.toResponse(saved);
    }

    /**
     * Consumer 수정
     */
    @Transactional
    public ConsumerResponse update(Long id, ConsumerRequest request) {
        log.info("Updating consumer: {} (id={})", request.getUsername(), id);

        // Consumer 존재 확인
        GatewayConsumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + id));

        // Username 중복 확인 (본인 제외)
        consumerRepository.findByUsername(request.getUsername())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer username " + request.getUsername());
                    }
                });

        // Custom ID 중복 확인 (본인 제외)
        if (request.getCustomId() != null && !request.getCustomId().isEmpty()) {
            consumerRepository.findByCustomId(request.getCustomId())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Consumer with customId " + request.getCustomId());
                        }
                    });
        }

        // Consumer 업데이트
        consumerMapper.updateEntity(consumer, request);
        GatewayConsumer updated = consumerRepository.save(consumer);

        log.info("Consumer updated: {} (id={})", updated.getUsername(), updated.getId());
        return consumerMapper.toResponse(updated);
    }

    /**
     * Consumer 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting consumer: {}", id);

        GatewayConsumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + id));

        consumerRepository.delete(consumer);
        log.info("Consumer deleted: {} (id={})", consumer.getUsername(), id);
    }

    /**
     * Consumer 활성화/비활성화 토글
     */
    @Transactional
    public ConsumerResponse toggleEnabled(Long id) {
        log.info("Toggling consumer: {}", id);

        GatewayConsumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + id));

        consumer.setEnabled(!consumer.getEnabled());
        GatewayConsumer updated = consumerRepository.save(consumer);

        log.info("Consumer toggled: {} (id={}, enabled={})", updated.getUsername(), updated.getId(), updated.getEnabled());
        return consumerMapper.toResponse(updated);
    }

    /**
     * API Key 재생성
     */
    @Transactional
    public ConsumerResponse regenerateApiKey(Long id) {
        log.info("Regenerating API Key for consumer: {}", id);

        GatewayConsumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Consumer " + id));

        consumer.regenerateApiKey();
        GatewayConsumer updated = consumerRepository.save(consumer);

        log.info("API Key regenerated for consumer: {} (id={})", updated.getUsername(), updated.getId());
        return consumerMapper.toResponse(updated);
    }

    /**
     * Consumer 통계
     */
    public Map<String, Object> getStats() {
        long total = consumerRepository.count();
        long enabled = consumerRepository.countByEnabledTrue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConsumers", total);
        stats.put("enabledConsumers", enabled);
        stats.put("disabledConsumers", total - enabled);

        return stats;
    }
}
