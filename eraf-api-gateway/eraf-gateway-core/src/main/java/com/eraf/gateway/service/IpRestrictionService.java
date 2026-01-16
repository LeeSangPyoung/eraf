package com.eraf.gateway.service;

import com.eraf.gateway.domain.IpRestriction;
import com.eraf.gateway.exception.IpBlockedException;
import com.eraf.gateway.repository.IpRestrictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * IP Restriction 서비스
 */
@Slf4j
@RequiredArgsConstructor
public class IpRestrictionService {

    private final IpRestrictionRepository repository;

    /**
     * IP 접근 체크
     * @param clientIp 클라이언트 IP
     * @param path 요청 경로
     * @throws IpBlockedException IP가 차단된 경우
     */
    public void checkIpAccess(String clientIp, String path) {
        // 1. DENY 리스트 확인 (블랙리스트)
        List<IpRestriction> denyList = repository.findEnabledByType(IpRestriction.RestrictionType.DENY);
        for (IpRestriction restriction : denyList) {
            if (restriction.isValid() && restriction.matches(clientIp) && restriction.matchesPath(path)) {
                log.warn("IP blocked by deny rule: {} for path: {}", clientIp, path);
                throw new IpBlockedException(clientIp);
            }
        }

        // 2. ALLOW 리스트 확인 (화이트리스트가 있으면 그 외 차단)
        List<IpRestriction> allowList = repository.findEnabledByType(IpRestriction.RestrictionType.ALLOW);

        // 해당 경로에 대한 화이트리스트가 있는지 확인
        List<IpRestriction> pathAllowList = allowList.stream()
                .filter(r -> r.isValid() && r.matchesPath(path))
                .toList();

        if (!pathAllowList.isEmpty()) {
            // 화이트리스트가 존재하면, 해당 IP가 리스트에 있어야 함
            boolean allowed = pathAllowList.stream()
                    .anyMatch(r -> r.matches(clientIp));

            if (!allowed) {
                log.warn("IP not in whitelist: {} for path: {}", clientIp, path);
                throw new IpBlockedException(clientIp);
            }
        }

        log.debug("IP access allowed: {} for path: {}", clientIp, path);
    }

    /**
     * IP 접근 허용 여부 확인 (예외 발생 없이)
     */
    public boolean isIpAllowed(String clientIp, String path) {
        try {
            checkIpAccess(clientIp, path);
            return true;
        } catch (IpBlockedException e) {
            return false;
        }
    }

    /**
     * IP 제한 규칙 생성
     */
    public IpRestriction createRestriction(IpRestriction restriction) {
        return repository.save(restriction);
    }

    /**
     * IP 제한 규칙 수정
     */
    public IpRestriction updateRestriction(IpRestriction restriction) {
        return repository.save(restriction);
    }

    /**
     * IP 제한 규칙 삭제
     */
    public void deleteRestriction(String id) {
        repository.deleteById(id);
    }

    /**
     * 모든 규칙 조회
     */
    public List<IpRestriction> getAllRestrictions() {
        return repository.findAll();
    }

    /**
     * 규칙 ID로 조회
     */
    public Optional<IpRestriction> getRestriction(String id) {
        return repository.findById(id);
    }

    /**
     * IP 주소 블랙리스트에 추가
     */
    public IpRestriction blockIp(String ipAddress, String description) {
        IpRestriction restriction = IpRestriction.builder()
                .ipAddress(ipAddress)
                .type(IpRestriction.RestrictionType.DENY)
                .description(description)
                .enabled(true)
                .build();
        return repository.save(restriction);
    }

    /**
     * IP 주소 화이트리스트에 추가
     */
    public IpRestriction allowIp(String ipAddress, String pathPattern, String description) {
        IpRestriction restriction = IpRestriction.builder()
                .ipAddress(ipAddress)
                .type(IpRestriction.RestrictionType.ALLOW)
                .pathPattern(pathPattern)
                .description(description)
                .enabled(true)
                .build();
        return repository.save(restriction);
    }
}
