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

    private boolean matchesCidr(String ip, String cidr) {
        // TODO: CIDR 매칭 로직 구현
        // Apache Commons Net의 SubnetUtils 또는 직접 구현
        return false;
    }
}
