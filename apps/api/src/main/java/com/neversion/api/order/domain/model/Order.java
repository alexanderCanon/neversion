package com.neversion.api.order.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for an order.
 * Created upon vendor validation of a reservation receipt (US-035).
 *
 * US-008: PK migrated from UUID to Long (BIGINT IDENTITY).
 * 'id' (Long) — internal PK. 'uuid' (UUID) — external identifier.
 * EPIC-05: Added clientId, paymentMethod, approvedAt.
 */
@Getter
@Setter
@Builder
public class Order {

    /** Internal DB PK — BIGINT IDENTITY (US-008). */
    private Long id;

    /** External identifier — exposed via API (US-008). */
    private UUID uuid;

    /** FK to reservations.id — BIGINT after US-009 normalization. */
    private Long reservationId;

    /** FK to clients.id — the client who placed the order (EPIC-05). */
    private Long clientId;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-008). */
    private Long vendorId;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Payment method provided by the client at checkout (BR-06). */
    private String paymentMethod;

    private String notes;

    /** Timestamp when the vendor approved the receipt (US-035). */
    private Instant approvedAt;

    private Instant createdAt;

    public Order() {
    }

    public Order(Long id, UUID uuid, Long reservationId, Long clientId, Long vendorId,
            OrderStatus status, String paymentMethod, String notes,
            Instant approvedAt, Instant createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.reservationId = reservationId;
        this.clientId = clientId;
        this.vendorId = vendorId;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
    }
}
