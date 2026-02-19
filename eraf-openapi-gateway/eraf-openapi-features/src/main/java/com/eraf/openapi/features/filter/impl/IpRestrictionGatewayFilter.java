package com.eraf.openapi.features.filter.impl;

import com.eraf.core.logging.AuditLogger;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * IP Restriction Gateway Filter
 * IP 기반 접근 제어
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpRestrictionGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "ip-restriction";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // IP 제한 설정 추출
        String policy = (String) config.getOrDefault("policy", "whitelist"); // whitelist or blacklist
        List<String> ipList = (List<String>) config.getOrDefault("ip_list", List.of());

        return (exchange, chain) -> applyIpRestriction(exchange, chain, policy, ipList);
    }

    private Mono<Void> applyIpRestriction(ServerWebExchange exchange, GatewayFilterChain chain,
                                            String policy, List<String> ipList) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        boolean isInList = ipList.stream().anyMatch(ip -> matchesIp(clientIp, ip));

        // Whitelist 정책: 목록에 있으면 허용
        if ("whitelist".equals(policy)) {
            if (!isInList) {
                log.warn("IP not in whitelist: {}", clientIp);
                AuditLogger.log(AuditLogger.AuditEvent.builder()
                        .action("IP_RESTRICTED")
                        .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                        .clientIp(clientIp)
                        .failure()
                        .detail("policy", "whitelist")
                        .build());
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.IP_RESTRICTED);
            }
        }
        // Blacklist 정책: 목록에 있으면 차단
        else if ("blacklist".equals(policy)) {
            if (isInList) {
                log.warn("IP in blacklist: {}", clientIp);
                AuditLogger.log(AuditLogger.AuditEvent.builder()
                        .action("IP_RESTRICTED")
                        .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                        .clientIp(clientIp)
                        .failure()
                        .detail("policy", "blacklist")
                        .build());
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.IP_RESTRICTED);
            }
        }

        log.debug("IP restriction passed for: {}", clientIp);
        return chain.filter(exchange);
    }

    private boolean matchesIp(String clientIp, String pattern) {
        // 정확히 일치
        if (clientIp.equals(pattern)) {
            return true;
        }

        // CIDR 표기법 지원 (예: 192.168.1.0/24)
        if (pattern.contains("/")) {
            return matchesCidr(clientIp, pattern);
        }

        // 와일드카드 지원 (예: 192.168.1.*)
        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            return clientIp.matches(regex);
        }

        return false;
    }

    /**
     * CIDR 매칭 로직 구현
     *
     * @param ip 클라이언트 IP (예: "192.168.1.100")
     * @param cidr CIDR 표기법 (예: "192.168.1.0/24")
     * @return 매칭 여부
     */
    private boolean matchesCidr(String ip, String cidr) {
        try {
            // CIDR 분리: IP/prefix
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                log.warn("Invalid CIDR format: {}", cidr);
                return false;
            }

            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            // prefix length 검증
            if (prefixLength < 0 || prefixLength > 32) {
                log.warn("Invalid prefix length in CIDR: {}", cidr);
                return false;
            }

            // IP 주소를 long으로 변환
            long clientIpLong = ipToLong(ip);
            long networkIpLong = ipToLong(networkIp);

            // Subnet mask 계산 (prefix length 기반)
            long mask = prefixLength == 0 ? 0 : (0xFFFFFFFFL << (32 - prefixLength));

            // 네트워크 부분이 일치하는지 확인
            return (clientIpLong & mask) == (networkIpLong & mask);

        } catch (Exception e) {
            log.error("Error matching CIDR: {} for IP: {}", cidr, ip, e);
            return false;
        }
    }

    /**
     * IPv4 주소를 long으로 변환
     *
     * @param ip IP 주소 문자열 (예: "192.168.1.100")
     * @return long 값
     */
    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IP octet: " + octet);
            }
            result |= ((long) octet << ((3 - i) * 8));
        }
        return result;
    }
}
