package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for the 'subscriptions' table.
 * The active link between a Client and a specific Profile, defining their payment timeline.
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 * 'paymentDueDate' is the critical field monitored by n8n background automations.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** FK to clients.id — the customer holding this subscription. */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** FK to profiles.id — the specific streaming profile assigned to this client. */
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    /** Date the client's access lifecycle began. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The date by which the client must pay to keep access.
     * Polled by n8n at 7-day, 3-day and overdue intervals.
     */
    @Column(name = "payment_due_date", nullable = false)
    private LocalDate paymentDueDate;

    /** Number of months the client has paid so far. */
    @Column(name = "months_paid", nullable = false)
    private Long monthsPaid;

    /**
     * Current access status.
     * Values: ACTIVE | SUSPENDED | CANCELLED (stored as VARCHAR in DB).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubStatus status;

    /** Admin notes for this subscription (e.g. "has 35 credit"). */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (startDate == null) startDate = LocalDate.now();
        if (monthsPaid == null) monthsPaid = 1L;
    }
}
