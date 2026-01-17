package com.eraf.gateway.loadbalancer.proxy;

import com.eraf.gateway.loadbalancer.domain.Server;
import com.eraf.gateway.loadbalancer.domain.Upstream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Enumeration;

/**
 * HTTP proxy client for forwarding requests to backend servers.
 * Handles connection pooling, timeouts, and error handling.
 */
@Slf4j
@Component
public class HttpProxyClient {
    private final WebClient.Builder webClientBuilder;

    public HttpProxyClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Proxy request to a backend server.
     */
    public Mono<ProxyResponse> proxyRequest(
            HttpServletRequest request,
            Server server,
            Upstream upstream) {

        String targetUrl = buildTargetUrl(server, request);
        HttpMethod method = HttpMethod.resolve(request.getMethod());

        if (method == null) {
            return Mono.error(new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod()));
        }

        log.debug("Proxying {} {} to {}", method, request.getRequestURI(), targetUrl);

        WebClient webClient = webClientBuilder
                .baseUrl(server.getUrl())
                .build();

        return webClient.method(method)
                .uri(targetUrl)
                .headers(headers -> copyHeaders(request, headers))
                .retrieve()
                .toEntity(String.class)
                .timeout(Duration.ofMillis(upstream.getReadTimeout()))
                .map(this::mapToProxyResponse)
                .onErrorResume(this::handleProxyError);
    }

    /**
     * Build target URL from server and request.
     */
    private String buildTargetUrl(Server server, HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();

        if (query != null && !query.isEmpty()) {
            return path + "?" + query;
        }
        return path;
    }

    /**
     * Copy headers from original request to proxy request.
     */
    private void copyHeaders(HttpServletRequest request, HttpHeaders headers) {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            // Skip hop-by-hop headers
            if (isHopByHopHeader(headerName)) {
                continue;
            }

            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }

        // Add X-Forwarded headers
        String clientIp = getClientIp(request);
        headers.add("X-Forwarded-For", clientIp);
        headers.add("X-Forwarded-Proto", request.getScheme());
        headers.add("X-Forwarded-Host", request.getServerName());
        headers.add("X-Real-IP", clientIp);
    }

    /**
     * Check if header is hop-by-hop header (should not be forwarded).
     */
    private boolean isHopByHopHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.equals("connection") ||
               lower.equals("keep-alive") ||
               lower.equals("proxy-authenticate") ||
               lower.equals("proxy-authorization") ||
               lower.equals("te") ||
               lower.equals("trailers") ||
               lower.equals("transfer-encoding") ||
               lower.equals("upgrade");
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            // Take first IP if multiple
            int commaIndex = ip.indexOf(',');
            if (commaIndex > 0) {
                ip = ip.substring(0, commaIndex).trim();
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * Map WebClient response to proxy response.
     */
    private ProxyResponse mapToProxyResponse(ResponseEntity<String> response) {
        return ProxyResponse.builder()
                .statusCode(response.getStatusCodeValue())
                .headers(response.getHeaders())
                .body(response.getBody())
                .success(true)
                .build();
    }

    /**
     * Handle proxy errors.
     */
    private Mono<ProxyResponse> handleProxyError(Throwable error) {
        log.error("Proxy request failed: {}", error.getMessage());

        if (error instanceof WebClientResponseException) {
            WebClientResponseException webClientError = (WebClientResponseException) error;
            return Mono.just(ProxyResponse.builder()
                    .statusCode(webClientError.getRawStatusCode())
                    .headers(webClientError.getHeaders())
                    .body(webClientError.getResponseBodyAsString())
                    .success(false)
                    .error(error.getMessage())
                    .build());
        }

        return Mono.just(ProxyResponse.builder()
                .statusCode(503)
                .body("Service Unavailable: " + error.getMessage())
                .success(false)
                .error(error.getMessage())
                .build());
    }
}
