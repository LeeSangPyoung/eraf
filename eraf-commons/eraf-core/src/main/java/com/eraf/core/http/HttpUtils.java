package com.eraf.core.http;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP 요청 유틸리티
 */
public final class HttpUtils {

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    );

    private HttpUtils() {
    }

    /**
     * 실제 클라이언트 IP 조회 (프록시 고려)
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP가 쉼표로 구분되어 있을 수 있음
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * User-Agent 조회
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Ajax 요청 여부 판별
     */
    public static boolean isAjax(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }

    /**
     * 모바일 요청 여부 판별
     */
    public static boolean isMobile(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if (userAgent == null) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        return userAgent.contains("mobile") ||
                userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("ipad") ||
                userAgent.contains("ipod");
    }

    /**
     * 브라우저 종류 판별
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("edg")) {
            return "Edge";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("safari")) {
            return "Safari";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        }
        return "Unknown";
    }

    /**
     * OS 종류 판별
     */
    public static String getOS(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        }
        return "Unknown";
    }

    /**
     * Referer 조회
     */
    public static String getReferer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }

    /**
     * 요청 URL 조회 (쿼리 스트링 포함)
     */
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURL());
        String queryString = request.getQueryString();
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    /**
     * Bearer 토큰 추출
     */
    public static String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
