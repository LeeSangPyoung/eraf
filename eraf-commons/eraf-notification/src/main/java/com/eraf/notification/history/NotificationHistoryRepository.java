package com.eraf.notification.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * 알림 이력 Repository
 */
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    /**
     * 알림 타입별 조회
     */
    Page<NotificationHistory> findByNotificationType(
            NotificationHistory.NotificationType type,
            Pageable pageable);

    /**
     * 상태별 조회
     */
    Page<NotificationHistory> findByStatus(
            NotificationHistory.NotificationStatus status,
            Pageable pageable);

    /**
     * 수신자별 조회
     */
    Page<NotificationHistory> findByRecipient(String recipient, Pageable pageable);

    /**
     * 기간별 조회
     */
    Page<NotificationHistory> findBySentAtBetween(
            Instant startDate,
            Instant endDate,
            Pageable pageable);

    /**
     * 실패한 알림 조회
     */
    @Query("SELECT n FROM NotificationHistory n WHERE n.status = 'FAILED' " +
           "AND n.retryCount < :maxRetries ORDER BY n.sentAt DESC")
    List<NotificationHistory> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * 알림 타입별 통계
     */
    @Query("SELECT n.notificationType, n.status, COUNT(n) FROM NotificationHistory n " +
           "WHERE n.sentAt >= :since " +
           "GROUP BY n.notificationType, n.status")
    List<Object[]> getStatisticsSince(@Param("since") Instant since);

    /**
     * 성공률 계산
     */
    @Query("SELECT COUNT(CASE WHEN n.status = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(n) " +
           "FROM NotificationHistory n WHERE n.notificationType = :type " +
           "AND n.sentAt >= :since")
    Double getSuccessRate(
            @Param("type") NotificationHistory.NotificationType type,
            @Param("since") Instant since);

    /**
     * 평균 응답 시간
     */
    @Query("SELECT AVG(n.responseTimeMs) FROM NotificationHistory n " +
           "WHERE n.notificationType = :type AND n.status = 'SUCCESS' " +
           "AND n.sentAt >= :since")
    Double getAverageResponseTime(
            @Param("type") NotificationHistory.NotificationType type,
            @Param("since") Instant since);

    /**
     * 최근 실패 알림 (최근 1시간)
     */
    @Query("SELECT n FROM NotificationHistory n WHERE n.status = 'FAILED' " +
           "AND n.sentAt >= :since ORDER BY n.sentAt DESC")
    List<NotificationHistory> findRecentFailures(@Param("since") Instant since);

    /**
     * 오래된 이력 삭제용 조회
     */
    List<NotificationHistory> findBySentAtBefore(Instant date);

    /**
     * 수신자의 최근 알림 조회
     */
    List<NotificationHistory> findTop10ByRecipientOrderBySentAtDesc(String recipient);
}
