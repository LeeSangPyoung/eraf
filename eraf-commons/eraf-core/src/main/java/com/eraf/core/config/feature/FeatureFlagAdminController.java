package com.eraf.core.config.feature;

import com.eraf.core.config.feature.dto.FeatureFlagRequest;
import com.eraf.core.config.feature.dto.FeatureFlagResponse;
import com.eraf.core.config.feature.dto.FeatureFlagStats;
import com.eraf.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feature Flag Admin Controller
 *
 * <p>REST API for managing feature flags (CRUD operations, statistics, cache management)
 *
 * @author ERAF Team
 * @since Phase 2
 */
@RestController
@RequestMapping("/admin/feature-flags")
@ConditionalOnProperty(name = "eraf.feature-flag.admin-enabled", havingValue = "true", matchIfMissing = true)
public class FeatureFlagAdminController {

    private final FeatureFlagAdminService adminService;

    public FeatureFlagAdminController(FeatureFlagAdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Create a new feature flag
     *
     * @param request the feature flag request
     * @return the created feature flag
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> create(@Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = adminService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update an existing feature flag
     *
     * @param id the feature flag ID
     * @param request the feature flag request
     * @return the updated feature flag
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FeatureFlagRequest request) {
        FeatureFlagResponse response = adminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete a feature flag
     *
     * @param id the feature flag ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Get feature flag by ID
     *
     * @param id the feature flag ID
     * @return the feature flag
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getById(@PathVariable Long id) {
        FeatureFlagResponse response = adminService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get feature flag by key
     *
     * @param key the feature flag key
     * @return the feature flag
     */
    @GetMapping("/key/{key}")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getByKey(@PathVariable String key) {
        FeatureFlagResponse response = adminService.getByKey(key);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all feature flags
     *
     * @return list of all feature flags
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getAll() {
        List<FeatureFlagResponse> responses = adminService.getAll();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Toggle feature flag enabled status
     *
     * @param id the feature flag ID
     * @return the updated feature flag
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> toggle(@PathVariable Long id) {
        FeatureFlagResponse response = adminService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get feature flag statistics
     *
     * @return feature flag statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FeatureFlagStats>> getStats() {
        FeatureFlagStats stats = adminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Reload all feature flag caches
     *
     * @return success response
     */
    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<String>> reloadCache() {
        adminService.reloadCache();
        return ResponseEntity.ok(ApiResponse.success("All caches reloaded successfully"));
    }

    /**
     * Evaluate a feature flag with context
     *
     * @param request evaluation request with flagKey and optional context
     * @return evaluation result
     */
    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evaluate(@RequestBody Map<String, Object> request) {
        String flagKey = (String) request.get("flagKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) request.get("context");

        Map<String, Object> result = adminService.evaluate(flagKey, context);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
