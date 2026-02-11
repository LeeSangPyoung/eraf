package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.CertificateRequest;
import com.eraf.openapi.admin.dto.CertificateResponse;
import com.eraf.openapi.core.domain.GatewayCertificate;
import org.springframework.stereotype.Component;

/**
 * Certificate Entity ↔ DTO 변환 Mapper
 */
@Component
public class CertificateMapper {

    public GatewayCertificate toEntity(CertificateRequest request) {
        return GatewayCertificate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cert(request.getCert())
                .privateKey(request.getPrivateKey())
                .certChain(request.getCertChain())
                .snis(request.getSnis())
                .issuer(request.getIssuer())
                .subject(request.getSubject())
                .validFrom(request.getValidFrom())
                .expiresAt(request.getExpiresAt())
                .certType(request.getCertType())
                .enabled(request.getEnabled())
                .tags(request.getTags())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntity(GatewayCertificate certificate, CertificateRequest request) {
        certificate.setName(request.getName());
        certificate.setDescription(request.getDescription());
        certificate.setCert(request.getCert());
        if (request.getPrivateKey() != null && !request.getPrivateKey().isBlank()) {
            certificate.setPrivateKey(request.getPrivateKey());
        }
        certificate.setCertChain(request.getCertChain());
        certificate.setSnis(request.getSnis());
        certificate.setIssuer(request.getIssuer());
        certificate.setSubject(request.getSubject());
        certificate.setValidFrom(request.getValidFrom());
        certificate.setExpiresAt(request.getExpiresAt());
        certificate.setCertType(request.getCertType());
        certificate.setEnabled(request.getEnabled());
        if (request.getTags() != null) {
            certificate.setTags(request.getTags());
        }
        if (request.getMetadata() != null) {
            certificate.setMetadata(request.getMetadata());
        }
    }

    public CertificateResponse toResponse(GatewayCertificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .description(certificate.getDescription())
                .cert(certificate.getCert())
                .certChain(certificate.getCertChain())
                .snis(certificate.getSnis())
                .issuer(certificate.getIssuer())
                .subject(certificate.getSubject())
                .validFrom(certificate.getValidFrom())
                .expiresAt(certificate.getExpiresAt())
                .certType(certificate.getCertType())
                .enabled(certificate.getEnabled())
                .expired(certificate.isExpired())
                .expiringSoon(certificate.isExpiringSoon())
                .tags(certificate.getTags())
                .metadata(certificate.getMetadata())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .createdBy(certificate.getCreatedBy())
                .build();
    }
}
