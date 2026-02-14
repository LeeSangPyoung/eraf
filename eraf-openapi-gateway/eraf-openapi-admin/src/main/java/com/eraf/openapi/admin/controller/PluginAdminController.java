package com.eraf.openapi.admin.controller;

import com.eraf.response.ApiResponse;
import com.eraf.openapi.admin.dto.PluginRequest;
import com.eraf.openapi.admin.dto.PluginResponse;
import com.eraf.openapi.admin.service.PluginAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Plugin 관리 REST API
 */
@RestController
@RequestMapping("/admin/plugins")
@RequiredArgsConstructor
public class PluginAdminController {

    private final PluginAdminService pluginAdminService;

    /**
     * 모든 Plugin 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PluginResponse>>> getAllPlugins(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<PluginResponse> plugins = enabledOnly ?
                pluginAdminService.findAllEnabled() :
                pluginAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(plugins));
    }

    /**
     * Plugin 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PluginResponse>> getPlugin(@PathVariable Long id) {
        PluginResponse plugin = pluginAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(plugin));
    }

    /**
     * Route ID로 Plugin 조회
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<PluginResponse>>> getPluginsByRoute(@PathVariable Long routeId) {
        List<PluginResponse> plugins = pluginAdminService.findByRouteId(routeId);
        return ResponseEntity.ok(ApiResponse.success(plugins));
    }

    /**
     * Service ID로 Plugin 조회
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<PluginResponse>>> getPluginsByService(@PathVariable Long serviceId) {
        List<PluginResponse> plugins = pluginAdminService.findByServiceId(serviceId);
        return ResponseEntity.ok(ApiResponse.success(plugins));
    }

    /**
     * Global Plugin 조회
     */
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<List<PluginResponse>>> getGlobalPlugins() {
        List<PluginResponse> plugins = pluginAdminService.findGlobalPlugins();
        return ResponseEntity.ok(ApiResponse.success(plugins));
    }

    /**
     * Plugin 이름으로 조회
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<List<PluginResponse>>> getPluginsByName(@PathVariable String name) {
        List<PluginResponse> plugins = pluginAdminService.findByName(name);
        return ResponseEntity.ok(ApiResponse.success(plugins));
    }

    /**
     * Plugin 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PluginResponse>> createPlugin(@Valid @RequestBody PluginRequest request) {
        PluginResponse created = pluginAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Plugin 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PluginResponse>> updatePlugin(
            @PathVariable Long id,
            @Valid @RequestBody PluginRequest request) {
        PluginResponse updated = pluginAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Plugin 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlugin(@PathVariable Long id) {
        pluginAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Plugin 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<PluginResponse>> togglePluginEnabled(@PathVariable Long id) {
        PluginResponse updated = pluginAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Plugin 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPluginStats() {
        return ResponseEntity.ok(ApiResponse.success(pluginAdminService.getStats()));
    }
}
