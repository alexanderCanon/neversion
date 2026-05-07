package com.neversion.api.shared.infrastructure.adapters.out;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the notification_log table.
 * Infrastructure concern only — never crosses the domain boundary.
 * <p>
 * Backend inserts records (status=pending). NotificationWorker processes
 * and updates status to 'sent' or 'failed' after dispatch.
 */
@Entity
@Table(name = "notification_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /** Notification type identifier, e.g. VENDOR_WELCOME. */
    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    /** JSON string with template variables (email, storeName, temporaryPassword, etc.). */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * Lifecycle status: pending → sent | failed.
     * Backend always inserts as 'pending'.
     */
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "pending";

    // ── EPIC-08 fields (V26) ────────────────────────────────────────────────

    /** Entity type for categorization and dedup: 'client', 'order', 'subscription', 'vendor'. */
    @Column(name = "entity_type", length = 30)
    private String entityType;

    /** FK to the entity (internal BIGINT ID). Used with entity_type+stage for dedup. */
    @Column(name = "entity_id")
    private Long entityId;

    /** Stage within the entity lifecycle: 'welcome', 'approved', 'reminder_7d', 'due', etc. */
    @Column(length = 30)
    private String stage;

    /** Timestamp when the worker processed this record (sent or failed). */
    @Column(name = "processed_at")
    private Instant processedAt;

    /** Error message if sending failed. */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
