package com.neversion.api.shared.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the notification_log table.
 */
public interface SpringDataNotificationLogRepository
        extends JpaRepository<NotificationLogEntity, Long> {
}
