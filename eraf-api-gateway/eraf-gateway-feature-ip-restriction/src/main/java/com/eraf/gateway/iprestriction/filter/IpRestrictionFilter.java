package com.eraf.gateway.iprestriction.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import com.eraf.gateway.iprestriction.exception.IpBlockedException;
import com.eraf.gateway.iprestriction.service.IpRestrictionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * IP Restriction 필터
 */
@Slf4j
@RequiredArgsConstructor
public class IpRestrictionFilter extends OncePerRequestFilter {

    private final IpRestrictionService ipRestrictionService;
    private final List<String> excludePatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 제외 패턴 확인
        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = HttpUtils.getClientIp(request);

        try {
            ipRestrictionService.checkIpAccess(clientIp, path);
            filterChain.doFilter(request, response);

        } catch (IpBlockedException e) {
            log.warn("IP blocked: {} on path: {}", clientIp, path);
            GatewayResponseUtils.sendError(response, GatewayErrorCode.IP_BLOCKED);
        }
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
