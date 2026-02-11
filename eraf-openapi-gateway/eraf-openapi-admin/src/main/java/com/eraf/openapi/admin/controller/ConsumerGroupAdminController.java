package com.eraf.openapi.admin.controller;

import com.eraf.core.response.ApiResponse;
import com.eraf.openapi.admin.dto.ConsumerGroupRequest;
import com.eraf.openapi.admin.dto.ConsumerGroupResponse;
import com.eraf.openapi.admin.service.ConsumerGroupAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Consumer Group 관리 REST API
 */
@RestController
@RequestMapping("/admin/consumer-groups")
@RequiredArgsConstructor
public class ConsumerGroupAdminController {

    private final ConsumerGroupAdminService consumerGroupAdminService;

    /**
     * 모든 Consumer Group 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsumerGroupResponse>>> getAllConsumerGroups(
            @RequestParam(required = false, defaultValue = "false") boolean enabledOnly) {
        List<ConsumerGroupResponse> groups = enabledOnly ?
                consumerGroupAdminService.findAllEnabled() :
                consumerGroupAdminService.findAll();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * Consumer Group 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> getConsumerGroup(@PathVariable Long id) {
        ConsumerGroupResponse group = consumerGroupAdminService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /**
     * Consumer Group 상세 조회 (Name)
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> getConsumerGroupByName(@PathVariable String name) {
        ConsumerGroupResponse group = consumerGroupAdminService.findByName(name);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /**
     * Consumer Group 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> createConsumerGroup(@Valid @RequestBody ConsumerGroupRequest request) {
        ConsumerGroupResponse created = consumerGroupAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /**
     * Consumer Group 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> updateConsumerGroup(
            @PathVariable Long id,
            @Valid @RequestBody ConsumerGroupRequest request) {
        ConsumerGroupResponse updated = consumerGroupAdminService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Consumer Group 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConsumerGroup(@PathVariable Long id) {
        consumerGroupAdminService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Consumer Group 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> toggleConsumerGroup(@PathVariable Long id) {
        ConsumerGroupResponse updated = consumerGroupAdminService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Consumer를 그룹에 추가
     */
    @PostMapping("/{groupId}/consumers/{consumerId}")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> addConsumer(
            @PathVariable Long groupId,
            @PathVariable Long consumerId) {
        ConsumerGroupResponse updated = consumerGroupAdminService.addConsumer(groupId, consumerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(updated));
    }

    /**
     * Consumer를 그룹에서 제거
     */
    @DeleteMapping("/{groupId}/consumers/{consumerId}")
    public ResponseEntity<ApiResponse<ConsumerGroupResponse>> removeConsumer(
            @PathVariable Long groupId,
            @PathVariable Long consumerId) {
        ConsumerGroupResponse updated = consumerGroupAdminService.removeConsumer(groupId, consumerId);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Consumer가 속한 그룹 조회
     */
    @GetMapping("/by-consumer/{consumerId}")
    public ResponseEntity<ApiResponse<List<ConsumerGroupResponse>>> getGroupsByConsumer(@PathVariable Long consumerId) {
        List<ConsumerGroupResponse> groups = consumerGroupAdminService.findGroupsByConsumerId(consumerId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    /**
     * Consumer Group 통계
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = consumerGroupAdminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
