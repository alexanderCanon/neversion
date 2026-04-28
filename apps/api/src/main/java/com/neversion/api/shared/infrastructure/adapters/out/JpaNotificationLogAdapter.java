package com.neversion.api.shared.infrastructure.adapters.out;

import com.neversion.api.shared.port.out.NotificationLogPort;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing the NotificationLogPort outbound port.
 * Always inserts with status='pending'.
 * Agent Notifications is responsible for processing and updating status.
 */
@Component
public class JpaNotificationLogAdapter implements NotificationLogPort {

    private final SpringDataNotificationLogRepository repository;

    public JpaNotificationLogAdapter(SpringDataNotificationLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void record(String type, String recipientEmail, String payload) {
        NotificationLogEntity entity = NotificationLogEntity.builder()
                .type(type)
                .recipientEmail(recipientEmail)
                .payload(payload)
                .build();
        repository.save(entity);
    }
}
