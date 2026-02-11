package com.eraf.openapi.admin.service;

import com.eraf.openapi.admin.dto.CertificateRequest;
import com.eraf.openapi.admin.dto.CertificateResponse;
import com.eraf.openapi.admin.mapper.CertificateMapper;
import com.eraf.openapi.core.domain.GatewayCertificate;
import com.eraf.openapi.core.repository.GatewayCertificateRepository;
import com.eraf.core.exception.BusinessException;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Certificate 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateAdminService {

    private final GatewayCertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;

    /**
     * 모든 Certificate 조회
     */
    public List<CertificateResponse> findAll() {
        return certificateRepository.findAll().stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 Certificate 조회
     */
    public List<CertificateResponse> findAllEnabled() {
        return certificateRepository.findByEnabledTrue().stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID로 Certificate 조회
     */
    public CertificateResponse findById(Long id) {
        GatewayCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Certificate " + id));
        return certificateMapper.toResponse(certificate);
    }

    /**
     * Name으로 Certificate 조회
     */
    public CertificateResponse findByName(String name) {
        GatewayCertificate certificate = certificateRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Certificate " + name));
        return certificateMapper.toResponse(certificate);
    }

    /**
     * Certificate 생성
     */
    @Transactional
    public CertificateResponse create(CertificateRequest request) {
        log.info("Creating certificate: {}", request.getName());

        if (request.getPrivateKey() == null || request.getPrivateKey().isBlank()) {
            throw new BusinessException(GatewayErrorCode.VALIDATION_FAILED, "Private key is required for new certificate");
        }

        if (certificateRepository.existsByName(request.getName())) {
            throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Certificate name " + request.getName());
        }

        GatewayCertificate certificate = certificateMapper.toEntity(request);
        GatewayCertificate saved = certificateRepository.save(certificate);

        log.info("Certificate created: {} (id={})", saved.getName(), saved.getId());
        return certificateMapper.toResponse(saved);
    }

    /**
     * Certificate 수정
     */
    @Transactional
    public CertificateResponse update(Long id, CertificateRequest request) {
        log.info("Updating certificate: {} (id={})", request.getName(), id);

        GatewayCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Certificate " + id));

        // Name 중복 확인 (본인 제외)
        certificateRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(GatewayErrorCode.DUPLICATE_RESOURCE, "Certificate name " + request.getName());
                    }
                });

        certificateMapper.updateEntity(certificate, request);
        GatewayCertificate updated = certificateRepository.save(certificate);

        log.info("Certificate updated: {} (id={})", updated.getName(), updated.getId());
        return certificateMapper.toResponse(updated);
    }

    /**
     * Certificate 삭제
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting certificate: {}", id);

        GatewayCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Certificate " + id));

        certificateRepository.delete(certificate);
        log.info("Certificate deleted: {} (id={})", certificate.getName(), id);
    }

    /**
     * Certificate 활성화/비활성화 토글
     */
    @Transactional
    public CertificateResponse toggleEnabled(Long id) {
        log.info("Toggling certificate: {}", id);

        GatewayCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(GatewayErrorCode.RESOURCE_NOT_FOUND, "Certificate " + id));

        certificate.setEnabled(!certificate.getEnabled());
        GatewayCertificate updated = certificateRepository.save(certificate);

        log.info("Certificate toggled: {} (id={}, enabled={})", updated.getName(), updated.getId(), updated.getEnabled());
        return certificateMapper.toResponse(updated);
    }

    /**
     * 만료 임박 Certificate 조회 (30일 이내)
     */
    public List<CertificateResponse> findExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(30);
        return certificateRepository.findExpiringSoonCertificates(now, threshold).stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 Certificate 조회
     */
    public List<CertificateResponse> findExpired() {
        return certificateRepository.findExpiredCertificates(LocalDateTime.now()).stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Certificate 통계
     */
    public Map<String, Object> getStats() {
        long total = certificateRepository.count();
        long enabled = certificateRepository.countByEnabledTrue();
        long expired = certificateRepository.countExpiredCertificates();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCertificates", total);
        stats.put("enabledCertificates", enabled);
        stats.put("expiredCertificates", expired);

        return stats;
    }
}
