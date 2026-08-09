package com.neversion.api.shared.infrastructure.adapters.out;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.shared.port.out.NotificationLogPort;

import org.springframework.context.annotation.Primary;

/**
 * JPA adapter implementing the NotificationLogPort outbound port.
 * Always inserts with status='pending'.
 * NotificationWorker is responsible for processing and updating status.
 */
@Component
@Primary
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

    @Override
    public void record(String type, String recipientEmail, String payload,
                       String entityType, Long entityId, String stage) {
        NotificationLogEntity entity = NotificationLogEntity.builder()
                .type(type)
                .recipientEmail(recipientEmail)
                .payload(payload)
                .entityType(entityType)
                .entityId(entityId)
                .stage(stage)
                .build();
        repository.save(entity);
    }

    @Override
    public boolean existsByEntityAndStage(String entityType, Long entityId, String stage) {
        return repository.existsByEntityTypeAndEntityIdAndStage(entityType, entityId, stage);
    }

    @Override
    public List<PendingNotification> findPending(int limit) {
        return repository.findByStatus("pending", PageRequest.of(0, limit))
                .stream()
                .map(e -> new PendingNotification(e.getId(), e.getType(), e.getRecipientEmail(), e.getPayload()))
                .toList();
    }

    @Override
    @Transactional
    public void markSent(Long id) {
        repository.markSent(id);
    }

    @Override
    @Transactional
    public void markFailed(Long id, String errorMessage) {
        repository.markFailed(id, errorMessage);
    }
}
