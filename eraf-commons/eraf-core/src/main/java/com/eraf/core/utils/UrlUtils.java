package com.eraf.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * URL 유틸리티
 * 웹 개발의 필수 유틸리티
 */
public final class UrlUtils {

    private UrlUtils() {
    }

    // ===== URL 인코딩/디코딩 =====

    /**
     * URL 인코딩 (UTF-8)
     */
    public static String encode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }

    /**
     * URL 디코딩 (UTF-8)
     */
    public static String decode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 decoding not supported", e);
        }
    }

    /**
     * URL 인코딩 (Charset 지정)
     */
    public static String encode(String value, String charset) {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + charset, e);
        }
    }

    /**
     * URL 디코딩 (Charset 지정)
     */
    public static String decode(String value, String charset) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Decoding not supported: " + charset, e);
        }
    }

    // ===== URL 생성 =====

    /**
     * Base URL과 쿼리 파라미터로 URL 생성
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return baseUrl;
        }
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        String queryString = buildQueryString(params);
        if (baseUrl.contains("?")) {
            return baseUrl + "&" + queryString;
        } else {
            return baseUrl + "?" + queryString;
        }
    }

    /**
     * Base URL, 경로, 쿼리 파라미터로 URL 생성
     */
    public static String buildUrl(String baseUrl, String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl);

        // 경로 추가
        if (path != null && !path.isEmpty()) {
            if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
                url.append("/");
            }
            url.append(path);
        }

        // 쿼리 파라미터 추가
        if (params != null && !params.isEmpty()) {
            String queryString = buildQueryString(params);
            if (url.indexOf("?") != -1) {
                url.append("&");
            } else {
                url.append("?");
            }
            url.append(queryString);
        }

        return url.toString();
    }

    // ===== 쿼리 스트링 처리 =====

    /**
     * Map을 쿼리 스트링으로 변환
     * 예: {a: "1", b: "2"} -> "a=1&b=2"
     */
    public static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * 쿼리 스트링을 Map으로 파싱
     * 예: "a=1&b=2" -> {a: "1", b: "2"}
     */
    public static Map<String, String> parseQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // ? 제거
        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        Map<String, String> params = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String value = decode(pair.substring(idx + 1));
                params.put(key, value);
            } else {
                params.put(decode(pair), "");
            }
        }
        return params;
    }

    /**
     * 쿼리 스트링을 MultiValueMap으로 파싱 (같은 키의 여러 값 지원)
     */
    public static Map<String, List<String>> parseQueryStringMultiValue(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return new LinkedHashMap<>();
        }

        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        Map<String, List<String>> params = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key, value;
            if (idx > 0) {
                key = decode(pair.substring(0, idx));
                value = decode(pair.substring(idx + 1));
            } else {
                key = decode(pair);
                value = "";
            }
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return params;
    }

    // ===== URL 파싱 =====

    /**
     * URL에서 도메인 추출
     */
    public static String getDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * URL에서 프로토콜 추출 (http, https 등)
     */
    public static String getProtocol(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * URL에서 포트 추출
     */
    public static int getPort(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPort();
        } catch (URISyntaxException e) {
            return -1;
        }
    }

    /**
     * URL에서 경로 추출
     */
    public static String getPath(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPath();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * URL에서 쿼리 스트링 추출
     */
    public static String getQueryString(String url) {
        try {
            URI uri = new URI(url);
            return uri.getQuery();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * URL에서 쿼리 파라미터 추출
     */
    public static Map<String, String> getQueryParams(String url) {
        String queryString = getQueryString(url);
        return parseQueryString(queryString);
    }

    /**
     * URL에서 특정 쿼리 파라미터 값 추출
     */
    public static String getQueryParam(String url, String paramName) {
        Map<String, String> params = getQueryParams(url);
        return params.get(paramName);
    }

    // ===== URL 조작 =====

    /**
     * URL에 쿼리 파라미터 추가
     */
    public static String addQueryParam(String url, String key, String value) {
        if (url == null || key == null) {
            return url;
        }
        Map<String, String> params = new HashMap<>();
        params.put(key, value);
        return buildUrl(url, params);
    }

    /**
     * URL에서 쿼리 파라미터 제거
     */
    public static String removeQueryParam(String url, String paramName) {
        if (url == null || paramName == null) {
            return url;
        }

        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null) {
                return url;
            }

            Map<String, String> params = parseQueryString(query);
            params.remove(paramName);

            String newQuery = buildQueryString(params);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(),
                    newQuery.isEmpty() ? null : newQuery, uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * URL의 쿼리 스트링 전체 제거
     */
    public static String removeQueryString(String url) {
        if (url == null) {
            return null;
        }
        int index = url.indexOf('?');
        return index != -1 ? url.substring(0, index) : url;
    }

    // ===== 검증 =====

    /**
     * 유효한 URL인지 확인
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * HTTP/HTTPS URL인지 확인
     */
    public static boolean isHttpUrl(String url) {
        if (!isValidUrl(url)) {
            return false;
        }
        String protocol = getProtocol(url);
        return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
    }

    /**
     * HTTPS URL인지 확인
     */
    public static boolean isHttpsUrl(String url) {
        if (!isValidUrl(url)) {
            return false;
        }
        return "https".equalsIgnoreCase(getProtocol(url));
    }

    /**
     * 로컬호스트 URL인지 확인
     */
    public static boolean isLocalhost(String url) {
        String domain = getDomain(url);
        return domain != null &&
                (domain.equals("localhost") || domain.equals("127.0.0.1") || domain.equals("::1"));
    }

    // ===== URL 정규화 =====

    /**
     * URL 정규화 (슬래시 정리, 프로토콜 소문자 등)
     */
    public static String normalize(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = new URI(url);
            return uri.normalize().toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * 경로 결합 (슬래시 처리)
     */
    public static String joinPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            if (path == null || path.isEmpty()) {
                continue;
            }

            // 첫 번째가 아니면 앞 슬래시 제거
            if (i > 0 && path.startsWith("/")) {
                path = path.substring(1);
            }

            // 마지막이 아니면 뒤 슬래시 제거
            if (i < paths.length - 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            result.append(path);
            if (i < paths.length - 1 && !path.isEmpty()) {
                result.append("/");
            }
        }
        return result.toString();
    }

    // ===== 유틸리티 =====

    /**
     * Base URL 추출 (프로토콜 + 도메인 + 포트)
     * 예: https://example.com:8080/path?q=1 -> https://example.com:8080
     */
    public static String getBaseUrl(String url) {
        try {
            URL u = new URL(url);
            int port = u.getPort();
            String portPart = (port == -1 || port == 80 || port == 443) ? "" : ":" + port;
            return u.getProtocol() + "://" + u.getHost() + portPart;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 상대 URL을 절대 URL로 변환
     */
    public static String toAbsoluteUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null) {
            return baseUrl;
        }
        if (isValidUrl(relativeUrl)) {
            return relativeUrl;
        }
        try {
            URL base = new URL(baseUrl);
            URL absolute = new URL(base, relativeUrl);
            return absolute.toString();
        } catch (MalformedURLException e) {
            return relativeUrl;
        }
    }

    /**
     * 파일 확장자 추출
     */
    public static String getExtension(String url) {
        String path = getPath(url);
        if (path == null) {
            return null;
        }
        int lastDot = path.lastIndexOf('.');
        int lastSlash = path.lastIndexOf('/');
        if (lastDot > lastSlash && lastDot < path.length() - 1) {
            return path.substring(lastDot + 1);
        }
        return null;
    }
}
