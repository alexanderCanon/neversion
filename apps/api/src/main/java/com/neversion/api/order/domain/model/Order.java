package com.neversion.api.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.shared.domain.model.enums.AccountPreference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /** Internal DB PK — BIGINT IDENTITY (US-008). */
    private Long id;

    /** External identifier — exposed via API (US-008). */
    private UUID uuid;

    /** FK to reservations.id — BIGINT after US-009 normalization. */
    private Long reservationId;

    /** External identifier of the reservation. */
    private UUID reservationUuid;

    /** FK to clients.id — the client who placed the order (EPIC-05). */
    private Long clientId;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-008). */
    private Long vendorId;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Payment method provided by the client at checkout (BR-06). */
    private String paymentMethod;

    /** De-normalized from reservation.accountPreference at order creation. */
    private AccountPreference accountPreference;

    private String notes;

    private String receiptUrl;

    /** De-normalized from reservation at creation (US-037 listing). */
    private BigDecimal total;
    private BigDecimal discount;

    /** Timestamp when the vendor approved the receipt (US-035). */
    private Instant approvedAt;

    private Instant createdAt;
}
