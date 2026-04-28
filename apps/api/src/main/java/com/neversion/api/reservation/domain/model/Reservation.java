package com.neversion.api.reservation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a storefront reservation.
 * US-009: PK migrated from UUID to Long (BIGINT IDENTITY).
 * 'id' (Long) — internal PK. 'uuid' (UUID) — external identifier.
 */
@Getter
@Setter
@Builder
public class Reservation {

    private Long id;
    private UUID uuid;

    /** Internal DB PK of the Client — resolved from clientUuid before persistence. */
    private Long clientId;

    /** External identifier of the Client — received from the REST layer. */
    private UUID clientUuid;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-009). */
    private Long vendorId;

    private BigDecimal discount;
    private BigDecimal total;
    private String receiptUrl;

    /** Payment method selected by the client at checkout (EPIC-05, BR-06). */
    private String paymentMethod;

    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    private Instant expirationDate;
    private Instant createdAt;
    private String notes;
    private List<ReservationDetail> details;

    public Reservation() {
    }

    public Reservation(Long id, UUID uuid, Long clientId, UUID clientUuid, Long vendorId,
            BigDecimal discount, BigDecimal total, String receiptUrl, String paymentMethod,
            ReservationStatus status, Instant expirationDate,
            Instant createdAt, String notes, List<ReservationDetail> details) {
        this.id = id;
        this.uuid = uuid;
        this.clientId = clientId;
        this.clientUuid = clientUuid;
        this.vendorId = vendorId;
        this.discount = discount;
        this.total = total;
        this.receiptUrl = receiptUrl;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.notes = notes;
        this.details = details;
    }
}
