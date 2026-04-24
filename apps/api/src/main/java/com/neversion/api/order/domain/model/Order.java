package com.neversion.api.order.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for an order.
 * Created upon admin validation of a reservation receipt.
 *
 * US-008: PK migrated from UUID to Long (BIGINT IDENTITY).
 * 'id' (Long) — internal PK. 'uuid' (UUID) — external identifier.
 */
@Getter
@Setter
@Builder
public class Order {

    /** Internal DB PK — BIGINT IDENTITY (US-008). */
    private Long id;

    /** External identifier — exposed via API (US-008). */
    private UUID uuid;

    /** FK to reservations — stays as UUID until US-009 normalizes reservations. */
    private UUID reservationId;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-008). */
    private Long vendorId;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    private String notes;
    private Instant createdAt;

    public Order() {
    }

    public Order(Long id, UUID uuid, UUID reservationId, Long vendorId,
            OrderStatus status, String notes, Instant createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.reservationId = reservationId;
        this.vendorId = vendorId;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
