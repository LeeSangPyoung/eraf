package com.eraf.jpa.audit;

import com.eraf.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * AuditLogAdminController 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AuditLogAdminControllerTest {

    @Mock
    private AuditLogQueryService queryService;

    @Mock
    private AuditLogJpaRepository repository;

    private AuditLogAdminController controller;

    private List<AuditLogEntity> testEntities;

    @BeforeEach
    void setUp() {
        controller = new AuditLogAdminController(queryService, repository);

        // 테스트 데이터 생성
        testEntities = List.of(
                createEntity(1L, "user1", "LOGIN", "USER", "user1", AuditEventStandard.Result.SUCCESS),
                createEntity(2L, "user1", "READ", "DOCUMENT", "doc123", AuditEventStandard.Result.SUCCESS),
                createEntity(3L, "user2", "CREATE", "DOCUMENT", "doc456", AuditEventStandard.Result.SUCCESS),
                createEntity(4L, "user2", "DELETE", "DOCUMENT", "doc789", AuditEventStandard.Result.FAILURE),
                createEntity(5L, "user3", "LOGIN_FAILED", "USER", "user3", AuditEventStandard.Result.FAILURE),
                createEntity(6L, "user3", "ACCESS_DENIED", "DOCUMENT", "doc999", AuditEventStandard.Result.DENIED)
        );
    }

    @Test
    @DisplayName("검색 - userId 필터")
    void testSearch_ByUserId() {
        // given
        List<AuditLogEntity> user1Entities = testEntities.stream()
                .filter(e -> "user1".equals(e.getUserId())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(user1Entities, PageRequest.of(0, 20), user1Entities.size());
        when(queryService.search(any(), any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.search("user1", null, null, null, null, null, null, null, null,
                        null, null, false, 0, 20, new String[]{"timestamp", "desc"});

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent()).allMatch(log -> log.getUserId().equals("user1"));
    }

    @Test
    @DisplayName("검색 - action 필터")
    void testSearch_ByAction() {
        // given
        List<AuditLogEntity> loginEntities = testEntities.stream()
                .filter(e -> "LOGIN".equals(e.getAction())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(loginEntities, PageRequest.of(0, 20), loginEntities.size());
        when(queryService.search(any(), any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.search(null, null, List.of("LOGIN"), null, null, null, null, null, null,
                        null, null, false, 0, 20, new String[]{"timestamp", "desc"});

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getAction()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("검색 - resource 필터")
    void testSearch_ByResource() {
        // given
        List<AuditLogEntity> docEntities = testEntities.stream()
                .filter(e -> "DOCUMENT".equals(e.getResource())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(docEntities, PageRequest.of(0, 20), docEntities.size());
        when(queryService.search(any(), any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.search(null, null, null, "DOCUMENT", null, null, null, null, null,
                        null, null, false, 0, 20, new String[]{"timestamp", "desc"});

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(4);
        assertThat(resultPage.getContent()).allMatch(log -> log.getResource().equals("DOCUMENT"));
    }

    @Test
    @DisplayName("검색 - result 필터 (실패)")
    void testSearch_ByResult_Failure() {
        // given
        List<AuditLogEntity> failureEntities = testEntities.stream()
                .filter(e -> AuditEventStandard.Result.FAILURE.equals(e.getResult())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(failureEntities, PageRequest.of(0, 20), failureEntities.size());
        when(queryService.search(any(), any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.search(null, null, null, null, null,
                        List.of(AuditEventStandard.Result.FAILURE), null, null, null,
                        null, null, false, 0, 20, new String[]{"timestamp", "desc"});

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("ID로 조회")
    void testGetById() {
        // given
        AuditLogEntity entity = testEntities.get(0);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // when
        ResponseEntity<ApiResponse<AuditLogResponse>> response = controller.getById(1L);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        AuditLogResponse log = response.getBody().getData();
        assertThat(log).isNotNull();
        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getUserId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("ID로 조회 - 존재하지 않는 경우")
    void testGetById_NotFound() {
        // given
        when(repository.findById(999999L)).thenReturn(Optional.empty());

        // when
        ResponseEntity<ApiResponse<AuditLogResponse>> response = controller.getById(999999L);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("사용자별 최근 활동 조회")
    void testGetRecentActivitiesByUser() {
        // given
        List<AuditLogEntity> user1Entities = testEntities.stream()
                .filter(e -> "user1".equals(e.getUserId())).toList();
        when(queryService.findRecentActivitiesByUser("user1", 50)).thenReturn(user1Entities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getRecentActivitiesByUser("user1", 50);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(2);
        assertThat(logs).allMatch(log -> log.getUserId().equals("user1"));
    }

    @Test
    @DisplayName("리소스별 변경 이력 조회")
    void testGetResourceHistory() {
        // given
        List<AuditLogEntity> docEntities = testEntities.stream()
                .filter(e -> "DOCUMENT".equals(e.getResource()) && "doc123".equals(e.getResourceId())).toList();
        when(queryService.findResourceHistory("DOCUMENT", "doc123", 50)).thenReturn(docEntities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getResourceHistory("DOCUMENT", "doc123", 50);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getResource()).isEqualTo("DOCUMENT");
        assertThat(logs.get(0).getResourceId()).isEqualTo("doc123");
    }

    @Test
    @DisplayName("특정 기간 조회")
    void testGetByPeriod() {
        // given
        Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant to = Instant.now().plus(1, ChronoUnit.HOURS);
        when(queryService.findByPeriod(from, to, 100)).thenReturn(testEntities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getByPeriod(from, to, 100);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(6);
    }

    @Test
    @DisplayName("최근 N일 조회")
    void testGetRecentDays() {
        // given
        when(queryService.findRecentDays(7, 100)).thenReturn(testEntities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getRecentDays(7, 100);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(6);
    }

    @Test
    @DisplayName("특정 액션 조회")
    void testGetByAction() {
        // given
        List<AuditLogEntity> loginEntities = testEntities.stream()
                .filter(e -> "LOGIN".equals(e.getAction())).toList();
        when(queryService.findByAction("LOGIN", 50)).thenReturn(loginEntities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getByAction("LOGIN", 50);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("실패한 작업 조회")
    void testGetFailures() {
        // given
        List<AuditLogEntity> failureEntities = testEntities.stream()
                .filter(e -> AuditEventStandard.Result.FAILURE.equals(e.getResult())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(failureEntities, PageRequest.of(0, 20), failureEntities.size());
        when(queryService.findFailures(any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.getFailures(0, 20);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("접근 거부 조회")
    void testGetAccessDenied() {
        // given
        List<AuditLogEntity> deniedEntities = testEntities.stream()
                .filter(e -> AuditEventStandard.Result.DENIED.equals(e.getResult())).toList();
        Page<AuditLogEntity> page = new PageImpl<>(deniedEntities, PageRequest.of(0, 20), deniedEntities.size());
        when(queryService.findAccessDenied(any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.getAccessDenied(0, 20);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getResult()).isEqualTo(AuditEventStandard.Result.DENIED);
    }

    @Test
    @DisplayName("로그인 실패 이력 조회")
    void testGetLoginFailures() {
        // given
        List<AuditLogEntity> loginFailEntities = testEntities.stream()
                .filter(e -> "LOGIN_FAILED".equals(e.getAction())).toList();
        when(queryService.findLoginFailures("user3", 20)).thenReturn(loginFailEntities);

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getLoginFailures("user3", 20);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("LOGIN_FAILED");
    }

    @Test
    @DisplayName("트레이스 ID로 조회")
    void testGetByTraceId() {
        // given
        String traceId = "trace-123";
        AuditLogEntity e1 = createEntity(7L, "user1", "CREATE", "ORDER", "order1", AuditEventStandard.Result.SUCCESS);
        e1.setTraceId(traceId);
        AuditLogEntity e2 = createEntity(8L, "user1", "UPDATE", "ORDER", "order1", AuditEventStandard.Result.SUCCESS);
        e2.setTraceId(traceId);
        when(queryService.findByTraceId(traceId)).thenReturn(List.of(e1, e2));

        // when
        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response =
                controller.getByTraceId(traceId);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<AuditLogResponse> logs = response.getBody().getData();
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(2);
        assertThat(logs).allMatch(log -> log.getTraceId().equals(traceId));
    }

    @Test
    @DisplayName("통계 조회")
    void testGetStats() {
        // given
        when(repository.count()).thenReturn(6L);
        when(repository.countByResult(AuditEventStandard.Result.FAILURE)).thenReturn(2L);
        when(repository.countByResult(AuditEventStandard.Result.ERROR)).thenReturn(0L);
        when(repository.countByResult(AuditEventStandard.Result.DENIED)).thenReturn(1L);

        // when
        ResponseEntity<ApiResponse<AuditLogAdminController.AuditLogStats>> response =
                controller.getStats();

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        AuditLogAdminController.AuditLogStats stats = response.getBody().getData();
        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(6);
        assertThat(stats.getFailures()).isEqualTo(2);
        assertThat(stats.getErrors()).isEqualTo(0);
        assertThat(stats.getDenied()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이징 검색")
    void testSearch_Pagination() {
        // given
        List<AuditLogEntity> pageContent = testEntities.subList(3, 6);
        Page<AuditLogEntity> page = new PageImpl<>(pageContent, PageRequest.of(1, 3), 6);
        when(queryService.search(any(), any(Pageable.class))).thenReturn(page);

        // when
        ResponseEntity<ApiResponse<Page<AuditLogResponse>>> response =
                controller.search(null, null, null, null, null, null, null, null, null,
                        null, null, false, 1, 3, new String[]{"timestamp", "desc"});

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Page<AuditLogResponse> resultPage = response.getBody().getData();
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(6);
        assertThat(resultPage.getTotalPages()).isEqualTo(2);
        assertThat(resultPage.getNumber()).isEqualTo(1);
        assertThat(resultPage.getContent()).hasSize(3);
    }

    // Helper methods
    private AuditLogEntity createEntity(Long id, String userId, String action,
                                         String resource, String resourceId, String result) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(id);
        entity.setTimestamp(Instant.now());
        entity.setUserId(userId);
        entity.setUsername(userId);
        entity.setAction(action);
        entity.setResource(resource);
        entity.setResourceId(resourceId);
        entity.setResult(result);
        entity.setDeleted(false);
        return entity;
    }
}
