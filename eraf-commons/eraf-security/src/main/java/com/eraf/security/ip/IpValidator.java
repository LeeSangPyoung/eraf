package com.eraf.security.ip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * IP 주소 검증 유틸리티
 * CIDR 표기법 지원
 */
public class IpValidator {

    private static final Logger log = LoggerFactory.getLogger(IpValidator.class);

    private final List<IpMatcher> whitelist;
    private final List<IpMatcher> blacklist;

    public IpValidator(List<String> whitelistPatterns, List<String> blacklistPatterns) {
        this.whitelist = parsePatterns(whitelistPatterns);
        this.blacklist = parsePatterns(blacklistPatterns);
    }

    /**
     * IP가 화이트리스트에 있는지 확인
     */
    public boolean isWhitelisted(String ip) {
        if (whitelist.isEmpty()) {
            return false;
        }
        return matchesAny(ip, whitelist);
    }

    /**
     * IP가 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String ip) {
        if (blacklist.isEmpty()) {
            return false;
        }
        return matchesAny(ip, blacklist);
    }

    /**
     * IP가 접근 허용되는지 확인
     *
     * @param ip IP 주소
     * @param defaultPolicy 기본 정책
     * @return true면 허용, false면 차단
     */
    public boolean isAllowed(String ip, IpAccessControlProperties.AccessPolicy defaultPolicy) {
        // 블랙리스트 우선 확인
        if (isBlacklisted(ip)) {
            return false;
        }

        // 화이트리스트 확인
        if (isWhitelisted(ip)) {
            return true;
        }

        // 기본 정책에 따라 결정
        return defaultPolicy == IpAccessControlProperties.AccessPolicy.ALLOW;
    }

    private boolean matchesAny(String ip, List<IpMatcher> matchers) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            for (IpMatcher matcher : matchers) {
                if (matcher.matches(address)) {
                    return true;
                }
            }
        } catch (UnknownHostException e) {
            log.warn("Invalid IP address: {}", ip, e);
        }
        return false;
    }

    private List<IpMatcher> parsePatterns(List<String> patterns) {
        List<IpMatcher> matchers = new ArrayList<>();
        for (String pattern : patterns) {
            try {
                if (pattern.contains("/")) {
                    // CIDR notation
                    matchers.add(new CidrMatcher(pattern));
                } else {
                    // Single IP
                    matchers.add(new ExactIpMatcher(pattern));
                }
            } catch (Exception e) {
                log.error("Failed to parse IP pattern: {}", pattern, e);
            }
        }
        return matchers;
    }

    /**
     * IP 매칭 인터페이스
     */
    private interface IpMatcher {
        boolean matches(InetAddress address);
    }

    /**
     * 정확한 IP 매칭
     */
    private static class ExactIpMatcher implements IpMatcher {
        private final InetAddress targetAddress;

        public ExactIpMatcher(String ip) throws UnknownHostException {
            this.targetAddress = InetAddress.getByName(ip);
        }

        @Override
        public boolean matches(InetAddress address) {
            return targetAddress.equals(address);
        }
    }

    /**
     * CIDR 범위 매칭
     * 예: 192.168.1.0/24
     */
    private static class CidrMatcher implements IpMatcher {
        private final InetAddress networkAddress;
        private final int prefixLength;

        public CidrMatcher(String cidr) throws UnknownHostException {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
            }

            this.networkAddress = InetAddress.getByName(parts[0]);
            this.prefixLength = Integer.parseInt(parts[1]);

            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
            }
        }

        @Override
        public boolean matches(InetAddress address) {
            byte[] networkBytes = networkAddress.getAddress();
            byte[] addressBytes = address.getAddress();

            // IPv4만 지원
            if (networkBytes.length != 4 || addressBytes.length != 4) {
                return false;
            }

            int mask = -1 << (32 - prefixLength);

            int networkInt = bytesToInt(networkBytes);
            int addressInt = bytesToInt(addressBytes);

            return (networkInt & mask) == (addressInt & mask);
        }

        private int bytesToInt(byte[] bytes) {
            return ((bytes[0] & 0xFF) << 24) |
                   ((bytes[1] & 0xFF) << 16) |
                   ((bytes[2] & 0xFF) << 8) |
                   (bytes[3] & 0xFF);
        }
    }
}
