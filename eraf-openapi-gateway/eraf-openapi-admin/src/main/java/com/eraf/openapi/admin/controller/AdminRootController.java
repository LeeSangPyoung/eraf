package com.eraf.openapi.admin.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Admin API 루트 컨트롤러
 * 사용 가능한 API 엔드포인트 정보 제공
 */
@RestController
@RequestMapping("/admin")
public class AdminRootController {

    /**
     * Admin API 정보 조회
     */
    @GetMapping
    public ResponseEntity<AdminApiInfo> getAdminInfo() {
        AdminApiInfo info = new AdminApiInfo();
        info.setName("ERAF OpenAPI Gateway - Admin API");
        info.setVersion("1.0.0");
        info.setDescription("RESTful Admin API for managing API Gateway");

        info.setEndpoints(Arrays.asList(
            new EndpointInfo("GET", "/admin/routes", "모든 Route 조회"),
            new EndpointInfo("POST", "/admin/routes", "Route 생성"),
            new EndpointInfo("GET", "/admin/routes/{id}", "Route 상세 조회"),
            new EndpointInfo("PUT", "/admin/routes/{id}", "Route 수정"),
            new EndpointInfo("DELETE", "/admin/routes/{id}", "Route 삭제"),
            new EndpointInfo("PATCH", "/admin/routes/{id}/toggle", "Route 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/services", "모든 Service 조회"),
            new EndpointInfo("POST", "/admin/services", "Service 생성"),
            new EndpointInfo("GET", "/admin/services/{id}", "Service 상세 조회"),
            new EndpointInfo("PUT", "/admin/services/{id}", "Service 수정"),
            new EndpointInfo("DELETE", "/admin/services/{id}", "Service 삭제"),
            new EndpointInfo("PATCH", "/admin/services/{id}/toggle", "Service 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/targets", "모든 Target 조회"),
            new EndpointInfo("POST", "/admin/targets", "Target 생성"),
            new EndpointInfo("GET", "/admin/targets/{id}", "Target 상세 조회"),
            new EndpointInfo("PUT", "/admin/targets/{id}", "Target 수정"),
            new EndpointInfo("DELETE", "/admin/targets/{id}", "Target 삭제"),
            new EndpointInfo("PATCH", "/admin/targets/{id}/toggle", "Target 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/plugins", "모든 Plugin 조회"),
            new EndpointInfo("POST", "/admin/plugins", "Plugin 생성"),
            new EndpointInfo("GET", "/admin/plugins/{id}", "Plugin 상세 조회"),
            new EndpointInfo("PUT", "/admin/plugins/{id}", "Plugin 수정"),
            new EndpointInfo("DELETE", "/admin/plugins/{id}", "Plugin 삭제"),
            new EndpointInfo("PATCH", "/admin/plugins/{id}/toggle", "Plugin 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/apis", "모든 API 조회"),
            new EndpointInfo("POST", "/admin/apis", "API 생성"),
            new EndpointInfo("GET", "/admin/apis/{id}", "API 상세 조회"),
            new EndpointInfo("PUT", "/admin/apis/{id}", "API 수정"),
            new EndpointInfo("DELETE", "/admin/apis/{id}", "API 삭제"),
            new EndpointInfo("PATCH", "/admin/apis/{id}/toggle", "API 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/consumers", "모든 Consumer 조회"),
            new EndpointInfo("POST", "/admin/consumers", "Consumer 생성"),
            new EndpointInfo("GET", "/admin/consumers/{id}", "Consumer 상세 조회"),
            new EndpointInfo("PUT", "/admin/consumers/{id}", "Consumer 수정"),
            new EndpointInfo("DELETE", "/admin/consumers/{id}", "Consumer 삭제"),
            new EndpointInfo("PATCH", "/admin/consumers/{id}/toggle", "Consumer 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/upstreams", "모든 Upstream 조회"),
            new EndpointInfo("POST", "/admin/upstreams", "Upstream 생성"),
            new EndpointInfo("GET", "/admin/upstreams/{id}", "Upstream 상세 조회"),
            new EndpointInfo("PUT", "/admin/upstreams/{id}", "Upstream 수정"),
            new EndpointInfo("DELETE", "/admin/upstreams/{id}", "Upstream 삭제"),
            new EndpointInfo("PATCH", "/admin/upstreams/{id}/toggle", "Upstream 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/certificates", "모든 Certificate 조회"),
            new EndpointInfo("POST", "/admin/certificates", "Certificate 생성"),
            new EndpointInfo("GET", "/admin/certificates/{id}", "Certificate 상세 조회"),
            new EndpointInfo("PUT", "/admin/certificates/{id}", "Certificate 수정"),
            new EndpointInfo("DELETE", "/admin/certificates/{id}", "Certificate 삭제"),
            new EndpointInfo("PATCH", "/admin/certificates/{id}/toggle", "Certificate 활성화/비활성화"),

            new EndpointInfo("GET", "/admin/consumer-groups", "모든 Consumer Group 조회"),
            new EndpointInfo("POST", "/admin/consumer-groups", "Consumer Group 생성"),
            new EndpointInfo("GET", "/admin/consumer-groups/{id}", "Consumer Group 상세 조회"),
            new EndpointInfo("PUT", "/admin/consumer-groups/{id}", "Consumer Group 수정"),
            new EndpointInfo("DELETE", "/admin/consumer-groups/{id}", "Consumer Group 삭제"),
            new EndpointInfo("PATCH", "/admin/consumer-groups/{id}/toggle", "Consumer Group 활성화/비활성화")
        ));

        return ResponseEntity.ok(info);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminApiInfo {
        private String name;
        private String version;
        private String description;
        private List<EndpointInfo> endpoints;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointInfo {
        private String method;
        private String path;
        private String description;
    }
}
