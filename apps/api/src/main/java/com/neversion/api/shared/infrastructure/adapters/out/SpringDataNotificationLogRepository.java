package com.neversion.api.shared.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the notification_log table.
 */
public interface SpringDataNotificationLogRepository
        extends JpaRepository<NotificationLogEntity, Long> {

    Optional<NotificationLogEntity> findByUuid(UUID uuid);

    List<NotificationLogEntity> findByStatus(String status, Pageable pageable);

    boolean existsByEntityTypeAndEntityIdAndStage(String entityType, Long entityId, String stage);

    @Modifying
    @Query("UPDATE NotificationLogEntity n SET n.status = 'sent', n.processedAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markSent(@Param("id") Long id);

    @Modifying
    @Query("UPDATE NotificationLogEntity n SET n.status = 'failed', n.processedAt = CURRENT_TIMESTAMP, n.errorMessage = :errorMessage WHERE n.id = :id")
    void markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
