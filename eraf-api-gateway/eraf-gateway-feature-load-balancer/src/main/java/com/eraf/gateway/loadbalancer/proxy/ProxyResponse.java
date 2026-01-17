package com.eraf.gateway.loadbalancer.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

/**
 * Response from proxied backend server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyResponse {
    /**
     * HTTP status code.
     */
    private int statusCode;

    /**
     * Response headers.
     */
    private HttpHeaders headers;

    /**
     * Response body.
     */
    private String body;

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * Error message if request failed.
     */
    private String error;

    /**
     * Check if response indicates server error (5xx).
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    /**
     * Check if response indicates client error (4xx).
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
}
