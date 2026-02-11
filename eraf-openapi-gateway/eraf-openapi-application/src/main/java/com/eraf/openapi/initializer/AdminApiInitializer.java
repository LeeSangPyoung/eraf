package com.eraf.openapi.initializer;

import com.eraf.openapi.core.domain.GatewayApi;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.repository.GatewayApiRepository;
import com.eraf.openapi.core.repository.GatewayServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Admin API 초기 데이터 로더
 * <p>
 * 애플리케이션 시작 시 Admin API를 자동으로 API Registry에 등록합니다.
 * 순환 참조 문제 해결: Admin API를 먼저 등록해두면 이후 검증 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminApiInitializer implements ApplicationRunner {

    private final GatewayApiRepository apiRepository;
    private final GatewayServiceRepository serviceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("========== Initializing Admin APIs ==========");

        // Gateway Service 확인 (없으면 생성)
        GatewayService gatewayService = getOrCreateGatewayService();

        // Admin API 등록 (이미 있으면 스킵)
        registerAdminApis(gatewayService);

        log.info("========== Admin APIs initialization completed ==========");
    }

    private GatewayService getOrCreateGatewayService() {
        Optional<GatewayService> existing = serviceRepository.findByName("gateway-admin");

        if (existing.isPresent()) {
            log.info("Gateway service already exists: {}", existing.get().getId());
            return existing.get();
        }

        GatewayService service = GatewayService.builder()
                .name("gateway-admin")
                .description("Gateway Admin Service (Internal)")
                .protocol("http")
                .host("localhost")
                .port(9000)
                .enabled(true)
                .build();

        service = serviceRepository.save(service);
        log.info("Created gateway service: {}", service.getId());
        return service;
    }

    private void registerAdminApis(GatewayService service) {
        List<AdminApiDefinition> adminApis = Arrays.asList(
                // Routes APIs
                new AdminApiDefinition("admin-routes-list", "/admin/routes", "GET",
                        "List all routes", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-routes-get", "/admin/routes/*", "GET",
                        "Get route details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-routes-create", "/admin/routes", "POST",
                        "Create route", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-routes-update", "/admin/routes/*", "PUT",
                        "Update route", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-routes-delete", "/admin/routes/*", "DELETE",
                        "Delete route", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-routes-toggle", "/admin/routes/*/toggle", "PATCH",
                        "Toggle route enabled", List.of("ROLE_ADMIN")),

                // Services APIs
                new AdminApiDefinition("admin-services-list", "/admin/services", "GET",
                        "List all services", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-services-get", "/admin/services/*", "GET",
                        "Get service details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-services-create", "/admin/services", "POST",
                        "Create service", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-services-update", "/admin/services/*", "PUT",
                        "Update service", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-services-delete", "/admin/services/*", "DELETE",
                        "Delete service", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-services-toggle", "/admin/services/*/toggle", "PATCH",
                        "Toggle service enabled", List.of("ROLE_ADMIN")),

                // Targets APIs
                new AdminApiDefinition("admin-targets-list", "/admin/targets", "GET",
                        "List all targets", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-targets-get", "/admin/targets/*", "GET",
                        "Get target details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-targets-create", "/admin/targets", "POST",
                        "Create target", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-targets-update", "/admin/targets/*", "PUT",
                        "Update target", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-targets-delete", "/admin/targets/*", "DELETE",
                        "Delete target", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-targets-toggle", "/admin/targets/*/toggle", "PATCH",
                        "Toggle target enabled", List.of("ROLE_ADMIN")),

                // Plugins APIs
                new AdminApiDefinition("admin-plugins-list", "/admin/plugins", "GET",
                        "List all plugins", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-plugins-get", "/admin/plugins/*", "GET",
                        "Get plugin details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-plugins-create", "/admin/plugins", "POST",
                        "Create plugin", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-plugins-update", "/admin/plugins/*", "PUT",
                        "Update plugin", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-plugins-delete", "/admin/plugins/*", "DELETE",
                        "Delete plugin", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-plugins-toggle", "/admin/plugins/*/toggle", "PATCH",
                        "Toggle plugin enabled", List.of("ROLE_ADMIN")),

                // APIs APIs
                new AdminApiDefinition("admin-apis-list", "/admin/apis", "GET",
                        "List all APIs", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-apis-get", "/admin/apis/*", "GET",
                        "Get API details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-apis-stats", "/admin/apis/stats", "GET",
                        "Get API statistics", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-apis-create", "/admin/apis", "POST",
                        "Create API", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-apis-update", "/admin/apis/*", "PUT",
                        "Update API", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-apis-delete", "/admin/apis/*", "DELETE",
                        "Delete API", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-apis-toggle", "/admin/apis/*/toggle", "PATCH",
                        "Toggle API enabled", List.of("ROLE_ADMIN")),

                // Request Logs APIs
                new AdminApiDefinition("admin-logs-list", "/admin/request-logs", "GET",
                        "List request logs", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-logs-stats", "/admin/request-logs/stats", "GET",
                        "Get request logs statistics", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-logs-clear", "/admin/request-logs/clear", "DELETE",
                        "Clear request logs", List.of("ROLE_ADMIN")),

                // Upstreams APIs
                new AdminApiDefinition("admin-upstreams-list", "/admin/upstreams", "GET",
                        "List all upstreams", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-upstreams-get", "/admin/upstreams/*", "GET",
                        "Get upstream details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-upstreams-stats", "/admin/upstreams/stats", "GET",
                        "Get upstream statistics", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-upstreams-create", "/admin/upstreams", "POST",
                        "Create upstream", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-upstreams-update", "/admin/upstreams/*", "PUT",
                        "Update upstream", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-upstreams-delete", "/admin/upstreams/*", "DELETE",
                        "Delete upstream", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-upstreams-toggle", "/admin/upstreams/*/toggle", "PATCH",
                        "Toggle upstream enabled", List.of("ROLE_ADMIN")),

                // Certificates APIs
                new AdminApiDefinition("admin-certificates-list", "/admin/certificates", "GET",
                        "List all certificates", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-certificates-get", "/admin/certificates/*", "GET",
                        "Get certificate details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-certificates-stats", "/admin/certificates/stats", "GET",
                        "Get certificate statistics", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-certificates-expiring", "/admin/certificates/expiring-soon", "GET",
                        "List expiring certificates", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-certificates-expired", "/admin/certificates/expired", "GET",
                        "List expired certificates", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-certificates-create", "/admin/certificates", "POST",
                        "Create certificate", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-certificates-update", "/admin/certificates/*", "PUT",
                        "Update certificate", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-certificates-delete", "/admin/certificates/*", "DELETE",
                        "Delete certificate", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-certificates-toggle", "/admin/certificates/*/toggle", "PATCH",
                        "Toggle certificate enabled", List.of("ROLE_ADMIN")),

                // Consumer Groups APIs
                new AdminApiDefinition("admin-consumer-groups-list", "/admin/consumer-groups", "GET",
                        "List all consumer groups", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-consumer-groups-get", "/admin/consumer-groups/*", "GET",
                        "Get consumer group details", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-consumer-groups-stats", "/admin/consumer-groups/stats", "GET",
                        "Get consumer group statistics", Arrays.asList("ROLE_ADMIN", "ROLE_VIEWER")),
                new AdminApiDefinition("admin-consumer-groups-create", "/admin/consumer-groups", "POST",
                        "Create consumer group", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-consumer-groups-update", "/admin/consumer-groups/*", "PUT",
                        "Update consumer group", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-consumer-groups-delete", "/admin/consumer-groups/*", "DELETE",
                        "Delete consumer group", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-consumer-groups-toggle", "/admin/consumer-groups/*/toggle", "PATCH",
                        "Toggle consumer group enabled", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-consumer-groups-add-member", "/admin/consumer-groups/*/consumers/*", "POST",
                        "Add consumer to group", List.of("ROLE_ADMIN")),
                new AdminApiDefinition("admin-consumer-groups-remove-member", "/admin/consumer-groups/*/consumers/*", "DELETE",
                        "Remove consumer from group", List.of("ROLE_ADMIN"))
        );

        int registered = 0;
        int skipped = 0;

        for (AdminApiDefinition def : adminApis) {
            Optional<GatewayApi> existing = apiRepository.findByName(def.name);

            if (existing.isPresent()) {
                skipped++;
                continue;
            }

            GatewayApi api = GatewayApi.builder()
                    .name(def.name)
                    .path(def.path)
                    .method(def.method)
                    .description(def.description)
                    .service(service)
                    .authRequired(true)
                    .authType("JWT")
                    .requiredRoles(def.requiredRoles)
                    .validationEnabled(false)
                    .enabled(true)
                    .priority(0)
                    .build();

            apiRepository.save(api);
            registered++;
        }

        log.info("Admin APIs registered: {} new, {} skipped (already exist)", registered, skipped);
    }

    private record AdminApiDefinition(
            String name,
            String path,
            String method,
            String description,
            List<String> requiredRoles
    ) {
    }
}
